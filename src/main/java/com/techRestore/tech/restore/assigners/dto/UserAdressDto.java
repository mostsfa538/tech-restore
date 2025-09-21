package com.techRestore.tech.restore.assigners.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class UserAdressDto {
  private UUID id;
    private String state;
    private String city;
    private String street;
    private String building;
    private String notes;
    private boolean isDefault;
}
