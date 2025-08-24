package com.techRestore.tech.restore.services.adress;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techRestore.tech.restore.dto.adress.AddressRequestDTO;
import com.techRestore.tech.restore.dto.adress.AddressResponseDTO;
import com.techRestore.tech.restore.exception.NotFoundException;
import com.techRestore.tech.restore.model.entities.Address;
import com.techRestore.tech.restore.model.entities.User;
import com.techRestore.tech.restore.repository.AddressRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AdressService {
    private final AddressRepository adressRepository;

    @Transactional
    public AddressResponseDTO addAdress(UUID userId, AddressRequestDTO request) {
        if (request.isDefault()) {
            Address existingDefault = adressRepository.findByUserIdAndIsDefaultTrue(userId);
            if (existingDefault != null) {
                existingDefault.setDefault(false);
                adressRepository.save(existingDefault);
            }
        }
        Address address = new Address();
        address.setUser(new User());
        address.getUser().setId(userId);
        address.setState(request.getState());
        address.setCity(request.getCity());
        address.setStreet(request.getStreet());
        address.setBuilding(request.getBuilding());
        address.setNotes(request.getNotes());
        address.setDefault(request.isDefault());
        adressRepository.save(address);
        return mapToAddressResponseDTO(address);
    }

    @Transactional
    public AddressResponseDTO updateAddress(UUID userId, UUID addressId, AddressRequestDTO request) {
        Address address = adressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Address not found"));
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Address does not belong to user");
        }

        if (request.isDefault() && !address.isDefault()) {
            Address existingDefault = adressRepository.findByUserIdAndIsDefaultTrue(userId);
            if (existingDefault != null && !existingDefault.getId().equals(addressId)) {
                existingDefault.setDefault(false);
                adressRepository.save(existingDefault);
            }
        }

        address.setState(request.getState());
        address.setCity(request.getCity());
        address.setStreet(request.getStreet());
        address.setBuilding(request.getBuilding());
        address.setNotes(request.getNotes());
        address.setDefault(request.isDefault());
        adressRepository.save(address);

        return mapToAddressResponseDTO(address);
    }

    @Transactional
    public void deleteAddress(UUID userId, UUID addressId) {
        Address address = adressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Address not found"));
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Address does not belong to user");
        }
        adressRepository.delete(address);
    }

    @Transactional(readOnly = true)
    public Page<AddressResponseDTO> getUserAddresses(UUID userId, Pageable pageable) {
        Page<Address> addresses = adressRepository.findByUserId(userId, pageable);
        return addresses.map(this::mapToAddressResponseDTO);
    }

    private AddressResponseDTO mapToAddressResponseDTO(Address address) {
        AddressResponseDTO dto = new AddressResponseDTO();
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
