package com.techRestore.tech.restore.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techRestore.tech.restore.common.model.entities.RepairRequest;
import com.techRestore.tech.restore.common.model.enums.RepairStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepairRequestRepository extends JpaRepository<RepairRequest, UUID> {

    Page<RepairRequest> findByStatus(RepairStatus status, Pageable pageable);

    Page<RepairRequest> getAllRepairRequestByUserId(UUID userId, Pageable pageable);

    Page<RepairRequest> findAllByShopId(UUID shopId, Pageable pageable);

    Optional<RepairRequest> findByIdAndShopId(UUID id, UUID shopId);

    Page<RepairRequest> findByShopIdAndStatus(UUID shopId, RepairStatus status, Pageable pageable);

    Page<RepairRequest> findByStatusInAndDeliveryIdIsNull(List<RepairStatus> statuses, Pageable pageable);

    Page<RepairRequest> findByDeliveryId(UUID deliveryId, Pageable pageable);

}
