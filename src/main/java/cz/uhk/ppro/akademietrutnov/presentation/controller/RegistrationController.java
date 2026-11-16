package cz.uhk.ppro.akademietrutnov.presentation.controller;

import cz.uhk.ppro.akademietrutnov.domain.model.Registration;
import cz.uhk.ppro.akademietrutnov.domain.model.RegistrationStatus;
import cz.uhk.ppro.akademietrutnov.domain.model.User;
import cz.uhk.ppro.akademietrutnov.domain.service.RegistrationService;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.UserRepository;
import cz.uhk.ppro.akademietrutnov.presentation.dto.RegistrationRequestDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class RegistrationController {

    private final RegistrationService registrationService;
    private final UserRepository userRepository;

    public RegistrationController(RegistrationService registrationService, UserRepository userRepository) {
        this.registrationService = registrationService;
        this.userRepository = userRepository;
    }

    @PostMapping("/terms/{termId}/register")
    @PreAuthorize("isAuthenticated()")
    public String registerForTerm(
            @PathVariable("termId") Long termId,
            @ModelAttribute("regRequest") RegistrationRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Uživatel nenalezen."));

        boolean isAdmin = currentUser.isAdmin();

        Long targetStudentId;
        if (isAdmin && requestDto.getStudentId() != null) {
            targetStudentId = requestDto.getStudentId();
        } else {
            targetStudentId = currentUser.getId();
        }

        Registration reg = registrationService.registerStudent(
                termId, targetStudentId, requestDto.getExceptionReason(), isAdmin);

        if (reg.getStatus() == RegistrationStatus.CONFIRMED) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Přihláška byla úspěšně vytvořena a účast byla potvrzena.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Kapacita termínu je naplněna. Přihláška byla zařazena do pořadníku náhradníků na " +
                            reg.getQueuePosition() + ". místo.");
        }

        return "redirect:/terms/" + termId;
    }

    @PostMapping("/registrations/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public String cancelRegistration(
            @PathVariable("id") Long id,
            @RequestParam(value = "reason", required = false) String reason,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Uživatel nenalezen."));

        Registration cancelled = registrationService.cancelRegistration(
                id, reason, currentUser.isAdmin(), currentUser.getId());

        redirectAttributes.addFlashAttribute("successMessage",
                "Přihláška studenta " + cancelled.getStudent().getFullName() + " byla zrušena (záznam zůstává archivován).");

        return "redirect:/terms/" + cancelled.getTerm().getId();
    }

    @GetMapping("/my-registrations")
    @PreAuthorize("isAuthenticated()")
    public String viewMyRegistrations(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Uživatel nenalezen."));

        List<Registration> registrations = registrationService.findByStudentId(currentUser.getId());
        model.addAttribute("registrations", registrations);
        model.addAttribute("currentUser", currentUser);

        return "registrations/my-list";
    }
}
