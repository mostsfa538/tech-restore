package com.techRestore.tech.restore.common.interfaces;

public interface RegistrationStrategy<T, R> {
    /**
     * Creates and configures the entity from registration data
     * 
     * @param registrationData the registration request data
     * @return the created entity
     */
    T createEntity(R registrationData);

    /**
     * Saves the entity to the database
     * 
     * @param entity the entity to save
     * @return the saved entity
     */
    T saveEntity(T entity);

    /**
     * Gets the email from the registration data
     * 
     * @param registrationData the registration request data
     * @return the email address
     */
    String getEmail(R registrationData);

    /**
     * Gets the success message for this registration type
     * 
     * @param entity the saved entity
     * @return success message or entity ID
     */
    String getSuccessMessage(T entity);
}
