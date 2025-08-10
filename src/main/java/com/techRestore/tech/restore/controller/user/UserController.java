package com.techRestore.tech.restore.controller.user;

import com.techRestore.tech.restore.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.services.user.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserServices userServices;

    @GetMapping("/{shopId}/{categoryId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsOfShopWithCategory(
            @PathVariable UUID shopId,
            @PathVariable UUID categoryId) {
        List<ProductResponseDTO> products = userServices.getProductsByCategory(shopId, categoryId);
        return ResponseEntity.ok(products);
    }
}
