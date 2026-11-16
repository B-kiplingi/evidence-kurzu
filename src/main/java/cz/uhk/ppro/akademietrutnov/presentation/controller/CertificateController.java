package cz.uhk.ppro.akademietrutnov.presentation.controller;

import cz.uhk.ppro.akademietrutnov.domain.model.Certificate;
import cz.uhk.ppro.akademietrutnov.domain.model.User;
import cz.uhk.ppro.akademietrutnov.domain.service.CertificateService;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class CertificateController {

    private final CertificateService certificateService;
    private final UserRepository userRepository;

    public CertificateController(CertificateService certificateService, UserRepository userRepository) {
        this.certificateService = certificateService;
        this.userRepository = userRepository;
    }

    @PostMapping("/registrations/{id}/issue-certificate")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public String issueCertificate(
            @PathVariable("id") Long registrationId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Uživatel nenalezen."));

        Certificate cert = certificateService.issueCertificate(
                registrationId, currentUser.getId(), currentUser.isAdmin());

        redirectAttributes.addFlashAttribute("successMessage",
                "Osvědčení č. " + cert.getCertificateNumber() + " pro absolventa " +
                        cert.getStudentName() + " bylo úspěšně vystaveno.");

        return "redirect:/certificates/" + cert.getId();
    }

    @PostMapping("/registrations/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public String rejectCompletion(
            @PathVariable("id") Long registrationId,
            @RequestParam(value = "reason", required = false) String reason,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Uživatel nenalezen."));

        certificateService.rejectCompletion(registrationId, reason, currentUser.getId(), currentUser.isAdmin());

        redirectAttributes.addFlashAttribute("successMessage", "Registrace byla označena jako nedokončená.");
        return "redirect:/terms";
    }

    @GetMapping("/certificates")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public String listCertificates(Model model) {
        List<Certificate> certificates = certificateService.findAll();
        model.addAttribute("certificates", certificates);
        return "certificates/list";
    }

    @GetMapping("/certificates/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    public String viewCertificate(@PathVariable("id") Long id, Model model) {
        Certificate certificate = certificateService.findById(id);
        model.addAttribute("certificate", certificate);
        return "certificates/detail";
    }

    @GetMapping("/certificates/verify/{number}")
    public String verifyCertificate(@PathVariable("number") String number, Model model) {
        Certificate certificate = certificateService.findByNumber(number);
        model.addAttribute("certificate", certificate);
        model.addAttribute("isVerified", true);
        return "certificates/verify";
    }
}
