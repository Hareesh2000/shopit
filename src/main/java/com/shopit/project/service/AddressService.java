package com.shopit.project.service;

import com.shopit.project.payload.AddressDTO;

import java.util.List;

public interface AddressService {
    AddressDTO addAddress(AddressDTO addressDTO);

    List<AddressDTO> getAllAddresses();

    AddressDTO getAddressById(Long addressId);

    List<AddressDTO> getAddressByUser();

    AddressDTO updateAddress(AddressDTO addressDTO, Long addressId);

    String deleteAddress(Long addressId);
}
