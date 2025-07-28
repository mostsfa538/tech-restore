package com.techRestore.tech.restore.repository;

import com.techRestore.tech.restore.model.User;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, UUID> {
    User findByEmail(String email);
    boolean existsByEmail(String email);
}
