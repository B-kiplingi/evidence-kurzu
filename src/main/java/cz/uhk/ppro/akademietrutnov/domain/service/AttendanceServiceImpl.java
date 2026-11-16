package cz.uhk.ppro.akademietrutnov.domain.service;

import cz.uhk.ppro.akademietrutnov.domain.exception.EntityNotFoundException;
import cz.uhk.ppro.akademietrutnov.domain.model.Attendance;
import cz.uhk.ppro.akademietrutnov.domain.model.Registration;
import cz.uhk.ppro.akademietrutnov.domain.model.TermDay;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.AttendanceRepository;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.RegistrationRepository;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.TermDayRepository;
import cz.uhk.ppro.akademietrutnov.presentation.dto.AttendanceFormDto;
import cz.uhk.ppro.akademietrutnov.presentation.dto.AttendanceRecordDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final TermDayRepository termDayRepository;
    private final RegistrationRepository registrationRepository;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository,
                                 TermDayRepository termDayRepository,
                                 RegistrationRepository registrationRepository) {
        this.attendanceRepository = attendanceRepository;
        this.termDayRepository = termDayRepository;
        this.registrationRepository = registrationRepository;
    }

    @Override
    public void saveAttendance(AttendanceFormDto formDto) {
        TermDay day = termDayRepository.findById(formDto.getTermDayId())
                .orElseThrow(() -> new EntityNotFoundException("Den rozvrhu s ID " + formDto.getTermDayId() + " nebyl nalezen."));

        for (AttendanceRecordDto record : formDto.getRecords()) {
            Registration registration = registrationRepository.findById(record.getRegistrationId())
                    .orElseThrow(() -> new EntityNotFoundException("Přihláška s ID " + record.getRegistrationId() + " nebyla nalezena."));

            Optional<Attendance> existing = attendanceRepository.findByTermDayIdAndRegistrationId(
                    day.getId(), registration.getId());

            if (existing.isPresent()) {
                Attendance att = existing.get();
                att.setPresent(record.isPresent());
                att.setNote(record.getNote());
                attendanceRepository.save(att);
            } else {
                Attendance att = new Attendance(day, registration, record.isPresent(), record.getNote());
                attendanceRepository.save(att);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> getAttendanceForDay(Long termDayId) {
        return attendanceRepository.findByTermDayId(termDayId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> getAttendanceForRegistration(Long registrationId) {
        return attendanceRepository.findByRegistrationId(registrationId);
    }

    @Override
    @Transactional(readOnly = true)
    public double getStudentAttendancePercentage(Long registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new EntityNotFoundException("Přihláška s ID " + registrationId + " nebyla nalezena."));

        int totalDays = registration.getTerm().getTermDays().size();
        if (totalDays == 0) {
            return 100.0;
        }

        long presentDays = attendanceRepository.findByRegistrationId(registrationId).stream()
                .filter(Attendance::isPresent)
                .count();

        return Math.round(((double) presentDays / totalDays) * 1000.0) / 10.0;
    }

    @Override
    @Transactional(readOnly = true)
    public int getStudentAttendedDaysCount(Long registrationId) {
        return (int) attendanceRepository.findByRegistrationId(registrationId).stream()
                .filter(Attendance::isPresent)
                .count();
    }
}
