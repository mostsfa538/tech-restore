package com.techRestore.tech.restore.user.service;

import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.User;
import com.techRestore.tech.restore.common.utils.DTOConverter;
import com.techRestore.tech.restore.user.dto.user.UserProfileDTO;
import com.techRestore.tech.restore.user.dto.user.UserProfileUpdateDTO;
import com.techRestore.tech.restore.user.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServices {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthUtil authUtil;

    @Transactional(readOnly = true)
    public UserProfileDTO getCurrentUserProfile() {
        User user = authUtil.getCurrentUser();

        User fullUser = userRepository.findByIdWithAddresses(user.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        return DTOConverter.convertToUserProfileDTO(fullUser);
    }

    @Transactional
    public UserProfileDTO updateUserProfile(UserProfileUpdateDTO updateDTO) {
        User user = authUtil.getCurrentUser();

        if (updateDTO.getFirst_name() != null) {
            user.setFirst_name(updateDTO.getFirst_name());
        }
        if (updateDTO.getLast_name() != null) {
            user.setLast_name(updateDTO.getLast_name());
        }
        if (updateDTO.getPhone() != null) {
            user.setPhone(updateDTO.getPhone());
        }
        userRepository.save(user);
        return getCurrentUserProfile();
    }

    @Transactional
    public void deactivateUserAccount() {
        User user = authUtil.getCurrentUser();
        user.setActivate(false);
        userRepository.save(user);
    }
}