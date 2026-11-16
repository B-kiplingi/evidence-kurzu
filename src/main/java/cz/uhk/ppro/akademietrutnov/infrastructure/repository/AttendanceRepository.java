package cz.uhk.ppro.akademietrutnov.infrastructure.repository;

import cz.uhk.ppro.akademietrutnov.domain.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByTermDayId(Long termDayId);

    List<Attendance> findByRegistrationId(Long registrationId);

    Optional<Attendance> findByTermDayIdAndRegistrationId(Long termDayId, Long registrationId);

    List<Attendance> findByRegistration_Term_Id(Long termId);
}
