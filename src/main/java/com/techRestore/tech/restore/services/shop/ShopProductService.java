package com.techRestore.tech.restore.services.shop;

import com.techRestore.tech.restore.dto.product.CreateProductDto;
import com.techRestore.tech.restore.dto.product.ProductResponseDTO;
import com.techRestore.tech.restore.dto.product.UpdateProductDto;
import com.techRestore.tech.restore.dto.shop.StockUpdateRequest;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.Category;
import com.techRestore.tech.restore.model.entities.Product;
import com.techRestore.tech.restore.model.entities.Shop;
import com.techRestore.tech.restore.repository.CategoryRepository;
import com.techRestore.tech.restore.repository.ProductRepository;
import com.techRestore.tech.restore.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ShopProductService {
    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;


        private UUID getCurrentShopId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String email = authentication.getName();
      Shop shop = shopRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Shop not found"));

      if (shop == null) {
          throw new RuntimeException("User not found with email: " + email);
      }
      return shop.getId();
    }

    public List<ProductResponseDTO> getProductsByShopId() {
        UUID shopId = getCurrentShopId();
        List<Product> products = productRepository.findByShopId(shopId);
        return products.stream()
                       .map(this::convertDto)
                       .toList();
    }

    public ProductResponseDTO addProductToShop(CreateProductDto createProductDto) {
        UUID shopId = getCurrentShopId();
        shopRepository.findById(shopId)
            .orElseThrow(() -> new NotFoundException("Shop not found with id: " + shopId));

        Product product = new Product();
        product.setShopId(shopId);
        product.setName(createProductDto.name());
        product.setDescription(createProductDto.description());
        product.setPrice(createProductDto.price());
        product.setImageUrl(createProductDto.imageUrl());
        product.setStock(createProductDto.stockQuantity());
        product.setCondition(createProductDto.condition());

        if (createProductDto.category() != null) {
            Category category;
            if (createProductDto.category().getId() != null) {
                category = categoryRepository.findById(createProductDto.category().getId())
                        .orElseThrow(() -> new NotFoundException("Category not found"));
            } else {
                throw new RuntimeException("Category must have either ID or name");
            }

            product.setCategory(category);
        }

        productRepository.save(product);
        return convertDto(product);
    }


    public ProductResponseDTO updateProduct(UUID productId, UpdateProductDto updateProductDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));

        if (updateProductDto.name() != null) {
            product.setName(updateProductDto.name());
        }
        if (updateProductDto.description() != null) {
            product.setDescription(updateProductDto.description());
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

        productRepository.save(product);

        return convertDto(product);
    }

    public void deleteProduct(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Product not found with id: " + productId);
        }
        productRepository.deleteById(productId);
    }

    public ProductResponseDTO updateProductStock(UUID productId, StockUpdateRequest stockUpdateRequest) {
        getCurrentShopId();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));

        if (stockUpdateRequest.newStock() < 0)
            throw new RuntimeException("Invalid input");

        product.setStock(stockUpdateRequest.newStock());
        productRepository.save(product);

        return convertDto(product);
    }

    private ProductResponseDTO convertDto(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getImageUrl(),
                product.getCondition(),
                product.getCreatedAt(),
                product.getCategory() != null ? product.getCategory().getName() : null
        );
    }
}