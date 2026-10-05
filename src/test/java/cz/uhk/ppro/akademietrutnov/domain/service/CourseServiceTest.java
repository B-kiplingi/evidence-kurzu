package cz.uhk.ppro.akademietrutnov.domain.service;

import cz.uhk.ppro.akademietrutnov.domain.exception.EntityNotFoundException;
import cz.uhk.ppro.akademietrutnov.domain.model.Course;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.CourseRepository;
import cz.uhk.ppro.akademietrutnov.presentation.dto.CourseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Course sampleCourse;

    @BeforeEach
    void setUp() {
        sampleCourse = new Course("Java Core", "Základy Javy", 40, new BigDecimal("12000.00"), true);
        sampleCourse.setId(1L);
    }

    @Test
    @DisplayName("findAll() vrátí seznam všech kurzů ze repozitáře")
    void shouldFindAllCourses() {
        when(courseRepository.findAllByOrderByNameAsc()).thenReturn(List.of(sampleCourse));

        List<Course> courses = courseService.findAll();

        assertThat(courses).hasSize(1);
        assertThat(courses.get(0).getName()).isEqualTo("Java Core");
        verify(courseRepository).findAllByOrderByNameAsc();
    }

    @Test
    @DisplayName("findById() vrátí kurz, pokud existuje")
    void shouldFindCourseByIdWhenExists() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));

        Course course = courseService.findById(1L);

        assertThat(course).isNotNull();
        assertThat(course.getId()).isEqualTo(1L);
        assertThat(course.getName()).isEqualTo("Java Core");
    }

    @Test
    @DisplayName("findById() vyhodí EntityNotFoundException pro neexistující ID")
    void shouldThrowWhenCourseNotFound() {
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.findById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("create() uloží a vrátí nový kurz z DTO")
    void shouldCreateNewCourse() {
        CourseDto dto = new CourseDto(null, "Spring Boot", "Anotace", 50, new BigDecimal("15000.00"), true);
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course c = invocation.getArgument(0);
            c.setId(2L);
            return c;
        });

        Course created = courseService.create(dto);

        assertThat(created.getId()).isEqualTo(2L);
        assertThat(created.getName()).isEqualTo("Spring Boot");
        assertThat(created.getPrice()).isEqualByComparingTo("15000.00");
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("update() upraví existující kurz a uloží změny")
    void shouldUpdateExistingCourse() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(sampleCourse);

        CourseDto updateDto = new CourseDto(1L, "Java Advanced", "Pokročilá Java", 60, new BigDecimal("18000.00"), false);
        Course updated = courseService.update(1L, updateDto);

        assertThat(updated.getName()).isEqualTo("Java Advanced");
        assertThat(updated.getHoursDuration()).isEqualTo(60);
        assertThat(updated.isAccredited()).isFalse();
        verify(courseRepository).save(sampleCourse);
    }

    @Test
    @DisplayName("delete() smaže kurz z repozitáře")
    void shouldDeleteCourse() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse));

        courseService.delete(1L);

        verify(courseRepository).delete(sampleCourse);
    }
}
