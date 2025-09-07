package com.techRestore.tech.restore.shop.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.Offer;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.utils.DTOConverter;
import com.techRestore.tech.restore.shop.dto.offers.OfferRequestDTO;
import com.techRestore.tech.restore.shop.dto.offers.OfferResponseDTO;
import com.techRestore.tech.restore.shop.repository.OffersRepository;
import com.techRestore.tech.restore.shop.repository.ShopRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShopOfferService {

    private final ShopRepository shopRepository;
    private final OffersRepository offersRepository;

    private Shop getCurrentShop() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user found");
        }

        String email = authentication.getName();
        Optional<Shop> shop = shopRepository.findByEmail(email);
        if (shop.isEmpty()) {
            throw new NotFoundException("Shop not found: " + email);
        }
        return shop.get();
    }

    public Page<OfferResponseDTO> getOffersByShop(Pageable pageable) {
        UUID shopId = getCurrentShop().getId();
        return offersRepository.findByShopId(shopId, pageable)
                .map(DTOConverter::convertToOfferResponseDTO);
    }

    public OfferResponseDTO getOfferById(UUID offerId) {
        UUID shopId = getCurrentShop().getId();
        return offersRepository.findByShopIdAndId(shopId, offerId)
                .map(DTOConverter::convertToOfferResponseDTO)
                .orElseThrow(() -> new NotFoundException("Offer not found"));
    }

    public OfferResponseDTO createOffer(OfferRequestDTO request) {
        Offer offer = new Offer();
        offer.setName(request.getName());
        offer.setDescription(request.getDescription());
        offer.setDiscountValue(request.getDiscountValue());
        offer.setStartDate(request.getStartDate());
        offer.setEndDate(request.getEndDate());
        offer.setStatus(request.getStatus());
        offer.setShop(getCurrentShop());
        Offer savedOffer = offersRepository.save(offer);
        return DTOConverter.convertToOfferResponseDTO(savedOffer);
    }

    public OfferResponseDTO updateOffer(UUID offerId, OfferRequestDTO request) {
        Offer existingOffer = offersRepository.findById(offerId)
                .orElseThrow(() -> new NotFoundException("Offer not found"));

        if (request.getName() != null) {
            existingOffer.setName(request.getName());
        }
        if (request.getDescription() != null) {
            existingOffer.setDescription(request.getDescription());
        }
        if (request.getDiscountValue() != null) {
            existingOffer.setDiscountValue(request.getDiscountValue());
        }
        if (request.getStartDate() != null) {
            existingOffer.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            existingOffer.setEndDate(request.getEndDate());
        }
        if (request.getStatus() != null) {
            existingOffer.setStatus(request.getStatus());
        }

        Offer updatedOffer = offersRepository.save(existingOffer);
        return DTOConverter.convertToOfferResponseDTO(updatedOffer);
    }

    public void deleteOffer(UUID offerId) {
        if (!offersRepository.existsById(offerId)) {
            throw new NotFoundException("Offer not found");
        }
        offersRepository.deleteById(offerId);
    }

}
