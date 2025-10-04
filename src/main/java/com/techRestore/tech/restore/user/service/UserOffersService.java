package com.techRestore.tech.restore.user.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.utils.DTOConverter;
import com.techRestore.tech.restore.shop.dto.offers.OfferResponseDTO;
import com.techRestore.tech.restore.shop.repository.OffersRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserOffersService {

    private final OffersRepository offersRepository;

    @Transactional(readOnly = true)
    public Page<OfferResponseDTO> getUserOffers(Pageable pageable) {
        return offersRepository.findAll(pageable)
                .map(DTOConverter::convertToOfferResponseDTO);
    }

    @Transactional(readOnly = true)
    public OfferResponseDTO getOfferById(UUID offerId) {
        return offersRepository.findById(offerId)
                .map(DTOConverter::convertToOfferResponseDTO)
                .orElseThrow(() -> new NotFoundException("Offer not found"));
    }
}
