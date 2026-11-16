package cz.uhk.ppro.akademietrutnov.presentation.dto;

import java.util.ArrayList;
import java.util.List;

public class MultiCourseStudentReportDto {

    private Long studentId;
    private String fullName;
    private String email;
    private String phone;
    private long distinctCoursesCount;
    private List<String> courseNames = new ArrayList<>();

    public MultiCourseStudentReportDto() {
    }

    public MultiCourseStudentReportDto(Long studentId, String fullName, String email, String phone, long distinctCoursesCount, List<String> courseNames) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.distinctCoursesCount = distinctCoursesCount;
        this.courseNames = courseNames != null ? courseNames : new ArrayList<>();
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
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

    public long getDistinctCoursesCount() {
        return distinctCoursesCount;
    }

    public void setDistinctCoursesCount(long distinctCoursesCount) {
        this.distinctCoursesCount = distinctCoursesCount;
    }

    public List<String> getCourseNames() {
        return courseNames;
    }

    public void setCourseNames(List<String> courseNames) {
        this.courseNames = courseNames;
    }
}
