package com.myshopycart.authservice.service;

import com.myshopycart.authservice.dto.UserAddressRequest;
import com.myshopycart.authservice.dto.UserAddressResponse;
import com.myshopycart.authservice.entity.UserAddress;
import com.myshopycart.authservice.repository.UserAddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserAddressService {

    private final UserAddressRepository userAddressRepository;

    public UserAddressService(
            UserAddressRepository userAddressRepository) {
        this.userAddressRepository = userAddressRepository;
    }

    @Transactional
    public UserAddressResponse createAddress(
            Long userId,
            UserAddressRequest request) {

        validateRequest(request);

        if (request.isDefaultAddress()) {
            clearDefaultAddress(userId);
        }

        UserAddress address = new UserAddress();

        address.setUserId(userId);
        address.setAddressType(request.getAddressType());
        address.setAddress(request.getAddress());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setDefaultAddress(request.isDefaultAddress());

        UserAddress savedAddress =
                userAddressRepository.save(address);

        return toResponse(savedAddress);
    }

    @Transactional(readOnly = true)
    public List<UserAddressResponse> getAddresses(Long userId) {

        return userAddressRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserAddressResponse getAddress(
            Long userId,
            Long addressId) {

        UserAddress address = userAddressRepository
                .findByIdAndUserId(addressId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Address not found"));

        return toResponse(address);
    }

    @Transactional
    public UserAddressResponse updateAddress(
            Long userId,
            Long addressId,
            UserAddressRequest request) {

        validateRequest(request);

        UserAddress address = userAddressRepository
                .findByIdAndUserId(addressId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Address not found"));

        if (request.isDefaultAddress()) {
            clearDefaultAddress(userId);
        }

        address.setAddressType(request.getAddressType());
        address.setAddress(request.getAddress());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setDefaultAddress(request.isDefaultAddress());

        UserAddress updatedAddress =
                userAddressRepository.save(address);

        return toResponse(updatedAddress);
    }

    @Transactional
    public void deleteAddress(
            Long userId,
            Long addressId) {

        UserAddress address = userAddressRepository
                .findByIdAndUserId(addressId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Address not found"));

        userAddressRepository.delete(address);
    }

    private void clearDefaultAddress(Long userId) {

        List<UserAddress> addresses =
                userAddressRepository.findByUserId(userId);

        for (UserAddress address : addresses) {
            if (address.isDefaultAddress()) {
                address.setDefaultAddress(false);
            }
        }

        userAddressRepository.saveAll(addresses);
    }

    private void validateRequest(UserAddressRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Address request must not be null");
        }

        if (isBlank(request.getAddressType())) {
            throw new IllegalArgumentException(
                    "Address type is required");
        }

        if (isBlank(request.getAddress())) {
            throw new IllegalArgumentException(
                    "Address is required");
        }

        if (isBlank(request.getCity())) {
            throw new IllegalArgumentException(
                    "City is required");
        }

        if (isBlank(request.getState())) {
            throw new IllegalArgumentException(
                    "State is required");
        }

        if (isBlank(request.getPostalCode())) {
            throw new IllegalArgumentException(
                    "Postal code is required");
        }

        if (isBlank(request.getCountry())) {
            throw new IllegalArgumentException(
                    "Country is required");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private UserAddressResponse toResponse(
            UserAddress address) {

        UserAddressResponse response =
                new UserAddressResponse();

        response.setId(address.getId());
        response.setUserId(address.getUserId());
        response.setAddressType(address.getAddressType());
        response.setAddress(address.getAddress());
        response.setCity(address.getCity());
        response.setState(address.getState());
        response.setPostalCode(address.getPostalCode());
        response.setCountry(address.getCountry());
        response.setDefaultAddress(address.isDefaultAddress());

        return response;
    }
}