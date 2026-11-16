package cz.uhk.ppro.akademietrutnov.presentation.controller;

import cz.uhk.ppro.akademietrutnov.domain.service.ReportService;
import cz.uhk.ppro.akademietrutnov.presentation.dto.LecturerHoursReportDto;
import cz.uhk.ppro.akademietrutnov.presentation.dto.MultiCourseStudentReportDto;
import cz.uhk.ppro.akademietrutnov.presentation.dto.TermOccupancyReportDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String showReports(
            @RequestParam(value = "year", required = false) Integer year,
            Model model) {

        int selectedYear = (year != null) ? year : LocalDate.now().getYear();

        List<TermOccupancyReportDto> occupancyReport = reportService.getTermOccupancyReport();
        List<LecturerHoursReportDto> lecturerReport = reportService.getLecturerHoursReport();
        List<MultiCourseStudentReportDto> multiCourseReport = reportService.getMultiCourseStudentsReport(selectedYear);

        model.addAttribute("occupancyReport", occupancyReport);
        model.addAttribute("lecturerReport", lecturerReport);
        model.addAttribute("multiCourseReport", multiCourseReport);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("currentYear", LocalDate.now().getYear());

        return "reports/dashboard";
    }
}
