package cz.uhk.ppro.akademietrutnov.presentation.controller;

import cz.uhk.ppro.akademietrutnov.domain.model.Course;
import cz.uhk.ppro.akademietrutnov.domain.service.CourseService;
import cz.uhk.ppro.akademietrutnov.domain.service.CourseTermService;
import cz.uhk.ppro.akademietrutnov.presentation.dto.CourseDto;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;
    private final CourseTermService courseTermService;

    public CourseController(CourseService courseService, CourseTermService courseTermService) {
        this.courseService = courseService;
        this.courseTermService = courseTermService;
    }

    @GetMapping
    public String listCourses(@RequestParam(value = "q", required = false) String query, Model model) {
        List<Course> courses = courseService.searchByName(query);
        model.addAttribute("courses", courses);
        model.addAttribute("query", query != null ? query : "");
        return "courses/list";
    }

    @GetMapping("/{id}")
    public String viewCourse(@PathVariable("id") Long id, Model model) {
        Course course = courseService.findById(id);
        model.addAttribute("course", course);
        model.addAttribute("terms", courseTermService.findByCourseId(id));
        return "courses/detail";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("course")) {
            model.addAttribute("course", new CourseDto());
        }
        model.addAttribute("isEdit", false);
        return "courses/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createCourse(
            @Valid @ModelAttribute("course") CourseDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "courses/form";
        }

        Course created = courseService.create(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Kurz '" + created.getName() + "' byl úspěšně vytvořen.");
        return "redirect:/courses/" + created.getId();
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Course course = courseService.findById(id);
        model.addAttribute("course", courseService.toDto(course));
        model.addAttribute("isEdit", true);
        model.addAttribute("courseId", id);
        return "courses/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateCourse(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("course") CourseDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("courseId", id);
            return "courses/form";
        }

        Course updated = courseService.update(id, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Kurz '" + updated.getName() + "' byl úspěšně upraven.");
        return "redirect:/courses/" + updated.getId();
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteCourse(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Course course = courseService.findById(id);
        String name = course.getName();
        courseService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Kurz '" + name + "' byl úspěšně odstraněn.");
        return "redirect:/courses";
    }
}
