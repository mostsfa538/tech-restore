package com.techRestore.tech.restore.dto.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateDTO {
    @Size(min = 2, max = 50)
    private String first_name;
    @Size(min = 2, max = 50)
    private String last_name;
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;
}