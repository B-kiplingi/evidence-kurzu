package cz.uhk.ppro.akademietrutnov.domain.service;

import cz.uhk.ppro.akademietrutnov.domain.model.Registration;

import java.util.List;
import java.util.Optional;

public interface RegistrationService {

    Registration registerStudent(Long termId, Long studentId, String exceptionReason, boolean isAdmin);

    Registration cancelRegistration(Long registrationId, String cancellationReason, boolean isAdmin, Long currentUserId);

    Registration promoteFirstWaitlisted(Long termId);

    Registration findById(Long id);

    List<Registration> findByTermId(Long termId);

    List<Registration> findConfirmedByTermId(Long termId);

    List<Registration> findWaitlistedByTermId(Long termId);

    Optional<Registration> findNextWaitlistCandidate(Long termId);

    List<Registration> findByStudentId(Long studentId);
}
