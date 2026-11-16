package cz.uhk.ppro.akademietrutnov.presentation.dto;

import java.util.ArrayList;
import java.util.List;

public class AttendanceFormDto {

    private Long termId;
    private Long termDayId;
    private List<AttendanceRecordDto> records = new ArrayList<>();

    public AttendanceFormDto() {
    }

    public AttendanceFormDto(Long termId, Long termDayId, List<AttendanceRecordDto> records) {
        this.termId = termId;
        this.termDayId = termDayId;
        this.records = records != null ? records : new ArrayList<>();
    }

    public Long getTermId() {
        return termId;
    }

    public void setTermId(Long termId) {
        this.termId = termId;
    }

    public Long getTermDayId() {
        return termDayId;
    }

    public void setTermDayId(Long termDayId) {
        this.termDayId = termDayId;
    }

    public List<AttendanceRecordDto> getRecords() {
        return records;
    }

    public void setRecords(List<AttendanceRecordDto> records) {
        this.records = records;
    }
}
