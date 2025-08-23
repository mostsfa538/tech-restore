package com.techRestore.tech.restore.repository;

import com.techRestore.tech.restore.model.entities.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {
    Page<Address> findByUserId(UUID userId,Pageable pageable);
    Address findByUserIdAndIsDefaultTrue(UUID userId);

}
