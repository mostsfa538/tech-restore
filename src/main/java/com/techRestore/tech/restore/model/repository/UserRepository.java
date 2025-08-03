package com.techRestore.tech.restore.model.repository;

import com.techRestore.tech.restore.model.entities.User;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, UUID> {
    User findByEmail(String email);
    boolean existsByEmail(String email);
}
