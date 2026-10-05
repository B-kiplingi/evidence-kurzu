package cz.uhk.ppro.akademietrutnov.domain.service;

import cz.uhk.ppro.akademietrutnov.domain.model.Course;
import cz.uhk.ppro.akademietrutnov.presentation.dto.CourseDto;

import java.util.List;

public interface CourseService {
    List<Course> findAll();
    List<Course> searchByName(String query);
    Course findById(Long id);
    Course create(CourseDto dto);
    Course update(Long id, CourseDto dto);
    void delete(Long id);
    CourseDto toDto(Course course);
}
