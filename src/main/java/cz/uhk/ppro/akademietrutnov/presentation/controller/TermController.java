package cz.uhk.ppro.akademietrutnov.presentation.controller;

import cz.uhk.ppro.akademietrutnov.domain.model.Course;
import cz.uhk.ppro.akademietrutnov.domain.model.CourseTerm;
import cz.uhk.ppro.akademietrutnov.domain.model.Registration;
import cz.uhk.ppro.akademietrutnov.domain.model.TermStatus;
import cz.uhk.ppro.akademietrutnov.domain.model.User;
import cz.uhk.ppro.akademietrutnov.domain.service.CourseService;
import cz.uhk.ppro.akademietrutnov.domain.service.CourseTermService;
import cz.uhk.ppro.akademietrutnov.domain.service.RegistrationService;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.UserRepository;
import cz.uhk.ppro.akademietrutnov.presentation.dto.CourseTermDto;
import cz.uhk.ppro.akademietrutnov.presentation.dto.RegistrationRequestDto;
import cz.uhk.ppro.akademietrutnov.presentation.dto.TermDayDto;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/terms")
public class TermController {

    private final CourseTermService courseTermService;
    private final CourseService courseService;
    private final RegistrationService registrationService;
    private final UserRepository userRepository;

    public TermController(CourseTermService courseTermService,
                          CourseService courseService,
                          RegistrationService registrationService,
                          UserRepository userRepository) {
        this.courseTermService = courseTermService;
        this.courseService = courseService;
        this.registrationService = registrationService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listTerms(
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "status", required = false) TermStatus status,
            Model model) {

        List<CourseTerm> terms;
        if (courseId != null) {
            terms = courseTermService.findByCourseId(courseId);
        } else if (status != null) {
            terms = courseTermService.findByStatus(status);
        } else {
            terms = courseTermService.findAll();
        }

        model.addAttribute("terms", terms);
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("statuses", TermStatus.values());
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("selectedStatus", status);

        return "terms/list";
    }

    @GetMapping("/{id}")
    public String viewTerm(@PathVariable("id") Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        CourseTerm term = courseTermService.findById(id);
        List<Registration> confirmed = registrationService.findConfirmedByTermId(id);
        List<Registration> waitlist = registrationService.findWaitlistedByTermId(id);
        Optional<Registration> nextCandidate = registrationService.findNextWaitlistCandidate(id);

        User currentUser = null;
        Registration myRegistration = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (currentUser != null) {
                final Long uid = currentUser.getId();
                myRegistration = term.getRegistrations().stream()
                        .filter(r -> r.getStudent().getId().equals(uid) && !r.isCancelled())
                        .findFirst().orElse(null);
            }
        }

        model.addAttribute("term", term);
        model.addAttribute("confirmedRegistrations", confirmed);
        model.addAttribute("waitlist", waitlist);
        model.addAttribute("nextCandidate", nextCandidate.orElse(null));
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("myRegistration", myRegistration);
        model.addAttribute("newDay", new TermDayDto());
        model.addAttribute("regRequest", new RegistrationRequestDto());
        model.addAttribute("allStudents", userRepository.findAll().stream().filter(User::isStudent).toList());

        return "terms/detail";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateForm(@RequestParam(value = "courseId", required = false) Long courseId, Model model) {
        CourseTermDto dto = new CourseTermDto();
        if (courseId != null) {
            dto.setCourseId(courseId);
        }
        model.addAttribute("termDto", dto);
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("lecturers", userRepository.findAll().stream().filter(User::isLecturer).toList());
        model.addAttribute("isEdit", false);
        return "terms/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createTerm(
            @Valid @ModelAttribute("termDto") CourseTermDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("courses", courseService.findAll());
            model.addAttribute("lecturers", userRepository.findAll().stream().filter(User::isLecturer).toList());
            model.addAttribute("isEdit", false);
            return "terms/form";
        }

        CourseTerm created = courseTermService.create(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Termín kurzu byl úspěšně vytvořen.");
        return "redirect:/terms/" + created.getId();
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        CourseTerm term = courseTermService.findById(id);
        model.addAttribute("termDto", courseTermService.toDto(term));
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("lecturers", userRepository.findAll().stream().filter(User::isLecturer).toList());
        model.addAttribute("isEdit", true);
        model.addAttribute("termId", id);
        return "terms/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateTerm(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("termDto") CourseTermDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("courses", courseService.findAll());
            model.addAttribute("lecturers", userRepository.findAll().stream().filter(User::isLecturer).toList());
            model.addAttribute("isEdit", true);
            model.addAttribute("termId", id);
            return "terms/form";
        }

        CourseTerm updated = courseTermService.update(id, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Termín byl úspěšně upraven.");
        return "redirect:/terms/" + updated.getId();
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteTerm(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        courseTermService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Termín byl úspěšně smazán.");
        return "redirect:/terms";
    }

    @PostMapping("/{id}/promote-next")
    @PreAuthorize("hasRole('ADMIN')")
    public String promoteNextWaitlisted(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Registration promoted = registrationService.promoteFirstWaitlisted(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "Náhradník " + promoted.getStudent().getFullName() + " byl úspěšně povýšen na řádného účastníka.");
        return "redirect:/terms/" + id;
    }

    @PostMapping("/{id}/days")
    @PreAuthorize("hasRole('ADMIN')")
    public String addDayToTerm(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("newDay") TermDayDto dayDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chyba ve formuláři dne rozvrhu. Zkontrolujte zadané hodnoty.");
            return "redirect:/terms/" + id;
        }

        courseTermService.addDay(id, dayDto);
        redirectAttributes.addFlashAttribute("successMessage", "Den výuky byl úspěšně přidán do rozvrhu.");
        return "redirect:/terms/" + id;
    }

    @PostMapping("/{id}/days/{dayId}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String removeDayFromTerm(
            @PathVariable("id") Long id,
            @PathVariable("dayId") Long dayId,
            RedirectAttributes redirectAttributes) {

        courseTermService.removeDay(dayId);
        redirectAttributes.addFlashAttribute("successMessage", "Den výuky byl odebrán z rozvrhu.");
        return "redirect:/terms/" + id;
    }
}
