package cz.uhk.ppro.akademietrutnov.infrastructure.repository;

import cz.uhk.ppro.akademietrutnov.domain.model.Registration;
import cz.uhk.ppro.akademietrutnov.domain.model.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    List<Registration> findByTermIdOrderByCreatedAtAsc(Long termId);

    List<Registration> findByTermIdAndStatusOrderByCreatedAtAsc(Long termId, RegistrationStatus status);

    List<Registration> findByTermIdAndStatusOrderByQueuePositionAsc(Long termId, RegistrationStatus status);

    Optional<Registration> findFirstByTermIdAndStatusOrderByQueuePositionAsc(Long termId, RegistrationStatus status);

    List<Registration> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    Optional<Registration> findByTermIdAndStudentId(Long termId, Long studentId);

    long countByTermIdAndStatus(Long termId, RegistrationStatus status);

    @Query("SELECT r FROM Registration r WHERE r.term.id = :termId AND r.student.id = :studentId AND r.status != 'CANCELLED'")
    Optional<Registration> findActiveByTermIdAndStudentId(@Param("termId") Long termId, @Param("studentId") Long studentId);

    @Query("SELECT r FROM Registration r WHERE r.student.id = :studentId AND r.term.course.id = :courseId AND (r.status = 'CONFIRMED' OR r.status = 'COMPLETED')")
    List<Registration> findConfirmedOrCompletedByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Query("SELECT r FROM Registration r WHERE r.term.id = :termId AND r.status = 'WAITLISTED' AND r.queuePosition > :position ORDER BY r.queuePosition ASC")
    List<Registration> findWaitlistedAfterPosition(@Param("termId") Long termId, @Param("position") int position);

    @Query("SELECT r.student.id, r.student.fullName, r.student.email, r.student.phone, COUNT(DISTINCT r.term.course.id) " +
           "FROM Registration r " +
           "WHERE r.status != 'CANCELLED' AND EXTRACT(YEAR FROM r.createdAt) = :year " +
           "GROUP BY r.student.id, r.student.fullName, r.student.email, r.student.phone " +
           "HAVING COUNT(DISTINCT r.term.course.id) > 1 " +
           "ORDER BY COUNT(DISTINCT r.term.course.id) DESC")
    List<Object[]> findStudentsWithMultipleCoursesInYear(@Param("year") int year);
}
