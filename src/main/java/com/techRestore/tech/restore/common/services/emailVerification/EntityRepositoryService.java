package com.techRestore.tech.restore.common.services.emailVerification;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techRestore.tech.restore.common.interfaces.OtpVerifiable;
import com.techRestore.tech.restore.common.model.entities.Assigner;
import com.techRestore.tech.restore.common.model.entities.Delivery;
import com.techRestore.tech.restore.common.model.entities.Shop;
import com.techRestore.tech.restore.common.model.entities.User;
import com.techRestore.tech.restore.assigners.repository.AssignerRepository;
import com.techRestore.tech.restore.common.exception.NotFoundException;
import com.techRestore.tech.restore.user.repository.UserRepository;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.delivery.repository.DeliveryRepository;

@Service
public class EntityRepositoryService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private AssignerRepository assignerRepository;

    public OtpVerifiable findByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            return user;
        }

        Optional<Shop> shopOpt = shopRepository.findByEmail(email);
        if (shopOpt.isPresent()) {
            return shopOpt.get();
        }

        Optional<Delivery> deliveryOpt = deliveryRepository.findByEmail(email);
        if (deliveryOpt.isPresent()) {
            return deliveryOpt.get();
        }

        Optional<Assigner> assignerOpt = assignerRepository.findByEmail(email);
        if (assignerOpt.isPresent()) {
            return assignerOpt.get();
        }

        throw new NotFoundException("Email not found: " + email);
    }

    public void save(OtpVerifiable entity) {

        if (entity instanceof User) {
            userRepository.save((User) entity);
        } else if (entity instanceof Shop) {
            shopRepository.save((Shop) entity);
        } else if (entity instanceof Delivery) {
            deliveryRepository.save((Delivery) entity);
        } else if (entity instanceof Assigner) {
            assignerRepository.save((Assigner) entity);
        }else {
            throw new IllegalArgumentException("Unknown entity type: " + entity.getClass().getSimpleName());
        }
    }
}