package cz.uhk.ppro.akademietrutnov.domain.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "course_terms")
public class CourseTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, length = 150)
    private String location;

    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TermStatus status = TermStatus.PLANNED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "term_lecturers",
            joinColumns = @JoinColumn(name = "term_id"),
            inverseJoinColumns = @JoinColumn(name = "lecturer_id")
    )
    private Set<User> lecturers = new HashSet<>();

    @OneToMany(mappedBy = "term", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayDate ASC, startTime ASC")
    private List<TermDay> termDays = new ArrayList<>();

    @OneToMany(mappedBy = "term", cascade = CascadeType.ALL)
    @OrderBy("createdAt ASC")
    private List<Registration> registrations = new ArrayList<>();

    public CourseTerm() {
    }

    public CourseTerm(Course course, LocalDate startDate, LocalDate endDate, String location, int capacity) {
        this.course = course;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
        this.capacity = capacity;
        this.status = TermStatus.PLANNED;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public TermStatus getStatus() {
        return status;
    }

    public void setStatus(TermStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<User> getLecturers() {
        return lecturers;
    }

    public void setLecturers(Set<User> lecturers) {
        this.lecturers = lecturers;
    }

    public void addLecturer(User lecturer) {
        this.lecturers.add(lecturer);
    }

    public void removeLecturer(User lecturer) {
        this.lecturers.remove(lecturer);
    }

    public List<TermDay> getTermDays() {
        return termDays;
    }

    public void setTermDays(List<TermDay> termDays) {
        this.termDays = termDays;
    }

    public void addTermDay(TermDay day) {
        termDays.add(day);
        day.setTerm(this);
    }

    public List<Registration> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<Registration> registrations) {
        this.registrations = registrations;
    }

    public long getConfirmedCount() {
        return registrations.stream()
                .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMED || r.getStatus() == RegistrationStatus.COMPLETED)
                .count();
    }

    public long getWaitlistedCount() {
        return registrations.stream()
                .filter(r -> r.getStatus() == RegistrationStatus.WAITLISTED)
                .count();
    }

    public int getAvailableSpots() {
        return Math.max(0, capacity - (int) getConfirmedCount());
    }

    public boolean isFull() {
        return getConfirmedCount() >= capacity;
    }

    public int getOccupancyPercentage() {
        if (capacity <= 0) return 0;
        return (int) Math.round(((double) getConfirmedCount() / capacity) * 100);
    }

    public boolean isStarted() {
        return startDate != null && startDate.isBefore(LocalDate.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CourseTerm that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
