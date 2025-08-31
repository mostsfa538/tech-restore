package com.techRestore.tech.restore.dto.delivery;

import lombok.Data;

@Data
public class DeliveryRegistration {
  private String email;
  private String password;
  private String name;
  private String address;
  private String phone;
}
