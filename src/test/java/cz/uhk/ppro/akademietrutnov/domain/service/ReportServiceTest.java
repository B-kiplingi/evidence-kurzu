package cz.uhk.ppro.akademietrutnov.domain.service;

import cz.uhk.ppro.akademietrutnov.domain.model.*;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.CourseTermRepository;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.RegistrationRepository;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.TermDayRepository;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.UserRepository;
import cz.uhk.ppro.akademietrutnov.presentation.dto.LecturerHoursReportDto;
import cz.uhk.ppro.akademietrutnov.presentation.dto.MultiCourseStudentReportDto;
import cz.uhk.ppro.akademietrutnov.presentation.dto.TermOccupancyReportDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private CourseTermRepository courseTermRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private TermDayRepository termDayRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    @DisplayName("Přehled naplněnosti termínů správně počítá počty a procenta")
    void shouldGenerateOccupancyReport() {
        Course course = new Course("Java", "Anotace", 40, new BigDecimal("10000.00"), true);
        course.setId(1L);

        CourseTerm term = new CourseTerm(course, LocalDate.now(), LocalDate.now().plusDays(10), "Učebna A1", 4);
        term.setId(10L);

        User s1 = new User("s1", "p", "S1", "s1@ex.cz");
        User s2 = new User("s2", "p", "S2", "s2@ex.cz");
        User s3 = new User("s3", "p", "S3", "s3@ex.cz");

        Registration r1 = new Registration(term, s1, RegistrationStatus.CONFIRMED);
        Registration r2 = new Registration(term, s2, RegistrationStatus.CONFIRMED);
        Registration r3 = new Registration(term, s3, RegistrationStatus.WAITLISTED, 1, null);

        term.setRegistrations(List.of(r1, r2, r3));

        when(courseTermRepository.findAllByOrderByStartDateDesc()).thenReturn(List.of(term));

        List<TermOccupancyReportDto> report = reportService.getTermOccupancyReport();

        assertThat(report).hasSize(1);
        TermOccupancyReportDto item = report.get(0);
        assertThat(item.getConfirmedCount()).isEqualTo(2);
        assertThat(item.getWaitlistedCount()).isEqualTo(1);
        assertThat(item.getCapacity()).isEqualTo(4);
        assertThat(item.getOccupancyPercentage()).isEqualTo(50);
    }

    @Test
    @DisplayName("ADR-05 Vykázané hodiny lektorů: počítá součet hodin ze skutečně vyučovaných dnů")
    void shouldCalculateLecturerHours() {
        User lecturer = new User("lektor", "p", "Ing. Karel Lektor", "karel@ex.cz", "+420 777 987 654");
        lecturer.setId(5L);
        Role roleLec = new Role("ROLE_LECTURER");
        lecturer.addRole(roleLec);

        when(userRepository.findAll()).thenReturn(List.of(lecturer));
        when(termDayRepository.sumHoursByLecturerId(5L)).thenReturn(28);
        when(courseTermRepository.findByLecturerId(5L)).thenReturn(List.of(new CourseTerm()));

        List<LecturerHoursReportDto> report = reportService.getLecturerHoursReport();

        assertThat(report).hasSize(1);
        LecturerHoursReportDto dto = report.get(0);
        assertThat(dto.getFullName()).isEqualTo("Ing. Karel Lektor");
        assertThat(dto.getTotalHoursTaught()).isEqualTo(28);
        assertThat(dto.getTermsTaughtCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Report studentů s více kurzy v roce vrátí studenty přihlášené na >1 kurz")
    void shouldFindMultiCourseStudents() {
        Object[] row = new Object[]{10L, "Eva Černá", "eva@ex.cz", "+420 602 333 444", 2L};

        Course c1 = new Course("Java", "A", 40, BigDecimal.TEN, true);
        c1.setId(1L);
        Course c2 = new Course("Účetnictví", "B", 40, BigDecimal.TEN, true);
        c2.setId(2L);

        CourseTerm t1 = new CourseTerm(c1, LocalDate.now(), LocalDate.now(), "A", 5);
        CourseTerm t2 = new CourseTerm(c2, LocalDate.now(), LocalDate.now(), "B", 5);

        User student = new User("eva", "p", "Eva Černá", "eva@ex.cz");
        student.setId(10L);

        Registration r1 = new Registration(t1, student, RegistrationStatus.CONFIRMED);
        Registration r2 = new Registration(t2, student, RegistrationStatus.CONFIRMED);

        when(registrationRepository.findStudentsWithMultipleCoursesInYear(2026))
                .thenReturn(java.util.Collections.singletonList(row));
        when(registrationRepository.findByStudentIdOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(r1, r2));

        List<MultiCourseStudentReportDto> report = reportService.getMultiCourseStudentsReport(2026);

        assertThat(report).hasSize(1);
        MultiCourseStudentReportDto dto = report.get(0);
        assertThat(dto.getFullName()).isEqualTo("Eva Černá");
        assertThat(dto.getDistinctCoursesCount()).isEqualTo(2L);
        assertThat(dto.getCourseNames()).containsExactlyInAnyOrder("Java", "Účetnictví");
    }
}
