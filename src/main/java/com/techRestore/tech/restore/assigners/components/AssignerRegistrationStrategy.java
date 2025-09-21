package com.techRestore.tech.restore.assigners.components;

import com.techRestore.tech.restore.assigners.dto.AssignerRegistration;
import com.techRestore.tech.restore.assigners.repository.AssignerRepository;
import com.techRestore.tech.restore.common.interfaces.RegistrationStrategy;
import com.techRestore.tech.restore.common.model.entities.Assigner;
import com.techRestore.tech.restore.common.model.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AssignerRegistrationStrategy implements RegistrationStrategy<Assigner, AssignerRegistration> {
    
    private final AssignerRepository assignerRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public Assigner createEntity(AssignerRegistration registrationData) {
        Assigner assigner = new Assigner();
        assigner.setEmail(registrationData.getEmail());
        assigner.setPassword(passwordEncoder.encode(registrationData.getPassword()));
        assigner.setName(registrationData.getName());
        assigner.setDepartment(registrationData.getDepartment());
        assigner.setPhone(registrationData.getPhone());
        assigner.setRole(Role.ASSIGNER);
        assigner.setCreatedAt(LocalDateTime.now());
        assigner.setActivate(true); // ADD THIS LINE - Make sure assigner is activated
        return assigner;
    }
    
    @Override
    public Assigner saveEntity(Assigner entity) {
        return assignerRepository.save(entity);
    }
    
    @Override
    public String getEmail(AssignerRegistration registrationData) {
        return registrationData.getEmail();
    }
    
    @Override
    public String getSuccessMessage(Assigner entity) {
        return entity.getId().toString();
    }
}