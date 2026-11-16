package cz.uhk.ppro.akademietrutnov.presentation.dto;

public class AttendanceRecordDto {

    private Long registrationId;
    private Long termDayId;
    private String studentName;
    private boolean present;
    private String note;

    public AttendanceRecordDto() {
    }

    public AttendanceRecordDto(Long registrationId, Long termDayId, String studentName, boolean present, String note) {
        this.registrationId = registrationId;
        this.termDayId = termDayId;
        this.studentName = studentName;
        this.present = present;
        this.note = note;
    }

    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
    }

    public Long getTermDayId() {
        return termDayId;
    }

    public void setTermDayId(Long termDayId) {
        this.termDayId = termDayId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
