package cz.uhk.ppro.akademietrutnov.infrastructure.repository;

import cz.uhk.ppro.akademietrutnov.domain.model.CourseTerm;
import cz.uhk.ppro.akademietrutnov.domain.model.TermStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseTermRepository extends JpaRepository<CourseTerm, Long> {

    List<CourseTerm> findAllByOrderByStartDateDesc();

    List<CourseTerm> findByCourseIdOrderByStartDateAsc(Long courseId);

    List<CourseTerm> findByStatusOrderByStartDateDesc(TermStatus status);

    @Query("SELECT t FROM CourseTerm t JOIN t.lecturers l WHERE l.id = :lecturerId ORDER BY t.startDate DESC")
    List<CourseTerm> findByLecturerId(@Param("lecturerId") Long lecturerId);
}
