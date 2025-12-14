package com.techRestore.tech.restore.common.repository;

import com.techRestore.tech.restore.common.model.entities.ChatSession;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.model.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {

    @Query("SELECT cs FROM ChatSession cs WHERE cs.user = :user AND cs.shop = :shop AND cs.isActive = true")
    Optional<ChatSession> findActiveSessionBetweenUserAndShop(@Param("user") User user, @Param("shop") Shop shop);

    @Query("SELECT cs FROM ChatSession cs WHERE cs.user.id = :userId AND cs.isActive = true")
    List<ChatSession> findActiveSessionsByUserId(@Param("userId") UUID userId);

    @Query("SELECT cs FROM ChatSession cs WHERE cs.shop.id = :shopId AND cs.isActive = true")
    List<ChatSession> findActiveSessionsByShopId(@Param("shopId") UUID shopId);

    @Query("SELECT cs FROM ChatSession cs WHERE cs.user.id = :userId AND cs.shop.id = :shopId")
    Optional<ChatSession> findByUserIdAndShopId(@Param("userId") UUID userId, @Param("shopId") UUID shopId);

    @Query("SELECT cs FROM ChatSession cs WHERE cs.user.id = :userId AND cs.isActive = true")
    List<ChatSession> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT cs FROM ChatSession cs WHERE cs.shop.id = :shopId AND cs.isActive = true")
    List<ChatSession> findByShopId(@Param("shopId") UUID shopId);
}
