package com.techRestore.tech.restore.shop.service;

import com.techRestore.tech.restore.admin.repository.CategoryRepository;
import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.Category;
import com.techRestore.tech.restore.common.model.entities.Product;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.services.BaseService;
import com.techRestore.tech.restore.common.utils.DTOConverter;
import com.techRestore.tech.restore.shop.dto.product.CreateProductDto;
import com.techRestore.tech.restore.shop.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.shop.dto.product.UpdateProductDto;
import com.techRestore.tech.restore.shop.dto.shop.StockUpdateRequest;
import com.techRestore.tech.restore.shop.repository.ProductRepository;
import com.techRestore.tech.restore.shop.repository.ShopRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ShopProductService extends BaseService<Product, UUID> {

    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepository;

    public ShopProductService(ProductRepository productRepository,
            ShopRepository shopRepository,
            CategoryRepository categoryRepository) {
        super(productRepository);
        this.shopRepository = shopRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Get current authenticated shop ID
     */
    private UUID getCurrentShopId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Shop shop = shopRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Shop not found with email: " + email));
        return shop.getId();
    }

    public Page<ProductResponseDTO> getProductsByShopId(Pageable pageable) {
        UUID shopId = getCurrentShopId();
        return ((ProductRepository) repository).findByShopId(shopId, pageable)
                .map(DTOConverter::convertToProductDTO);
    }

    public ProductResponseDTO addProductToShop(CreateProductDto createProductDto) {
        UUID shopId = getCurrentShopId();

        findByIdOrThrow(shopRepository, shopId, "Shop");

        Product product = new Product();
        product.setShopId(shopId);
        product.setName(createProductDto.name());
        product.setDescription(createProductDto.description());
        product.setPrice(createProductDto.price());
        product.setImageUrl(createProductDto.imageUrl());
        product.setStock(createProductDto.stockQuantity());
        product.setCondition(createProductDto.condition());

        if (createProductDto.category() != null && createProductDto.category().getId() != null) {
            Category category = findByIdOrThrow(categoryRepository, createProductDto.category().getId(), "Category");
            product.setCategory(category);
        }

        repository.save(product);
        return DTOConverter.convertToProductDTO(product);
    }

    public ProductResponseDTO updateProduct(UUID productId, UpdateProductDto updateProductDto) {
        Product product = findProductByIdAndValidateOwnership(productId);

        if (updateProductDto.name() != null && !updateProductDto.name().trim().isEmpty()) {
            product.setName(updateProductDto.name().trim());
        }
        if (updateProductDto.description() != null && !updateProductDto.description().trim().isEmpty()) {
            product.setDescription(updateProductDto.description().trim());
        }
        if (updateProductDto.price() != null) {
            product.setPrice(updateProductDto.price());
        }
        if (updateProductDto.imageUrl() != null) {
            product.setImageUrl(updateProductDto.imageUrl());
        }
        if (updateProductDto.category() != null) {
            product.setCategory(updateProductDto.category());
        }
        if (updateProductDto.stockQuantity() != null) {
            product.setStock(updateProductDto.stockQuantity());
        }
        if (updateProductDto.condition() != null) {
            product.setCondition(updateProductDto.condition());
        }

        repository.save(product);
        return DTOConverter.convertToProductDTO(product);
    }

    public void deleteProduct(UUID productId) {
        Product product = findProductByIdAndValidateOwnership(productId);
        product.setDeleted(true);
        repository.save(product);
    }

    public ProductResponseDTO updateProductStock(UUID productId, StockUpdateRequest stockUpdateRequest) {
        Product product = findProductByIdAndValidateOwnership(productId);

        if (stockUpdateRequest.newStock() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        product.setStock(stockUpdateRequest.newStock());
        repository.save(product);

        return DTOConverter.convertToProductDTO(product);
    }

    private Product findProductByIdAndValidateOwnership(UUID productId) {
        UUID shopId = getCurrentShopId();
        Product product = findByIdOrThrow(productId, "Product");

        if (!product.getShopId().equals(shopId)) {
            throw new NotFoundException("Product not found for this shop");
        }

        return product;
    }

}