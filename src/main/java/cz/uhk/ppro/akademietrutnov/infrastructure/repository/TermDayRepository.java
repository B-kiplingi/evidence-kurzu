package cz.uhk.ppro.akademietrutnov.infrastructure.repository;

import cz.uhk.ppro.akademietrutnov.domain.model.TermDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TermDayRepository extends JpaRepository<TermDay, Long> {

    List<TermDay> findByTermIdOrderByDayDateAscStartTimeAsc(Long termId);

    List<TermDay> findByLecturerId(Long lecturerId);

    @Query("SELECT COALESCE(SUM(td.hoursCount), 0) FROM TermDay td WHERE td.lecturer.id = :lecturerId")
    int sumHoursByLecturerId(@Param("lecturerId") Long lecturerId);
}
