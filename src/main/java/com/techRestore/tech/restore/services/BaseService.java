package com.techRestore.tech.restore.services;

import com.techRestore.tech.restore.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

@RequiredArgsConstructor
public abstract class BaseService<T, ID> {

    protected final JpaRepository<T, ID> repository;

    /**
     * Generic method to find entity by ID with proper exception handling
     */
    protected T findByIdOrThrow(ID id, String entityName) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(entityName + " not found with id: " + id));
    }

    /**
     * Generic method to find entity by ID from any repository with proper exception
     * handling
     */
    protected <E> E findByIdOrThrow(JpaRepository<E, ID> repo, ID id, String entityName) {
        return repo.findById(id)
                .orElseThrow(() -> new NotFoundException(entityName + " not found with id: " + id));
    }

    /**
     * Generic method to check if entity exists
     */
    protected boolean existsById(ID id) {
        return repository.existsById(id);
    }

    /**
     * Generic method to check if entity exists in any repository
     */
    protected <E> boolean existsById(JpaRepository<E, ID> repo, ID id) {
        return repo.existsById(id);
    }

    /**
     * Generic method to delete entity with existence check
     */
    protected void deleteByIdOrThrow(ID id, String entityName) {
        if (!repository.existsById(id)) {
            throw new NotFoundException(entityName + " not found with id: " + id);
        }
        repository.deleteById(id);
    }

    /**
     * Generic method to delete entity from any repository with existence check
     */
    protected <E> void deleteByIdOrThrow(JpaRepository<E, ID> repo, ID id, String entityName) {
        if (!repo.existsById(id)) {
            throw new NotFoundException(entityName + " not found with id: " + id);
        }
        repo.deleteById(id);
    }
}