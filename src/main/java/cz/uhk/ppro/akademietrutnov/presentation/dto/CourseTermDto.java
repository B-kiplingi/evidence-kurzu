package cz.uhk.ppro.akademietrutnov.presentation.dto;

import cz.uhk.ppro.akademietrutnov.domain.model.TermStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class CourseTermDto {

    private Long id;

    @NotNull(message = "Kurz musí být vybrán")
    private Long courseId;

    @NotNull(message = "Datum zahájení je povinné")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @NotNull(message = "Datum ukončení je povinné")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @NotBlank(message = "Místo konání (učebna) je povinné")
    private String location;

    @NotNull(message = "Kapacita je povinná")
    @Min(value = 1, message = "Kapacita musí být alespoň 1")
    private Integer capacity;

    private TermStatus status = TermStatus.PLANNED;

    private Set<Long> lecturerIds = new HashSet<>();

    public CourseTermDto() {
    }

    public CourseTermDto(Long id, Long courseId, LocalDate startDate, LocalDate endDate, String location, Integer capacity, TermStatus status, Set<Long> lecturerIds) {
        this.id = id;
        this.courseId = courseId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
        this.capacity = capacity;
        this.status = status != null ? status : TermStatus.PLANNED;
        this.lecturerIds = lecturerIds != null ? lecturerIds : new HashSet<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
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

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public TermStatus getStatus() {
        return status;
    }

    public void setStatus(TermStatus status) {
        this.status = status;
    }

    public Set<Long> getLecturerIds() {
        return lecturerIds;
    }

    public void setLecturerIds(Set<Long> lecturerIds) {
        this.lecturerIds = lecturerIds;
    }
}
