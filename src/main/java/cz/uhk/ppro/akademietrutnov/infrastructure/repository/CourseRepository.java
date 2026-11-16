package cz.uhk.ppro.akademietrutnov.infrastructure.repository;

import cz.uhk.ppro.akademietrutnov.domain.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findAllByOrderByNameAsc();
    List<Course> findByNameContainingIgnoreCaseOrderByNameAsc(String query);
    Optional<Course> findByName(String name);
}
