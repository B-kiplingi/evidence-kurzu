package cz.uhk.ppro.akademietrutnov.domain.service;

import cz.uhk.ppro.akademietrutnov.domain.exception.BusinessRuleException;
import cz.uhk.ppro.akademietrutnov.domain.exception.EntityNotFoundException;
import cz.uhk.ppro.akademietrutnov.domain.model.*;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.CourseTermRepository;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.RegistrationRepository;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final CourseTermRepository courseTermRepository;
    private final UserRepository userRepository;

    public RegistrationServiceImpl(RegistrationRepository registrationRepository,
                                   CourseTermRepository courseTermRepository,
                                   UserRepository userRepository) {
        this.registrationRepository = registrationRepository;
        this.courseTermRepository = courseTermRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Registration registerStudent(Long termId, Long studentId, String exceptionReason, boolean isAdmin) {
        CourseTerm term = courseTermRepository.findById(termId)
                .orElseThrow(() -> new EntityNotFoundException("Termín kurzu s ID " + termId + " nebyl nalezen."));

        if (term.getStatus() == TermStatus.CANCELLED) {
            throw new BusinessRuleException("Na zrušený termín se nelze přihlásit.");
        }

        if (term.getStatus() == TermStatus.COMPLETED) {
            throw new BusinessRuleException("Na již ukončený termín se nelze přihlásit.");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student s ID " + studentId + " nebyl nalezen."));

        // Kontrola, zda termín již začal (ADR-04)
        if (term.isStarted()) {
            if (!isAdmin) {
                throw new BusinessRuleException("Termín již byl zahájen. Dodatečný zápis může provést pouze administrátor s uvedením důvodu výjimky.");
            }
            if (exceptionReason == null || exceptionReason.trim().isEmpty()) {
                throw new BusinessRuleException("Při dodatečném zápisu po zahájení termínu je povinné uvést důvod výjimky.");
            }
        }

        // Kontrola, zda student již na tomto termínu nemá aktivní (nezrušenou) přihlášku
        Optional<Registration> existingActive = registrationRepository.findActiveByTermIdAndStudentId(termId, studentId);
        if (existingActive.isPresent()) {
            throw new BusinessRuleException("Student již má na tento termín evidovanou přihlášku (stav: " +
                    existingActive.get().getStatus().getDisplayName() + ").");
        }

        // Kontrola souběhu termínů téhož kurzu (ADR-03)
        List<Registration> sameCourseConfirmed = registrationRepository.findConfirmedOrCompletedByStudentAndCourse(
                studentId, term.getCourse().getId());
        boolean hasActiveConfirmedSameCourse = sameCourseConfirmed.stream()
                .anyMatch(r -> r.getTerm().getStatus() != TermStatus.COMPLETED && r.getTerm().getStatus() != TermStatus.CANCELLED);

        if (hasActiveConfirmedSameCourse) {
            throw new BusinessRuleException("Student již má aktivní řádnou účast na jiném termínu kurzu '" +
                    term.getCourse().getName() + "'. Souběžná účast na více řádných termínech téhož kurzu není povolena.");
        }

        // Stanovení stavu podle kapacity termínu (Obchodní pravidlo: nepřekročitelnost kapacity)
        Registration registration;
        if (term.getConfirmedCount() < term.getCapacity()) {
            // Místo je volné -> řádné potvrzené přihlášení
            registration = new Registration(term, student, RegistrationStatus.CONFIRMED, null, exceptionReason);
        } else {
            // Kapacita je naplněna -> zařazení do pořadníku náhradníků s přesným pořadím
            long waitlistCount = registrationRepository.countByTermIdAndStatus(termId, RegistrationStatus.WAITLISTED);
            int nextPosition = (int) waitlistCount + 1;
            registration = new Registration(term, student, RegistrationStatus.WAITLISTED, nextPosition, exceptionReason);
        }

        return registrationRepository.save(registration);
    }

    @Override
    public Registration cancelRegistration(Long registrationId, String cancellationReason, boolean isAdmin, Long currentUserId) {
        Registration registration = findById(registrationId);

        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new BusinessRuleException("Přihláška již byla zrušena.");
        }

        if (registration.getStatus() == RegistrationStatus.COMPLETED) {
            throw new BusinessRuleException("Dokončenou přihlášku nelze zrušit.");
        }

        // Kontrola oprávnění: zrušit může administrátor nebo samotný přihlášený student
        if (!isAdmin && (currentUserId == null || !registration.getStudent().getId().equals(currentUserId))) {
            throw new BusinessRuleException("Nemáte oprávnění zrušit tuto přihlášku.");
        }

        RegistrationStatus previousStatus = registration.getStatus();
        Integer previousPosition = registration.getQueuePosition();

        // Soft-delete / historizace (na čem klient trvá: zrušená přihláška zůstává dohledatelná)
        registration.setStatus(RegistrationStatus.CANCELLED);
        registration.setQueuePosition(null);
        registration.setCancellationReason(cancellationReason != null ? cancellationReason.trim() : "Odhlášení účastníka");
        registration.setCancelledAt(LocalDateTime.now());
        Registration saved = registrationRepository.save(registration);

        // Pokud byl náhradník v pořadníku, posuneme zbývající náhradníky za ním dopředu
        if (previousStatus == RegistrationStatus.WAITLISTED && previousPosition != null) {
            List<Registration> followers = registrationRepository.findWaitlistedAfterPosition(
                    registration.getTerm().getId(), previousPosition);
            for (Registration follower : followers) {
                follower.setQueuePosition(follower.getQueuePosition() - 1);
                registrationRepository.save(follower);
            }
        }

        return saved;
    }

    @Override
    public Registration promoteFirstWaitlisted(Long termId) {
        CourseTerm term = courseTermRepository.findById(termId)
                .orElseThrow(() -> new EntityNotFoundException("Termín kurzu s ID " + termId + " nebyl nalezen."));

        if (term.getConfirmedCount() >= term.getCapacity()) {
            throw new BusinessRuleException("Kapacita termínu je plně obsazena (" + term.getConfirmedCount() + "/" + term.getCapacity() + "). Nelze povýšit náhradníka.");
        }

        Registration firstWaitlisted = registrationRepository.findFirstByTermIdAndStatusOrderByQueuePositionAsc(
                termId, RegistrationStatus.WAITLISTED)
                .orElseThrow(() -> new BusinessRuleException("V pořadníku náhradníků se nenachází žádný čekající zájemce."));

        firstWaitlisted.setStatus(RegistrationStatus.CONFIRMED);
        firstWaitlisted.setQueuePosition(null);
        Registration promoted = registrationRepository.save(firstWaitlisted);

        // Posun zbývajících náhradníků (všichni s pozicí > 1 se posunou o 1 dolů)
        List<Registration> remaining = registrationRepository.findByTermIdAndStatusOrderByQueuePositionAsc(
                termId, RegistrationStatus.WAITLISTED);
        int newPos = 1;
        for (Registration r : remaining) {
            r.setQueuePosition(newPos++);
            registrationRepository.save(r);
        }

        return promoted;
    }

    @Override
    @Transactional(readOnly = true)
    public Registration findById(Long id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Přihláška s ID " + id + " nebyla nalezena."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Registration> findByTermId(Long termId) {
        return registrationRepository.findByTermIdOrderByCreatedAtAsc(termId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Registration> findConfirmedByTermId(Long termId) {
        return registrationRepository.findByTermIdAndStatusOrderByCreatedAtAsc(termId, RegistrationStatus.CONFIRMED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Registration> findWaitlistedByTermId(Long termId) {
        return registrationRepository.findByTermIdAndStatusOrderByQueuePositionAsc(termId, RegistrationStatus.WAITLISTED);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Registration> findNextWaitlistCandidate(Long termId) {
        return registrationRepository.findFirstByTermIdAndStatusOrderByQueuePositionAsc(termId, RegistrationStatus.WAITLISTED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Registration> findByStudentId(Long studentId) {
        return registrationRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }
}
