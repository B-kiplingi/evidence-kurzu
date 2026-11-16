package cz.uhk.ppro.akademietrutnov.presentation.dto;

import cz.uhk.ppro.akademietrutnov.domain.model.TermStatus;
import java.time.LocalDate;

public class TermOccupancyReportDto {

    private Long termId;
    private String courseName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private int capacity;
    private long confirmedCount;
    private long waitlistedCount;
    private int occupancyPercentage;
    private TermStatus status;

    public TermOccupancyReportDto() {
    }

    public TermOccupancyReportDto(Long termId, String courseName, LocalDate startDate, LocalDate endDate, String location, int capacity, long confirmedCount, long waitlistedCount, int occupancyPercentage, TermStatus status) {
        this.termId = termId;
        this.courseName = courseName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
        this.capacity = capacity;
        this.confirmedCount = confirmedCount;
        this.waitlistedCount = waitlistedCount;
        this.occupancyPercentage = occupancyPercentage;
        this.status = status;
    }

    public Long getTermId() {
        return termId;
    }

    public void setTermId(Long termId) {
        this.termId = termId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
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

    public long getConfirmedCount() {
        return confirmedCount;
    }

    public void setConfirmedCount(long confirmedCount) {
        this.confirmedCount = confirmedCount;
    }

    public long getWaitlistedCount() {
        return waitlistedCount;
    }

    public void setWaitlistedCount(long waitlistedCount) {
        this.waitlistedCount = waitlistedCount;
    }

    public int getOccupancyPercentage() {
        return occupancyPercentage;
    }

    public void setOccupancyPercentage(int occupancyPercentage) {
        this.occupancyPercentage = occupancyPercentage;
    }

    public TermStatus getStatus() {
        return status;
    }

    public void setStatus(TermStatus status) {
        this.status = status;
    }
}
