package cz.uhk.ppro.akademietrutnov.domain.service;

import cz.uhk.ppro.akademietrutnov.domain.model.Attendance;
import cz.uhk.ppro.akademietrutnov.presentation.dto.AttendanceFormDto;

import java.util.List;

public interface AttendanceService {

    void saveAttendance(AttendanceFormDto formDto);

    List<Attendance> getAttendanceForDay(Long termDayId);

    List<Attendance> getAttendanceForRegistration(Long registrationId);

    double getStudentAttendancePercentage(Long registrationId);

    int getStudentAttendedDaysCount(Long registrationId);
}
