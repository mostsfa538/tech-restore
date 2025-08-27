package com.techRestore.tech.restore.controller.user;

import com.techRestore.tech.restore.controller.BaseController;
import com.techRestore.tech.restore.dto.user.UserProfileDTO;
import com.techRestore.tech.restore.dto.user.UserProfileUpdateDTO;
import com.techRestore.tech.restore.services.user.UserServices;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserServices userServices;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile() {
        UserProfileDTO profile = userServices.getCurrentUserProfile();
        return successResponse(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDTO> updateUserProfile(@RequestBody UserProfileUpdateDTO updateDTO) {
        UserProfileDTO updatedProfile = userServices.updateUserProfile(updateDTO);
        return updatedResponse(updatedProfile);
    }

    @DeleteMapping("/profile")
    public ResponseEntity<Void> deleteUserAccount() {
        userServices.deleteUserAccount();
        return deletedResponse();
    }
}
