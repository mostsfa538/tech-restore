package com.techRestore.tech.restore.services.adress;

import java.util.UUID;

import com.techRestore.tech.restore.exception.ActivationException;
import com.techRestore.tech.restore.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techRestore.tech.restore.dto.common.address.AddressRequest;
import com.techRestore.tech.restore.dto.common.address.AddressResponse;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.Address;
import com.techRestore.tech.restore.model.entities.User;
import com.techRestore.tech.restore.repository.AddressRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
public class AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user found");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new NotFoundException("User not found: " + email);
        }
        if (!user.isActivate()) {
            throw new ActivationException("User account is deactivated: " + email);
        }

        return user.getId();
    }

    @Transactional
    public AddressResponse addAddress(AddressRequest request) {
        UUID userId = getCurrentUserId();
        if (request.isDefault()) {
            Address existingDefault = addressRepository.findByUserIdAndIsDefaultTrue(userId);
            if (existingDefault != null) {
                existingDefault.setDefault(false);
                addressRepository.save(existingDefault);
            }
        }
        Address address = new Address();
        address.setUser(new User());
        address.getUser().setId(userId);
        address.setState(request.state());
        address.setCity(request.city());
        address.setStreet(request.street());
        address.setBuilding(request.building());
        address.setNotes(request.notes());
        address.setDefault(request.isDefault());
        addressRepository.save(address);
        return mapToAddressResponseDTO(address);
    }

    @Transactional
    public AddressResponse updateAddress(UUID addressId, AddressRequest request) {
        UUID userId = getCurrentUserId();
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Address not found"));
        if (!address.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized: Address does not belong to user");
        }

        if (request.isDefault() && !address.isDefault()) {
            Address existingDefault = addressRepository.findByUserIdAndIsDefaultTrue(userId);
            if (existingDefault != null && !existingDefault.getId().equals(addressId)) {
                existingDefault.setDefault(false);
                addressRepository.save(existingDefault);
            }
        }

        address.setState(request.state());
        address.setCity(request.city());
        address.setStreet(request.street());
        address.setBuilding(request.building());
        address.setNotes(request.notes());
        address.setDefault(request.isDefault());
        addressRepository.save(address);

        return mapToAddressResponseDTO(address);
    }

    @Transactional
    public void deleteAddress(UUID addressId) {
        UUID userId = getCurrentUserId();
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Address not found"));
        if (!address.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized: Address does not belong to user");
        }
        addressRepository.delete(address);
    }

    @Transactional(readOnly = true)
    public Page<AddressResponse> getUserAddresses(Pageable pageable) {
        UUID userId = getCurrentUserId();
        Page<Address> addresses = addressRepository.findByUserId(userId, pageable);
        return addresses.map(this::mapToAddressResponseDTO);
    }

    private AddressResponse mapToAddressResponseDTO(Address address) {
        AddressResponse dto = new AddressResponse();
        dto.setId(address.getId());
        dto.setState(address.getState());
        dto.setCity(address.getCity());
        dto.setStreet(address.getStreet());
        dto.setBuilding(address.getBuilding());
        dto.setNotes(address.getNotes());
        dto.setDefault(address.isDefault());
        dto.setUserId(address.getUser().getId());
        dto.setCreatedAt(address.getCreatedAt());
        return dto;
    }

}
