package cz.uhk.ppro.akademietrutnov.domain.service;

import cz.uhk.ppro.akademietrutnov.domain.exception.EntityNotFoundException;
import cz.uhk.ppro.akademietrutnov.domain.model.Course;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.CourseRepository;
import cz.uhk.ppro.akademietrutnov.presentation.dto.CourseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> findAll() {
        return courseRepository.findAllByOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> searchByName(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAll();
        }
        return courseRepository.findByNameContainingIgnoreCaseOrderByNameAsc(query.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public Course findById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Kurz", id));
    }

    @Override
    public Course create(CourseDto dto) {
        Course course = new Course(
                dto.getName(),
                dto.getAnnotation(),
                dto.getHoursDuration(),
                dto.getPrice(),
                dto.isAccredited()
        );
        return courseRepository.save(course);
    }

    @Override
    public Course update(Long id, CourseDto dto) {
        Course course = findById(id);
        course.setName(dto.getName());
        course.setAnnotation(dto.getAnnotation());
        course.setHoursDuration(dto.getHoursDuration());
        course.setPrice(dto.getPrice());
        course.setAccredited(dto.isAccredited());
        return courseRepository.save(course);
    }

    @Override
    public void delete(Long id) {
        Course course = findById(id);
        courseRepository.delete(course);
    }

    @Override
    public CourseDto toDto(Course course) {
        return new CourseDto(
                course.getId(),
                course.getName(),
                course.getAnnotation(),
                course.getHoursDuration(),
                course.getPrice(),
                course.isAccredited()
        );
    }
}
