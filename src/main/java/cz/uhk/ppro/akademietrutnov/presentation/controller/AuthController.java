package cz.uhk.ppro.akademietrutnov.presentation.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/courses";
        }

        if (error != null) {
            model.addAttribute("errorMessage", "Neplatné uživatelské jméno nebo heslo.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Byl(a) jste úspěšně odhlášen(a).");
        }

        return "auth/login";
    }
}
