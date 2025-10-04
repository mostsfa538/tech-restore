package com.techRestore.tech.restore.user.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techRestore.tech.restore.common.controller.BaseController;
import com.techRestore.tech.restore.shop.dto.offers.OfferResponseDTO;
import com.techRestore.tech.restore.user.service.UserOffersService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserOfferController extends BaseController {

    private final UserOffersService offerService;

    @GetMapping("/offers")
    public ResponseEntity<Page<OfferResponseDTO>> getUserOffers(Pageable pageable) {
        Page<OfferResponseDTO> offers = offerService.getUserOffers(pageable);
        return successResponse(offers);
    }

    @GetMapping("offers/{offerId}")
    public ResponseEntity<OfferResponseDTO> getOfferById(@PathVariable UUID offerId) {
        OfferResponseDTO offer = offerService.getOfferById(offerId);
        return successResponse(offer);
    }
}
