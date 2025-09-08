package com.techRestore.tech.restore.common.interfaces;

import java.time.LocalDateTime;

public interface OtpVerifiable {
    String getEmail();

    String getOptCode();

    void setOptCode(String optCode);

    LocalDateTime getOtpExpiry();

    void setOtpExpiry(LocalDateTime otpExpiry);

    String getPassword();

    void setPassword(String password);

    void setActivate(boolean activate);

    String getDisplayName();

    String getEntityType();
}
