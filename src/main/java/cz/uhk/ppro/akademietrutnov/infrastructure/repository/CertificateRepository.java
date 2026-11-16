package cz.uhk.ppro.akademietrutnov.infrastructure.repository;

import cz.uhk.ppro.akademietrutnov.domain.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    Optional<Certificate> findByCertificateNumber(String certificateNumber);

    Optional<Certificate> findByRegistrationId(Long registrationId);

    List<Certificate> findAllByOrderByCreatedAtDesc();

    @Query("SELECT c FROM Certificate c WHERE c.registration.student.id = :studentId ORDER BY c.issueDate DESC")
    List<Certificate> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(c) FROM Certificate c WHERE EXTRACT(YEAR FROM c.issueDate) = :year")
    long countByYear(@Param("year") int year);
}
