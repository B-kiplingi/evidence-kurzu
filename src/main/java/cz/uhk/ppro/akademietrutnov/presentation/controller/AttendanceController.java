package cz.uhk.ppro.akademietrutnov.presentation.controller;

import cz.uhk.ppro.akademietrutnov.domain.model.Attendance;
import cz.uhk.ppro.akademietrutnov.domain.model.CourseTerm;
import cz.uhk.ppro.akademietrutnov.domain.model.Registration;
import cz.uhk.ppro.akademietrutnov.domain.model.TermDay;
import cz.uhk.ppro.akademietrutnov.domain.service.AttendanceService;
import cz.uhk.ppro.akademietrutnov.domain.service.CourseTermService;
import cz.uhk.ppro.akademietrutnov.domain.service.RegistrationService;
import cz.uhk.ppro.akademietrutnov.presentation.dto.AttendanceFormDto;
import cz.uhk.ppro.akademietrutnov.presentation.dto.AttendanceRecordDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/terms/{termId}/attendance")
@PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
public class AttendanceController {

    private final CourseTermService courseTermService;
    private final RegistrationService registrationService;
    private final AttendanceService attendanceService;

    public AttendanceController(CourseTermService courseTermService,
                                RegistrationService registrationService,
                                AttendanceService attendanceService) {
        this.courseTermService = courseTermService;
        this.registrationService = registrationService;
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public String showAttendanceSheet(
            @PathVariable("termId") Long termId,
            @RequestParam(value = "dayId", required = false) Long dayId,
            Model model) {

        CourseTerm term = courseTermService.findById(termId);
        List<TermDay> days = term.getTermDays();
        List<Registration> confirmed = registrationService.findConfirmedByTermId(termId);

        TermDay selectedDay = null;
        if (dayId != null) {
            selectedDay = days.stream().filter(d -> d.getId().equals(dayId)).findFirst().orElse(null);
        }
        if (selectedDay == null && !days.isEmpty()) {
            selectedDay = days.get(0);
        }

        AttendanceFormDto formDto = new AttendanceFormDto();
        formDto.setTermId(termId);

        if (selectedDay != null) {
            formDto.setTermDayId(selectedDay.getId());
            List<Attendance> currentAttendances = attendanceService.getAttendanceForDay(selectedDay.getId());
            Map<Long, Attendance> attMap = new HashMap<>();
            for (Attendance a : currentAttendances) {
                attMap.put(a.getRegistration().getId(), a);
            }

            List<AttendanceRecordDto> records = new ArrayList<>();
            for (Registration reg : confirmed) {
                Attendance existing = attMap.get(reg.getId());
                boolean present = existing != null && existing.isPresent();
                String note = existing != null ? existing.getNote() : "";
                records.add(new AttendanceRecordDto(reg.getId(), selectedDay.getId(), reg.getStudent().getFullName(), present, note));
            }
            formDto.setRecords(records);
        }

        // Statistiky docházky pro každého potvrzeného studenta
        Map<Long, Double> attendanceRates = new HashMap<>();
        Map<Long, Integer> attendedDaysCounts = new HashMap<>();
        for (Registration reg : confirmed) {
            attendanceRates.put(reg.getId(), attendanceService.getStudentAttendancePercentage(reg.getId()));
            attendedDaysCounts.put(reg.getId(), attendanceService.getStudentAttendedDaysCount(reg.getId()));
        }

        model.addAttribute("term", term);
        model.addAttribute("days", days);
        model.addAttribute("selectedDay", selectedDay);
        model.addAttribute("confirmedRegistrations", confirmed);
        model.addAttribute("attendanceForm", formDto);
        model.addAttribute("attendanceRates", attendanceRates);
        model.addAttribute("attendedDaysCounts", attendedDaysCounts);

        return "attendance/sheet";
    }

    @PostMapping
    public String saveAttendance(
            @PathVariable("termId") Long termId,
            @ModelAttribute("attendanceForm") AttendanceFormDto formDto,
            RedirectAttributes redirectAttributes) {

        formDto.setTermId(termId);
        attendanceService.saveAttendance(formDto);

        redirectAttributes.addFlashAttribute("successMessage", "Docházka pro vybraný den byla úspěšně uložena.");
        return "redirect:/terms/" + termId + "/attendance?dayId=" + formDto.getTermDayId();
    }
}
