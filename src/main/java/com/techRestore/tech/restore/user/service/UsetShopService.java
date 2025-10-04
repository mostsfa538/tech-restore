package com.techRestore.tech.restore.user.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techRestore.tech.restore.common.utils.DTOConverter;
import com.techRestore.tech.restore.shop.dto.shop.ShopResponseDto;
import com.techRestore.tech.restore.shop.repository.ShopRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UsetShopService {

    private final ShopRepository shopRepository;

    @Transactional(readOnly = true)
    public Page<ShopResponseDto> getAllShops(Pageable pageable) {
        return shopRepository.findAll(pageable)
                .map(DTOConverter::convertToShopDTO);
    }


}
