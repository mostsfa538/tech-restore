package com.techRestore.tech.restore.user.service;

import com.techRestore.tech.restore.admin.repository.CategoryRepository;
import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.Product;
import com.techRestore.tech.restore.common.services.BaseService;
import com.techRestore.tech.restore.common.utils.DTOConverter;
import com.techRestore.tech.restore.shop.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.shop.repository.ProductRepository;
import static com.techRestore.tech.restore.shop.repository.spec.ProductSpecifications.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class UserProductService extends BaseService<Product, UUID> {

    private final CategoryRepository categoryRepository;

    private final ProductRepository productRepository;

    public UserProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        super(productRepository);
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAll(shopVerified(), pageable)
                .map(DTOConverter::convertToProductDTO);
    }

    public ProductResponseDTO getProductById(UUID productId) {
        Product product = findByIdOrThrow(productId, "Product");
        return DTOConverter.convertToProductDTO(product);
    }

    public Page<ProductResponseDTO> searchProducts(String keyword, Pageable pageable) {
        return productRepository.findAll(Specification.allOf(nameContains(keyword)).and(shopVerified()), pageable)
                .map(DTOConverter::convertToProductDTO);
    }

    public Page<ProductResponseDTO> getProductByShopId(UUID shopId, Pageable pageable) {
        return productRepository.findAll(Specification.allOf(hasShop(shopId)).and(shopVerified()), pageable)
                .map(DTOConverter::convertToProductDTO);
    }

    public Page<ProductResponseDTO> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice,
            Pageable pageable) {
        return productRepository
                .findAll(Specification.allOf(priceBetween(minPrice.doubleValue(), maxPrice.doubleValue()))
                        .and(shopVerified()), pageable)
                .map(DTOConverter::convertToProductDTO);
    }

    public Page<ProductResponseDTO> getProductsByCategory(UUID categoryId, Pageable pageable) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not Found"));

        return productRepository.findAll(Specification.allOf(hasCategory(categoryId)), pageable)
                .map(DTOConverter::convertToProductDTO);
    }

    public Page<ProductResponseDTO> getProductsByCategory(UUID shopId, UUID categoryId, Pageable pageable) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not Found"));

        return productRepository
                .findAll(Specification.allOf(hasCategory(categoryId).and(hasShop(shopId)).and(shopVerified())),
                        pageable)
                .map(DTOConverter::convertToProductDTO);
    }

}
