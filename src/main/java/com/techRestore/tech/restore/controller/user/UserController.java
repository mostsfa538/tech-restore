package com.techRestore.tech.restore.controller.user;

import com.techRestore.tech.restore.controller.BaseController;
import com.techRestore.tech.restore.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.services.user.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController extends BaseController {

    @Autowired
    private UserServices userServices;

    @GetMapping("/{shopId}/{categoryId}")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsOfShopWithCategory(
            @PathVariable UUID shopId,
            @PathVariable UUID categoryId,
            Pageable pageable) {
        Page<ProductResponseDTO> products = userServices.getProductsByCategory(shopId, categoryId, pageable);
        return successResponse(products);
    }
}
