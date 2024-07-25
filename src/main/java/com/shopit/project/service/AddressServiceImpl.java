package com.shopit.project.service;

import com.shopit.project.exceptions.APIException;
import com.shopit.project.exceptions.ResourceNotFoundException;
import com.shopit.project.model.Address;
import com.shopit.project.model.User;
import com.shopit.project.payload.AddressDTO;
import com.shopit.project.repository.AddressRepository;
import com.shopit.project.repository.UserRepository;
import com.shopit.project.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AddressServiceImpl implements AddressService {

    private final ModelMapper modelMapper;
    private final AuthUtil authUtil;
    private final UserRepository userRepository;
    AddressRepository addressRepository;

    public AddressServiceImpl(AddressRepository addressRepository, ModelMapper modelMapper, AuthUtil authUtil, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.modelMapper = modelMapper;
        this.authUtil = authUtil;
        this.userRepository = userRepository;
    }

    @Override
    public AddressDTO addAddress(AddressDTO addressDTO) {
        Address address = modelMapper.map(addressDTO, Address.class);
        User user = authUtil.loggedInUser();
        address.setUser(user);
        Address savedAddress = addressRepository.save(address);

        user.getAddresses().add(savedAddress);
        userRepository.save(user);

        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAllAddresses() {
        List<Address> addresses = addressRepository.findAll();
        List<AddressDTO> addressDTOS = addresses.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();

        return addressDTOS;
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "Address id", addressId));

        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAddressByUser() {
        User user = authUtil.loggedInUser();
        List<Address> addresses = addressRepository.findByUser(user);

        if(addresses.isEmpty())
                throw new APIException("User has no address created");

        List<AddressDTO> addressDTOS = addresses.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();

        return addressDTOS;
    }

    @Override
    public AddressDTO updateAddress(AddressDTO addressDTO, Long addressId) {
        Address existingAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "Address id", addressId));

        existingAddress.setAddressLine1(addressDTO.getAddressLine1());
        existingAddress.setAddressLine2(addressDTO.getAddressLine2());
        existingAddress.setCity(addressDTO.getCity());
        existingAddress.setState(addressDTO.getState());
        existingAddress.setPincode(addressDTO.getPincode());
        existingAddress.setCountry(addressDTO.getCountry());

        Address updatedAddress = addressRepository.save(existingAddress);

        User user = authUtil.loggedInUser();
        Address modifiedAddress = new Address();
        List<Address> addresses = user.getAddresses();
        for(Address address: addresses) {
            if(Objects.equals(address.getAddressId(), addressId)) {
                 modifiedAddress = address;
            }
        }

        addresses.remove(modifiedAddress);
        addresses.add(updatedAddress);

        user.setAddresses(addresses);
        userRepository.save(user);

        return modelMapper.map(updatedAddress, AddressDTO.class);
    }

    @Override
    public String deleteAddress(Long addressId) {
        Address existingAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "Address id", addressId));

        addressRepository.delete(existingAddress);

        User user = authUtil.loggedInUser();
        List<Address> addresses = user.getAddresses();
        Address modifiedAddress = new Address();
        for(Address address: addresses) {
            if(Objects.equals(address.getAddressId(), addressId)) {
                modifiedAddress = address;
            }
        }
        addresses.remove(modifiedAddress);
        user.setAddresses(addresses);
        userRepository.save(user);

        return "Address successfully deleted";
    }

}
