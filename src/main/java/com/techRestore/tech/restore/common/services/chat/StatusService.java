package com.techRestore.tech.restore.common.services.chat;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.model.entities.User;
import com.techRestore.tech.restore.common.model.enums.Status;
import com.techRestore.tech.restore.common.utils.DTOConverter;
import com.techRestore.tech.restore.shop.dto.shop.ShopResponseDto;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatusService {
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    public void connect(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setStatus(Status.ONLINE);
            userRepository.save(user);
            return;
        }
        Shop shop = shopRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Shop not found"));
        if (shop != null) {
            shop.setStatus(Status.ONLINE);
            shopRepository.save(shop);
        }
    }

    public void disconnect(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setStatus(Status.OFFLINE);
            userRepository.save(user);
            return;
        }
       Shop shop = shopRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Shop not found"));
        if (shop != null) {
            shop.setStatus(Status.OFFLINE);
            shopRepository.save(shop);
        }
    }

    public List<ShopResponseDto> findConnectedShops() {
        return shopRepository.findAllByStatus(Status.ONLINE)
                .stream()
                .map(DTOConverter::convertToShopyDTO)
                .collect(Collectors.toList());
    }
}