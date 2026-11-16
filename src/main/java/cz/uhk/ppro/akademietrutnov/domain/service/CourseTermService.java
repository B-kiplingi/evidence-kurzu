package cz.uhk.ppro.akademietrutnov.domain.service;

import cz.uhk.ppro.akademietrutnov.domain.model.CourseTerm;
import cz.uhk.ppro.akademietrutnov.domain.model.TermDay;
import cz.uhk.ppro.akademietrutnov.domain.model.TermStatus;
import cz.uhk.ppro.akademietrutnov.presentation.dto.CourseTermDto;
import cz.uhk.ppro.akademietrutnov.presentation.dto.TermDayDto;

import java.util.List;

public interface CourseTermService {

    List<CourseTerm> findAll();

    List<CourseTerm> findByCourseId(Long courseId);

    List<CourseTerm> findByStatus(TermStatus status);

    List<CourseTerm> findByLecturerId(Long lecturerId);

    CourseTerm findById(Long id);

    CourseTerm create(CourseTermDto dto);

    CourseTerm update(Long id, CourseTermDto dto);

    void delete(Long id);

    CourseTermDto toDto(CourseTerm term);

    TermDay addDay(Long termId, TermDayDto dayDto);

    void removeDay(Long dayId);
}
