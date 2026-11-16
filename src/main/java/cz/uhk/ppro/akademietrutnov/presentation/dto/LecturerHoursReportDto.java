package cz.uhk.ppro.akademietrutnov.presentation.dto;

public class LecturerHoursReportDto {

    private Long lecturerId;
    private String fullName;
    private String email;
    private String phone;
    private int termsTaughtCount;
    private int totalHoursTaught;

    public LecturerHoursReportDto() {
    }

    public LecturerHoursReportDto(Long lecturerId, String fullName, String email, String phone, int termsTaughtCount, int totalHoursTaught) {
        this.lecturerId = lecturerId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.termsTaughtCount = termsTaughtCount;
        this.totalHoursTaught = totalHoursTaught;
    }

    public Long getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(Long lecturerId) {
        this.lecturerId = lecturerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getTermsTaughtCount() {
        return termsTaughtCount;
    }

    public void setTermsTaughtCount(int termsTaughtCount) {
        this.termsTaughtCount = termsTaughtCount;
    }

    public int getTotalHoursTaught() {
        return totalHoursTaught;
    }

    public void setTotalHoursTaught(int totalHoursTaught) {
        this.totalHoursTaught = totalHoursTaught;
    }
}
