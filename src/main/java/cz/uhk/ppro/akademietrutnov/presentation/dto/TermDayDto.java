package cz.uhk.ppro.akademietrutnov.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public class TermDayDto {

    private Long id;

    @NotNull(message = "ID termínu je povinné")
    private Long termId;

    @NotNull(message = "Datum dne je povinné")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dayDate;

    @NotNull(message = "Čas od je povinný")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "Čas do je povinný")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @NotNull(message = "Počet hodin je povinný")
    @Min(value = 1, message = "Počet hodin musí být alespoň 1")
    private Integer hoursCount;

    private Long lecturerId;

    private String topic;

    public TermDayDto() {
    }

    public TermDayDto(Long id, Long termId, LocalDate dayDate, LocalTime startTime, LocalTime endTime, Integer hoursCount, Long lecturerId, String topic) {
        this.id = id;
        this.termId = termId;
        this.dayDate = dayDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.hoursCount = hoursCount;
        this.lecturerId = lecturerId;
        this.topic = topic;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTermId() {
        return termId;
    }

    public void setTermId(Long termId) {
        this.termId = termId;
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

    public Integer getHoursCount() {
        return hoursCount;
    }

    public void setHoursCount(Integer hoursCount) {
        this.hoursCount = hoursCount;
    }

    public Long getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(Long lecturerId) {
        this.lecturerId = lecturerId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
