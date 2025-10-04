package com.techRestore.tech.restore.user.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techRestore.tech.restore.common.controller.BaseController;
import com.techRestore.tech.restore.shop.dto.shop.ShopResponseDto;
import com.techRestore.tech.restore.user.service.UsetShopService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserShopController extends BaseController {

    private final UsetShopService userServices;

    @GetMapping("/shops/all")
    public ResponseEntity<Page<ShopResponseDto>> getAllShops(Pageable pageable) {
        return successResponse(userServices.getAllShops(pageable));
    }
}
