package cz.uhk.ppro.akademietrutnov.domain.service;

import cz.uhk.ppro.akademietrutnov.domain.exception.BusinessRuleException;
import cz.uhk.ppro.akademietrutnov.domain.model.*;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.CourseTermRepository;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.RegistrationRepository;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private CourseTermRepository courseTermRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    private Course course;
    private CourseTerm term;
    private User student1;
    private User student2;

    @BeforeEach
    void setUp() {
        course = new Course("Java Core", "Anotace", 40, new BigDecimal("10000.00"), true);
        course.setId(1L);

        term = new CourseTerm(course, LocalDate.now().plusDays(10), LocalDate.now().plusDays(20), "Učebna A1", 2);
        term.setId(10L);

        student1 = new User("student1", "pass", "Jan Novák", "jan@example.cz", "+420 777 111 222");
        student1.setId(101L);

        student2 = new User("student2", "pass", "Eva Černá", "eva@example.cz", "+420 777 333 444");
        student2.setId(102L);
    }

    @Test
    @DisplayName("Přihlášení na termín s volnou kapacitou zařadí studenta jako CONFIRMED")
    void shouldRegisterAsConfirmedWhenSpotsAvailable() {
        when(courseTermRepository.findById(10L)).thenReturn(Optional.of(term));
        when(userRepository.findById(101L)).thenReturn(Optional.of(student1));
        when(registrationRepository.findActiveByTermIdAndStudentId(10L, 101L)).thenReturn(Optional.empty());
        when(registrationRepository.findConfirmedOrCompletedByStudentAndCourse(101L, 1L)).thenReturn(Collections.emptyList());
        when(registrationRepository.save(any(Registration.class))).thenAnswer(i -> i.getArgument(0));

        Registration result = registrationService.registerStudent(10L, 101L, null, false);

        assertThat(result.getStatus()).isEqualTo(RegistrationStatus.CONFIRMED);
        assertThat(result.getQueuePosition()).isNull();
        assertThat(result.getStudent()).isEqualTo(student1);
        verify(registrationRepository).save(any(Registration.class));
    }

    @Test
    @DisplayName("Nepřekročitelnost kapacity: při plném termínu je student zařazen do pořadníku (WAITLISTED)")
    void shouldWaitlistStudentWhenCapacityIsFull() {
        // Simulujeme 2 již potvrzené studenty (kapacita je 2)
        Registration reg1 = new Registration(term, student1, RegistrationStatus.CONFIRMED);
        User studentOther = new User("other", "p", "Karel", "k@ex.cz");
        studentOther.setId(103L);
        Registration reg2 = new Registration(term, studentOther, RegistrationStatus.CONFIRMED);
        term.setRegistrations(List.of(reg1, reg2));

        when(courseTermRepository.findById(10L)).thenReturn(Optional.of(term));
        when(userRepository.findById(102L)).thenReturn(Optional.of(student2));
        when(registrationRepository.findActiveByTermIdAndStudentId(10L, 102L)).thenReturn(Optional.empty());
        when(registrationRepository.findConfirmedOrCompletedByStudentAndCourse(102L, 1L)).thenReturn(Collections.emptyList());
        when(registrationRepository.countByTermIdAndStatus(10L, RegistrationStatus.WAITLISTED)).thenReturn(1L);
        when(registrationRepository.save(any(Registration.class))).thenAnswer(i -> i.getArgument(0));

        Registration result = registrationService.registerStudent(10L, 102L, null, false);

        assertThat(result.getStatus()).isEqualTo(RegistrationStatus.WAITLISTED);
        assertThat(result.getQueuePosition()).isEqualTo(2); // 1 + 1 = 2
    }

    @Test
    @DisplayName("ADR-03 Souběh termínů: Nelze se přihlásit na dva aktivní řádné termíny téhož kurzu")
    void shouldRejectConcurrentEnrollmentOnSameCourse() {
        CourseTerm term2 = new CourseTerm(course, LocalDate.now().plusDays(30), LocalDate.now().plusDays(40), "Učebna B", 5);
        term2.setId(20L);

        Registration activeRegistration = new Registration(term2, student1, RegistrationStatus.CONFIRMED);

        when(courseTermRepository.findById(10L)).thenReturn(Optional.of(term));
        when(userRepository.findById(101L)).thenReturn(Optional.of(student1));
        when(registrationRepository.findActiveByTermIdAndStudentId(10L, 101L)).thenReturn(Optional.empty());
        when(registrationRepository.findConfirmedOrCompletedByStudentAndCourse(101L, 1L)).thenReturn(List.of(activeRegistration));

        assertThatThrownBy(() -> registrationService.registerStudent(10L, 101L, null, false))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Souběžná účast na více řádných termínech téhož kurzu není povolena");
    }

    @Test
    @DisplayName("ADR-04 Dodatečný zápis po zahájení: Běžný uživatel se nemůže zapsat po startu")
    void shouldForbidStudentRegistrationAfterCourseStart() {
        term.setStartDate(LocalDate.now().minusDays(2)); // termín začal před 2 dny

        when(courseTermRepository.findById(10L)).thenReturn(Optional.of(term));
        when(userRepository.findById(101L)).thenReturn(Optional.of(student1));

        assertThatThrownBy(() -> registrationService.registerStudent(10L, 101L, null, false))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Termín již byl zahájen");
    }

    @Test
    @DisplayName("ADR-04 Dodatečný zápis po zahájení: Administrátor může zapsat s povinným důvodem")
    void shouldAllowAdminRegistrationAfterCourseStartWithReason() {
        term.setStartDate(LocalDate.now().minusDays(2));

        when(courseTermRepository.findById(10L)).thenReturn(Optional.of(term));
        when(userRepository.findById(101L)).thenReturn(Optional.of(student1));
        when(registrationRepository.findActiveByTermIdAndStudentId(10L, 101L)).thenReturn(Optional.empty());
        when(registrationRepository.findConfirmedOrCompletedByStudentAndCourse(101L, 1L)).thenReturn(Collections.emptyList());
        when(registrationRepository.save(any(Registration.class))).thenAnswer(i -> i.getArgument(0));

        Registration result = registrationService.registerStudent(10L, 101L, "Schváleno ředitelkou", true);

        assertThat(result.getStatus()).isEqualTo(RegistrationStatus.CONFIRMED);
        assertThat(result.getExceptionReason()).isEqualTo("Schváleno ředitelkou");
    }

    @Test
    @DisplayName("ADR-02 Posun z pořadníku: promoteFirstWaitlisted povýší náhradníka na uvolněné místo a posune frontu")
    void shouldPromoteFirstWaitlistedAndShiftRemaining() {
        Registration waitlisted1 = new Registration(term, student1, RegistrationStatus.WAITLISTED, 1, null);
        Registration waitlisted2 = new Registration(term, student2, RegistrationStatus.WAITLISTED, 2, null);

        when(courseTermRepository.findById(10L)).thenReturn(Optional.of(term));
        when(registrationRepository.findFirstByTermIdAndStatusOrderByQueuePositionAsc(10L, RegistrationStatus.WAITLISTED))
                .thenReturn(Optional.of(waitlisted1));
        when(registrationRepository.findByTermIdAndStatusOrderByQueuePositionAsc(10L, RegistrationStatus.WAITLISTED))
                .thenReturn(List.of(waitlisted2));
        when(registrationRepository.save(any(Registration.class))).thenAnswer(i -> i.getArgument(0));

        Registration promoted = registrationService.promoteFirstWaitlisted(10L);

        assertThat(promoted.getStatus()).isEqualTo(RegistrationStatus.CONFIRMED);
        assertThat(promoted.getQueuePosition()).isNull();
        assertThat(waitlisted2.getQueuePosition()).isEqualTo(1); // posun na 1. pozici
    }

    @Test
    @DisplayName("Zrušení přihlášky uchová záznam v databázi (soft-delete s důvodem)")
    void shouldSoftCancelRegistration() {
        Registration reg = new Registration(term, student1, RegistrationStatus.CONFIRMED);
        reg.setId(50L);

        when(registrationRepository.findById(50L)).thenReturn(Optional.of(reg));
        when(registrationRepository.save(any(Registration.class))).thenAnswer(i -> i.getArgument(0));

        Registration cancelled = registrationService.cancelRegistration(50L, "Nemoc", false, 101L);

        assertThat(cancelled.getStatus()).isEqualTo(RegistrationStatus.CANCELLED);
        assertThat(cancelled.getCancelledAt()).isNotNull();
        assertThat(cancelled.getCancellationReason()).isEqualTo("Nemoc");
    }
}
