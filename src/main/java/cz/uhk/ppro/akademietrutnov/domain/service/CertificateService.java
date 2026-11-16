package cz.uhk.ppro.akademietrutnov.domain.service;

import cz.uhk.ppro.akademietrutnov.domain.model.Certificate;

import java.util.List;

public interface CertificateService {

    Certificate issueCertificate(Long registrationId, Long currentUserId, boolean isAdmin);

    void rejectCompletion(Long registrationId, String reason, Long currentUserId, boolean isAdmin);

    Certificate findById(Long id);

    Certificate findByNumber(String certificateNumber);

    List<Certificate> findAll();

    List<Certificate> findByStudentId(Long studentId);
}
