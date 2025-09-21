package com.techRestore.tech.restore.common.security.userdetails;

import com.techRestore.tech.restore.assigners.repository.AssignerRepository;
import com.techRestore.tech.restore.common.model.entities.Assigner;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssignerDetailsServiceImpl implements UserDetailsService {
    private final AssignerRepository assignerRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Assigner assigner = assignerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Assigner not found"));
        return new AssignerPrincipal(assigner);
    }
}
