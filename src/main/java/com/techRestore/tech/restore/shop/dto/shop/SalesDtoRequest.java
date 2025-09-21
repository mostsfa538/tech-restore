package com.techRestore.tech.restore.shop.dto.shop;

import java.time.LocalDateTime;

public record SalesDtoRequest(LocalDateTime startDate, LocalDateTime endDate) {

}
