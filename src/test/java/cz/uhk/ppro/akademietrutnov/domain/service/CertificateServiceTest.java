package cz.uhk.ppro.akademietrutnov.domain.service;

import cz.uhk.ppro.akademietrutnov.domain.exception.BusinessRuleException;
import cz.uhk.ppro.akademietrutnov.domain.model.*;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.CertificateRepository;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.RegistrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificateServiceTest {

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private AttendanceService attendanceService;

    @InjectMocks
    private CertificateServiceImpl certificateService;

    private Course course;
    private CourseTerm term;
    private User student;
    private User lecturer;
    private Registration registration;

    @BeforeEach
    void setUp() {
        course = new Course("Účetnictví", "Anotace", 40, new BigDecimal("12000.00"), true);
        course.setId(1L);

        term = new CourseTerm(course, LocalDate.now().minusDays(30), LocalDate.now().minusDays(10), "Učebna B", 5);
        term.setId(10L);

        lecturer = new User("lektor", "p", "Ing. Karel Lektor", "karel@example.cz");
        lecturer.setId(201L);
        term.addLecturer(lecturer);

        // 4 dny výuky
        for (int i = 1; i <= 4; i++) {
            TermDay td = new TermDay(term, LocalDate.now().minusDays(30 - i * 5), LocalTime.of(9, 0), LocalTime.of(17, 0), 8, lecturer, "Téma " + i);
            term.addTermDay(td);
        }

        student = new User("student", "p", "Jan Novák", "jan@example.cz");
        student.setId(101L);

        registration = new Registration(term, student, RegistrationStatus.CONFIRMED);
        registration.setId(50L);
    }

    @Test
    @DisplayName("ADR-01 Dokončení: Vystaví osvědčení, pokud je docházka alespoň 70 %")
    void shouldIssueCertificateWhenAttendanceAbove70Percent() {
        when(registrationRepository.findById(50L)).thenReturn(Optional.of(registration));
        when(certificateRepository.findByRegistrationId(50L)).thenReturn(Optional.empty());
        when(attendanceService.getStudentAttendancePercentage(50L)).thenReturn(75.0);
        when(certificateRepository.countByYear(anyInt())).thenReturn(0L);
        when(certificateRepository.findByCertificateNumber(anyString())).thenReturn(Optional.empty());
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(i -> i.getArgument(0));

        Certificate cert = certificateService.issueCertificate(50L, 201L, false);

        assertThat(cert).isNotNull();
        assertThat(cert.getStudentName()).isEqualTo("Jan Novák");
        assertThat(cert.getCourseName()).isEqualTo("Účetnictví");
        assertThat(cert.getCertificateNumber()).matches("OSV-\\d{4}-\\d{4}");
        assertThat(registration.getStatus()).isEqualTo(RegistrationStatus.COMPLETED);
        verify(certificateRepository).save(any(Certificate.class));
    }

    @Test
    @DisplayName("ADR-01 Dokončení: Odmítne vystavit osvědčení při docházce pod 70 %")
    void shouldRejectCertificateWhenAttendanceBelow70Percent() {
        when(registrationRepository.findById(50L)).thenReturn(Optional.of(registration));
        when(certificateRepository.findByRegistrationId(50L)).thenReturn(Optional.empty());
        when(attendanceService.getStudentAttendancePercentage(50L)).thenReturn(50.0);
        when(attendanceService.getStudentAttendedDaysCount(50L)).thenReturn(2);

        assertThatThrownBy(() -> certificateService.issueCertificate(50L, 201L, false))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Podmínkou pro úspěšné dokončení kurzu je alespoň 70% docházka");

        verify(certificateRepository, never()).save(any(Certificate.class));
    }

    @Test
    @DisplayName("Integrita osvědčení: Již vydané osvědčení nelze znovu vystavit (neměnnost)")
    void shouldRejectIssuingDuplicateCertificate() {
        Certificate existing = new Certificate(registration, "OSV-2026-0001", LocalDate.now(), "Jan", "Účetnictví", 40, true);

        when(registrationRepository.findById(50L)).thenReturn(Optional.of(registration));
        when(certificateRepository.findByRegistrationId(50L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> certificateService.issueCertificate(50L, 201L, false))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Osvědčení pro tuto přihlášku již bylo vystaveno");
    }

    @Test
    @DisplayName("Osvědčení nelze vystavit náhradníkovi v pořadníku")
    void shouldRejectIssuingCertificateForWaitlisted() {
        registration.setStatus(RegistrationStatus.WAITLISTED);
        registration.setQueuePosition(1);

        when(registrationRepository.findById(50L)).thenReturn(Optional.of(registration));

        assertThatThrownBy(() -> certificateService.issueCertificate(50L, 201L, false))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("pořadníku");
    }
}
