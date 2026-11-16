package cz.uhk.ppro.akademietrutnov.domain.service;

import cz.uhk.ppro.akademietrutnov.domain.model.*;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.AttendanceRepository;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.RegistrationRepository;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.TermDayRepository;
import cz.uhk.ppro.akademietrutnov.presentation.dto.AttendanceFormDto;
import cz.uhk.ppro.akademietrutnov.presentation.dto.AttendanceRecordDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private TermDayRepository termDayRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private CourseTerm term;
    private Registration registration;
    private TermDay day1;
    private TermDay day2;
    private TermDay day3;
    private TermDay day4;

    @BeforeEach
    void setUp() {
        term = new CourseTerm(null, LocalDate.now(), LocalDate.now().plusDays(10), "Učebna A", 5);

        day1 = new TermDay(term, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(16, 0), 7, null, "D1");
        day1.setId(1L);
        day2 = new TermDay(term, LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(16, 0), 7, null, "D2");
        day2.setId(2L);
        day3 = new TermDay(term, LocalDate.now().plusDays(2), LocalTime.of(9, 0), LocalTime.of(16, 0), 7, null, "D3");
        day3.setId(3L);
        day4 = new TermDay(term, LocalDate.now().plusDays(3), LocalTime.of(9, 0), LocalTime.of(16, 0), 7, null, "D4");
        day4.setId(4L);

        term.addTermDay(day1);
        term.addTermDay(day2);
        term.addTermDay(day3);
        term.addTermDay(day4);

        User student = new User("st", "p", "Student", "s@ex.cz");
        student.setId(10L);

        registration = new Registration(term, student, RegistrationStatus.CONFIRMED);
        registration.setId(100L);
    }

    @Test
    @DisplayName("Správně spočítá procento docházky (3 ze 4 dnů = 75.0 %)")
    void shouldCalculateAttendancePercentageAccurately() {
        Attendance a1 = new Attendance(day1, registration, true, null);
        Attendance a2 = new Attendance(day2, registration, true, null);
        Attendance a3 = new Attendance(day3, registration, true, null);
        Attendance a4 = new Attendance(day4, registration, false, "Omluven");

        when(registrationRepository.findById(100L)).thenReturn(Optional.of(registration));
        when(attendanceRepository.findByRegistrationId(100L)).thenReturn(List.of(a1, a2, a3, a4));

        double percentage = attendanceService.getStudentAttendancePercentage(100L);
        int attendedCount = attendanceService.getStudentAttendedDaysCount(100L);

        assertThat(percentage).isEqualTo(75.0);
        assertThat(attendedCount).isEqualTo(3);
    }

    @Test
    @DisplayName("Uložení docházky vytvoří nové záznamy pro dosud nezapsané dny")
    void shouldSaveNewAttendanceRecords() {
        AttendanceRecordDto rec = new AttendanceRecordDto(100L, 1L, "Student", true, "Poznámka");
        AttendanceFormDto form = new AttendanceFormDto(term.getId(), 1L, List.of(rec));

        when(termDayRepository.findById(1L)).thenReturn(Optional.of(day1));
        when(registrationRepository.findById(100L)).thenReturn(Optional.of(registration));
        when(attendanceRepository.findByTermDayIdAndRegistrationId(1L, 100L)).thenReturn(Optional.empty());

        attendanceService.saveAttendance(form);

        verify(attendanceRepository).save(any(Attendance.class));
    }
}
