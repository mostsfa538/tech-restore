package com.techRestore.tech.restore.common.security.config.batch;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCSV {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String condition;
    private String imageUrl;
    private boolean deleted;
    private String category;
}
