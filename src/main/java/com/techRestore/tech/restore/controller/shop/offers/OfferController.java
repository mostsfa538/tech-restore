package com.techRestore.tech.restore.controller.shop.offers;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.techRestore.tech.restore.dto.offers.OfferRequestDTO;
import com.techRestore.tech.restore.dto.offers.OfferResponseDTO;
import com.techRestore.tech.restore.services.shop.ShopOfferService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shop/offers")
@RequiredArgsConstructor
public class OfferController {
    private final ShopOfferService offerService;

    @GetMapping
    public ResponseEntity<Page<OfferResponseDTO>> getOffers(Pageable pageable) {
        return ResponseEntity.ok(offerService.getOffersByShop(pageable));
    }

    @GetMapping("/{offerId}")
    public ResponseEntity<OfferResponseDTO> getOfferById(@PathVariable UUID offerId) {
        return ResponseEntity.ok(offerService.getOfferById(offerId));
    }

    @PostMapping
    public ResponseEntity<OfferResponseDTO> createOffer(@RequestBody @Valid OfferRequestDTO request) {
        return ResponseEntity.ok(offerService.createOffer(request));
    }

    @PutMapping("/{offerId}")
    public ResponseEntity<OfferResponseDTO> updateOffer(
            @PathVariable UUID offerId,
            @RequestBody OfferRequestDTO request) {
        return ResponseEntity.ok(offerService.updateOffer(offerId, request));
    }

    @DeleteMapping("/{offerId}")
    public ResponseEntity<Void> deleteOffer(@PathVariable UUID offerId) {
        offerService.deleteOffer(offerId);
        return ResponseEntity.noContent().build();
    }
}
