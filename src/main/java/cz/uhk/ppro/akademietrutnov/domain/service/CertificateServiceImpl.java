package cz.uhk.ppro.akademietrutnov.domain.service;

import cz.uhk.ppro.akademietrutnov.domain.exception.BusinessRuleException;
import cz.uhk.ppro.akademietrutnov.domain.exception.EntityNotFoundException;
import cz.uhk.ppro.akademietrutnov.domain.model.Certificate;
import cz.uhk.ppro.akademietrutnov.domain.model.CourseTerm;
import cz.uhk.ppro.akademietrutnov.domain.model.Registration;
import cz.uhk.ppro.akademietrutnov.domain.model.RegistrationStatus;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.CertificateRepository;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CertificateServiceImpl implements CertificateService {

    private final CertificateRepository certificateRepository;
    private final RegistrationRepository registrationRepository;
    private final AttendanceService attendanceService;

    public CertificateServiceImpl(CertificateRepository certificateRepository,
                                  RegistrationRepository registrationRepository,
                                  AttendanceService attendanceService) {
        this.certificateRepository = certificateRepository;
        this.registrationRepository = registrationRepository;
        this.attendanceService = attendanceService;
    }

    @Override
    public synchronized Certificate issueCertificate(Long registrationId, Long currentUserId, boolean isAdmin) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new EntityNotFoundException("Přihláška s ID " + registrationId + " nebyla nalezena."));

        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new BusinessRuleException("Zrušené přihlášce nelze vystavit osvědčení.");
        }

        if (registration.getStatus() == RegistrationStatus.WAITLISTED) {
            throw new BusinessRuleException("Účastníkovi v pořadníku náhradníků nelze vystavit osvědčení.");
        }

        if (certificateRepository.findByRegistrationId(registrationId).isPresent()) {
            throw new BusinessRuleException("Osvědčení pro tuto přihlášku již bylo vystaveno. Vydané osvědčení je neměnné.");
        }

        CourseTerm term = registration.getTerm();

        // Oprávnění: vystavit může pouze administrátor nebo lektor přiřazený k tomuto termínu
        if (!isAdmin) {
            boolean isAssignedLecturer = term.getLecturers().stream()
                    .anyMatch(l -> l.getId().equals(currentUserId));
            if (!isAssignedLecturer) {
                throw new BusinessRuleException("Nemáte oprávnění vystavit osvědčení pro tento kurz (nejste administrátor ani vyučující lektor).");
            }
        }

        // Kontrola minimální docházky 70 % (ADR-01)
        int totalDays = term.getTermDays().size();
        if (totalDays > 0) {
            double attendanceRate = attendanceService.getStudentAttendancePercentage(registrationId);
            if (attendanceRate < 70.0) {
                throw new BusinessRuleException("Podmínkou pro úspěšné dokončení kurzu je alespoň 70% docházka. Účastník dosáhl pouze " +
                        attendanceRate + " % (" + attendanceService.getStudentAttendedDaysCount(registrationId) + "/" + totalDays + " dnů).");
            }
        }

        // Generování striktně unikátního evidenčního čísla (např. OSV-2026-0001)
        int year = LocalDate.now().getYear();
        long nextSeq = certificateRepository.countByYear(year) + 1;
        String certificateNumber = String.format("OSV-%d-%04d", year, nextSeq);

        while (certificateRepository.findByCertificateNumber(certificateNumber).isPresent()) {
            nextSeq++;
            certificateNumber = String.format("OSV-%d-%04d", year, nextSeq);
        }

        Certificate certificate = new Certificate(
                registration,
                certificateNumber,
                LocalDate.now(),
                registration.getStudent().getFullName(),
                term.getCourse().getName(),
                term.getCourse().getHoursDuration(),
                term.getCourse().isAccredited()
        );

        Certificate saved = certificateRepository.save(certificate);

        // Aktualizace stavu přihlášky na úspěšně dokončeno
        registration.setStatus(RegistrationStatus.COMPLETED);
        registrationRepository.save(registration);

        return saved;
    }

    @Override
    public void rejectCompletion(Long registrationId, String reason, Long currentUserId, boolean isAdmin) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new EntityNotFoundException("Přihláška s ID " + registrationId + " nebyla nalezena."));

        if (registration.getStatus() == RegistrationStatus.COMPLETED) {
            throw new BusinessRuleException("Kurz již byl úspěšně dokončen a osvědčení vystaveno. Status nelze změnit.");
        }

        registration.setStatus(RegistrationStatus.FAILED);
        registration.setCancellationReason(reason != null ? reason : "Nesplnění podmínek kurzu");
        registrationRepository.save(registration);
    }

    @Override
    @Transactional(readOnly = true)
    public Certificate findById(Long id) {
        return certificateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Osvědčení s ID " + id + " nebylo nalezeno."));
    }

    @Override
    @Transactional(readOnly = true)
    public Certificate findByNumber(String certificateNumber) {
        return certificateRepository.findByCertificateNumber(certificateNumber)
                .orElseThrow(() -> new EntityNotFoundException("Osvědčení s číslem " + certificateNumber + " nebylo nalezeno."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Certificate> findAll() {
        return certificateRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Certificate> findByStudentId(Long studentId) {
        return certificateRepository.findByStudentId(studentId);
    }
}
