package com.techRestore.tech.restore.common.security.config.batch;

import java.util.UUID;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.techRestore.tech.restore.admin.repository.CategoryRepository;
import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.common.model.entities.Category;
import com.techRestore.tech.restore.common.model.entities.Product;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.model.enums.ProductCondition;
import com.techRestore.tech.restore.user.service.AuthUtil;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductItemProcessor implements ItemProcessor<ProductCSV, Product> {

  private final AuthUtil authUtil;
  private final CategoryRepository categoryRepository;

  @Override
  public Product process(ProductCSV productCSV) throws Exception {
      Product product = new Product();
      Shop shop = authUtil.getCurrentShop();
      UUID shopId=shop.getId();
      if(shopId==null){
          throw new IllegalStateException("Shop ID is null");
      }
      product.setShopId(shopId);
      product.setName(productCSV.getName());
      product.setDescription(productCSV.getDescription());
      product.setPrice(productCSV.getPrice());
      product.setStock(productCSV.getStock());
      product.setImageUrl(productCSV.getImageUrl());
      product.setDeleted(productCSV.isDeleted());
      if(productCSV.getCondition()!=null){
        product.setCondition(ProductCondition.valueOf(productCSV.getCondition()));  
      }
      if (productCSV.getCategory() != null) {
        Category category=categoryRepository.findByName(productCSV.getCategory()).orElseThrow(()->new NotFoundException("category wasn't found"));
        product.setCategory(category);    
      }

      return product;
  }

}
