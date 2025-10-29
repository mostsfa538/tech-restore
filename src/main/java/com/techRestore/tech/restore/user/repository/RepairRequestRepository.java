package com.techRestore.tech.restore.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    long countByDeliveryIdAndStatusIn(UUID deliveryId, List<RepairStatus> statuses);

    @Query("SELECT r FROM RepairRequest r WHERE r.deliveryId = :deliveryId AND r.status IN :statuses")
    List<RepairRequest> findByDeliveryIdAndStatusIn(@Param("deliveryId") UUID deliveryId,
            @Param("statuses") List<RepairStatus> statuses);

    @Query("SELECT r.deliveryId, r.status, COUNT(r) FROM RepairRequest r WHERE r.deliveryId IN :deliveryIds GROUP BY r.deliveryId, r.status")
    List<Object[]> countByDeliveryIdsGroupedByStatus(@Param("deliveryIds") List<UUID> deliveryIds);

    @Modifying
    @Query("""
                UPDATE RepairRequest r
                SET r.deliveryId = :deliveryId,
                    r.status = :newStatus
                WHERE r.id = :id
                  AND r.status = :expectedStatus
                  AND r.deliveryId IS NULL
            """)
    int assignRepairIfAvailable(
            @Param("id") UUID id,
            @Param("deliveryId") UUID deliveryId,
            @Param("newStatus") RepairStatus newStatus,
            @Param("expectedStatus") RepairStatus expectedStatus);

}
