package cz.uhk.ppro.akademietrutnov.domain.service;

import cz.uhk.ppro.akademietrutnov.presentation.dto.LecturerHoursReportDto;
import cz.uhk.ppro.akademietrutnov.presentation.dto.MultiCourseStudentReportDto;
import cz.uhk.ppro.akademietrutnov.presentation.dto.TermOccupancyReportDto;

import java.util.List;

public interface ReportService {

    List<TermOccupancyReportDto> getTermOccupancyReport();

    List<LecturerHoursReportDto> getLecturerHoursReport();

    List<MultiCourseStudentReportDto> getMultiCourseStudentsReport(int year);
}
