package cz.uhk.ppro.akademietrutnov.presentation.dto;

public class RegistrationRequestDto {

    private Long termId;
    private Long studentId;
    private String exceptionReason;
    private String cancellationReason;

    public RegistrationRequestDto() {
    }

    public RegistrationRequestDto(Long termId, Long studentId, String exceptionReason) {
        this.termId = termId;
        this.studentId = studentId;
        this.exceptionReason = exceptionReason;
    }

    public Long getTermId() {
        return termId;
    }

    public void setTermId(Long termId) {
        this.termId = termId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getExceptionReason() {
        return exceptionReason;
    }

    public void setExceptionReason(String exceptionReason) {
        this.exceptionReason = exceptionReason;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}
