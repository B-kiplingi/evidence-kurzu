package cz.uhk.ppro.akademietrutnov.domain.service;

import cz.uhk.ppro.akademietrutnov.domain.model.CourseTerm;
import cz.uhk.ppro.akademietrutnov.domain.model.Registration;
import cz.uhk.ppro.akademietrutnov.domain.model.User;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.CourseTermRepository;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.RegistrationRepository;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.TermDayRepository;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.UserRepository;
import cz.uhk.ppro.akademietrutnov.presentation.dto.LecturerHoursReportDto;
import cz.uhk.ppro.akademietrutnov.presentation.dto.MultiCourseStudentReportDto;
import cz.uhk.ppro.akademietrutnov.presentation.dto.TermOccupancyReportDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final CourseTermRepository courseTermRepository;
    private final RegistrationRepository registrationRepository;
    private final TermDayRepository termDayRepository;
    private final UserRepository userRepository;

    public ReportServiceImpl(CourseTermRepository courseTermRepository,
                             RegistrationRepository registrationRepository,
                             TermDayRepository termDayRepository,
                             UserRepository userRepository) {
        this.courseTermRepository = courseTermRepository;
        this.registrationRepository = registrationRepository;
        this.termDayRepository = termDayRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<TermOccupancyReportDto> getTermOccupancyReport() {
        List<CourseTerm> terms = courseTermRepository.findAllByOrderByStartDateDesc();
        List<TermOccupancyReportDto> reports = new ArrayList<>();

        for (CourseTerm term : terms) {
            reports.add(new TermOccupancyReportDto(
                    term.getId(),
                    term.getCourse().getName(),
                    term.getStartDate(),
                    term.getEndDate(),
                    term.getLocation(),
                    term.getCapacity(),
                    term.getConfirmedCount(),
                    term.getWaitlistedCount(),
                    term.getOccupancyPercentage(),
                    term.getStatus()
            ));
        }

        return reports;
    }

    @Override
    public List<LecturerHoursReportDto> getLecturerHoursReport() {
        List<User> lecturers = userRepository.findAll().stream()
                .filter(User::isLecturer)
                .toList();

        List<LecturerHoursReportDto> reports = new ArrayList<>();

        for (User lecturer : lecturers) {
            int totalHours = termDayRepository.sumHoursByLecturerId(lecturer.getId());
            List<CourseTerm> terms = courseTermRepository.findByLecturerId(lecturer.getId());

            reports.add(new LecturerHoursReportDto(
                    lecturer.getId(),
                    lecturer.getFullName(),
                    lecturer.getEmail(),
                    lecturer.getPhone() != null ? lecturer.getPhone() : "-",
                    terms.size(),
                    totalHours
            ));
        }

        // Seřadit podle celkového počtu odučených hodin sestupně
        reports.sort((a, b) -> Integer.compare(b.getTotalHoursTaught(), a.getTotalHoursTaught()));
        return reports;
    }

    @Override
    public List<MultiCourseStudentReportDto> getMultiCourseStudentsReport(int year) {
        List<Object[]> rawData = registrationRepository.findStudentsWithMultipleCoursesInYear(year);
        List<MultiCourseStudentReportDto> results = new ArrayList<>();

        for (Object[] row : rawData) {
            Long studentId = (Long) row[0];
            String fullName = (String) row[1];
            String email = (String) row[2];
            String phone = (String) row[3];
            long count = (Long) row[4];

            // Načíst unikátní názvy kurzů tohoto studenta v daném roce
            List<Registration> studentRegs = registrationRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
            List<String> courseNames = studentRegs.stream()
                    .filter(r -> !r.isCancelled() && r.getCreatedAt().getYear() == year)
                    .map(r -> r.getTerm().getCourse().getName())
                    .distinct()
                    .collect(Collectors.toList());

            results.add(new MultiCourseStudentReportDto(
                    studentId,
                    fullName,
                    email,
                    phone != null ? phone : "-",
                    count,
                    courseNames
            ));
        }

        return results;
    }
}
