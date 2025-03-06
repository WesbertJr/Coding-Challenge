package com.reliaquest.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Employee {
    @NotBlank
    private String id;

    @NotBlank
    @JsonProperty("employee_name")
    private String name;

    @Positive @NotNull @JsonProperty("employee_salary")
    private Integer salary;

    @Min(16)
    @Max(75)
    @NotNull @JsonProperty("employee_age")
    private Integer age;

    @NotBlank
    @JsonProperty("employee_title")
    private String title;

    @JsonProperty("employee_email")
    private String email;

    @Override
    public String toString() {
        return "Employee{" + "id='"
                + id + '\'' + ", name='"
                + name + '\'' + ", salary="
                + salary + ", age="
                + age + ", title='"
                + title + '\'' + ", email='"
                + email + '\'' + "} "
                + super.toString();
    }
}
