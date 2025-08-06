package com.techRestore.tech.restore.services.shop;

import com.techRestore.tech.restore.dto.product.CreateProductDto;
import com.techRestore.tech.restore.dto.product.UpdateProductDto;
import com.techRestore.tech.restore.dto.shop.StockUpdateRequest;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.Product;
import com.techRestore.tech.restore.model.entities.Shop;
import com.techRestore.tech.restore.repository.ProductRepository;
import com.techRestore.tech.restore.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ShopProductService {
    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getProductsByShopId(UUID shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop not found with id: " + shopId));
        return shop.getProducts();
    }

    public Product addProductToShop(UUID shopId, CreateProductDto createProductDto) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop not found with id: " + shopId));

        Product product = new Product();
        product.setShop(shop);
        product.setName(createProductDto.name());
        product.setDescription(createProductDto.description());
        product.setPrice(createProductDto.price());
        product.setImageUrl(createProductDto.imageUrl());
        product.setCategory(createProductDto.category());
        product.setStock(createProductDto.stockQuantity());
        product.setCondition(createProductDto.condition());

        Product savedProduct = productRepository.save(product);

        shop.getProducts().add(savedProduct);

        return savedProduct;
    }

    public void updateProduct(UUID productId, UpdateProductDto updateProductDto) {
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
    }

    public void deleteProduct(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Product not found with id: " + productId);
        }
        productRepository.deleteById(productId);
    }

    public List<Product> getShopProductsWithCategory(UUID shopId, String category) {
        return productRepository.findByCategoryWithShopIdAndCategory(shopId, category);
    }

    public void updateProductStock(UUID shopId, UUID productId, StockUpdateRequest stockUpdateRequest) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop not found with id: " + shopId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));

        if (stockUpdateRequest.newStock() < 0)
            throw new RuntimeException("Invalid input");

        product.setStock(stockUpdateRequest.newStock());
        productRepository.save(product);
    }
}
