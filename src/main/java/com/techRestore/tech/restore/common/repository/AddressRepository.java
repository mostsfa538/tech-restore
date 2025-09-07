package com.techRestore.tech.restore.common.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techRestore.tech.restore.common.model.entities.Address;

import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {
    Page<Address> findByUserId(UUID userId, Pageable pageable);

    Address findByUserIdAndIsDefaultTrue(UUID userId);

}
