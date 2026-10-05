package cz.uhk.ppro.akademietrutnov.presentation.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CourseDto {

    private Long id;

    @NotBlank(message = "Název kurzu je povinný.")
    @Size(max = 150, message = "Název kurzu může mít maximálně 150 znaků.")
    private String name;

    private String annotation;

    @NotNull(message = "Rozsah kurzu v hodinách je povinný.")
    @Min(value = 1, message = "Rozsah kurzu musí být alespoň 1 hodina.")
    @Max(value = 1000, message = "Rozsah kurzu nemůže překročit 1000 hodin.")
    private Integer hoursDuration;

    @NotNull(message = "Cena kurzu je povinná.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Cena kurzu nesmí být záporná.")
    private BigDecimal price;

    private boolean accredited;

    public CourseDto() {
    }

    public CourseDto(Long id, String name, String annotation, Integer hoursDuration, BigDecimal price, boolean accredited) {
        this.id = id;
        this.name = name;
        this.annotation = annotation;
        this.hoursDuration = hoursDuration;
        this.price = price;
        this.accredited = accredited;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public Integer getHoursDuration() {
        return hoursDuration;
    }

    public void setHoursDuration(Integer hoursDuration) {
        this.hoursDuration = hoursDuration;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isAccredited() {
        return accredited;
    }

    public void setAccredited(boolean accredited) {
        this.accredited = accredited;
    }
}
