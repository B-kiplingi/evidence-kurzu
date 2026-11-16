package cz.uhk.ppro.akademietrutnov.domain.service;

import cz.uhk.ppro.akademietrutnov.domain.exception.BusinessRuleException;
import cz.uhk.ppro.akademietrutnov.domain.exception.EntityNotFoundException;
import cz.uhk.ppro.akademietrutnov.domain.model.Course;
import cz.uhk.ppro.akademietrutnov.domain.model.CourseTerm;
import cz.uhk.ppro.akademietrutnov.domain.model.TermDay;
import cz.uhk.ppro.akademietrutnov.domain.model.TermStatus;
import cz.uhk.ppro.akademietrutnov.domain.model.User;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.CourseRepository;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.CourseTermRepository;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.TermDayRepository;
import cz.uhk.ppro.akademietrutnov.infrastructure.repository.UserRepository;
import cz.uhk.ppro.akademietrutnov.presentation.dto.CourseTermDto;
import cz.uhk.ppro.akademietrutnov.presentation.dto.TermDayDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class CourseTermServiceImpl implements CourseTermService {

    private final CourseTermRepository courseTermRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final TermDayRepository termDayRepository;

    public CourseTermServiceImpl(CourseTermRepository courseTermRepository,
                                  CourseRepository courseRepository,
                                  UserRepository userRepository,
                                  TermDayRepository termDayRepository) {
        this.courseTermRepository = courseTermRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.termDayRepository = termDayRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseTerm> findAll() {
        return courseTermRepository.findAllByOrderByStartDateDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseTerm> findByCourseId(Long courseId) {
        return courseTermRepository.findByCourseIdOrderByStartDateAsc(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseTerm> findByStatus(TermStatus status) {
        return courseTermRepository.findByStatusOrderByStartDateDesc(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseTerm> findByLecturerId(Long lecturerId) {
        return courseTermRepository.findByLecturerId(lecturerId);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseTerm findById(Long id) {
        return courseTermRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Termín kurzu s ID " + id + " nebyl nalezen."));
    }

    @Override
    public CourseTerm create(CourseTermDto dto) {
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BusinessRuleException("Datum ukončení termínu nesmí předcházet datu zahájení.");
        }

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Kurz s ID " + dto.getCourseId() + " nebyl nalezen."));

        CourseTerm term = new CourseTerm(course, dto.getStartDate(), dto.getEndDate(), dto.getLocation(), dto.getCapacity());
        term.setStatus(dto.getStatus() != null ? dto.getStatus() : TermStatus.PLANNED);

        if (dto.getLecturerIds() != null && !dto.getLecturerIds().isEmpty()) {
            Set<User> lecturers = new HashSet<>(userRepository.findAllById(dto.getLecturerIds()));
            term.setLecturers(lecturers);
        }

        return courseTermRepository.save(term);
    }

    @Override
    public CourseTerm update(Long id, CourseTermDto dto) {
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BusinessRuleException("Datum ukončení termínu nesmí předcházet datu zahájení.");
        }

        CourseTerm term = findById(id);
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Kurz s ID " + dto.getCourseId() + " nebyl nalezen."));

        term.setCourse(course);
        term.setStartDate(dto.getStartDate());
        term.setEndDate(dto.getEndDate());
        term.setLocation(dto.getLocation());
        term.setCapacity(dto.getCapacity());
        term.setStatus(dto.getStatus());

        if (dto.getLecturerIds() != null) {
            Set<User> lecturers = new HashSet<>(userRepository.findAllById(dto.getLecturerIds()));
            term.setLecturers(lecturers);
        }

        return courseTermRepository.save(term);
    }

    @Override
    public void delete(Long id) {
        CourseTerm term = findById(id);
        courseTermRepository.delete(term);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseTermDto toDto(CourseTerm term) {
        Set<Long> lecturerIds = term.getLecturers().stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        return new CourseTermDto(
                term.getId(),
                term.getCourse().getId(),
                term.getStartDate(),
                term.getEndDate(),
                term.getLocation(),
                term.getCapacity(),
                term.getStatus(),
                lecturerIds
        );
    }

    @Override
    public TermDay addDay(Long termId, TermDayDto dayDto) {
        CourseTerm term = findById(termId);

        User lecturer = null;
        if (dayDto.getLecturerId() != null) {
            lecturer = userRepository.findById(dayDto.getLecturerId()).orElse(null);
        }

        TermDay day = new TermDay(term, dayDto.getDayDate(), dayDto.getStartTime(), dayDto.getEndTime(),
                dayDto.getHoursCount(), lecturer, dayDto.getTopic());

        term.addTermDay(day);
        return termDayRepository.save(day);
    }

    @Override
    public void removeDay(Long dayId) {
        TermDay day = termDayRepository.findById(dayId)
                .orElseThrow(() -> new EntityNotFoundException("Den rozvrhu s ID " + dayId + " nebyl nalezen."));
        termDayRepository.delete(day);
    }
}
