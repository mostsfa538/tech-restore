package com.techRestore.tech.restore.controller.shop;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techRestore.tech.restore.dto.shop.ShopResponseDto;
import com.techRestore.tech.restore.services.shop.ShopServices;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/AllShops")
@RequiredArgsConstructor
public class AllShopsController {
  private final ShopServices shopServices;

  @GetMapping
  public ResponseEntity<Page<ShopResponseDto>> getAllShops(Pageable pageable) {
      Page<ShopResponseDto> shops = shopServices.getAllShops(pageable);
      return ResponseEntity.ok(shops);
  }
  
}
