package cz.uhk.ppro.akademietrutnov.domain.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Entity
@Table(name = "term_days")
public class TermDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "term_id", nullable = false)
    private CourseTerm term;

    @Column(name = "day_date", nullable = false)
    private LocalDate dayDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "hours_count", nullable = false)
    private int hoursCount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lecturer_id")
    private User lecturer;

    @Column(length = 255)
    private String topic;

    public TermDay() {
    }

    public TermDay(CourseTerm term, LocalDate dayDate, LocalTime startTime, LocalTime endTime, int hoursCount, User lecturer, String topic) {
        this.term = term;
        this.dayDate = dayDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.hoursCount = hoursCount;
        this.lecturer = lecturer;
        this.topic = topic;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CourseTerm getTerm() {
        return term;
    }

    public void setTerm(CourseTerm term) {
        this.term = term;
    }

    public LocalDate getDayDate() {
        return dayDate;
    }

    public void setDayDate(LocalDate dayDate) {
        this.dayDate = dayDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public int getHoursCount() {
        return hoursCount;
    }

    public void setHoursCount(int hoursCount) {
        this.hoursCount = hoursCount;
    }

    public User getLecturer() {
        return lecturer;
    }

    public void setLecturer(User lecturer) {
        this.lecturer = lecturer;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TermDay termDay)) return false;
        return Objects.equals(id, termDay.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
