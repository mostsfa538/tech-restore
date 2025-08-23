package com.techRestore.tech.restore.dto.adress;

import lombok.Data;

@Data
public class AddressRequestDTO {
  private String state;
  private String city;
  private String street;
  private String building;
  private String notes;
  private boolean isDefault;
}