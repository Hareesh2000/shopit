package com.shopit.project.service;

import com.shopit.project.model.Address;
import com.shopit.project.payload.AddressDTO;
import com.shopit.project.repository.AddressRepository;
import com.shopit.project.repository.UserRepository;
import com.shopit.project.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import com.shopit.project.model.User;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @InjectMocks
    private AddressServiceImpl addressService;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AuthUtil authUtil;

    private User user;
    private Address address;
    private AddressDTO addressDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setAddresses(new ArrayList<>());

        address = new Address();
        address.setAddressId(1L);
        address.setUser(user);

        addressDTO = new AddressDTO();
        addressDTO.setAddressLine1("123 Main St");
    }

    @Test
    void addAddress_shouldReturnSavedAddressDTO() {
        when(authUtil.loggedInUser()).thenReturn(user);
        when(modelMapper.map(any(AddressDTO.class), eq(Address.class))).thenReturn(address);
        when(modelMapper.map(any(Address.class), eq(AddressDTO.class))).thenReturn(addressDTO);
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(userRepository.save(any(User.class))).thenReturn(user);

        AddressDTO result = addressService.addAddress(addressDTO);

        verify(addressRepository, times(1)).save(any(Address.class));
        verify(userRepository, times(1)).save(any(User.class));
        assertNotNull(result);
        assertEquals(addressDTO.getAddressLine1(), result.getAddressLine1());
    }

    @Test
    void getAllAddresses_shouldReturnListOfAddressDTOs() {
        List<Address> addresses = new ArrayList<>();
        addresses.add(address);

        when(modelMapper.map(any(Address.class), eq(AddressDTO.class))).thenReturn(addressDTO);
        when(addressRepository.findAll()).thenReturn(addresses);

        List<AddressDTO> result = addressService.getAllAddresses();

        verify(addressRepository, times(1)).findAll();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAddressById_shouldReturnAddressDTO() {
        when(modelMapper.map(any(Address.class), eq(AddressDTO.class))).thenReturn(addressDTO);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));

        AddressDTO result = addressService.getAddressById(1L);

        verify(addressRepository, times(1)).findById(anyLong());
        assertNotNull(result);
        assertEquals(addressDTO.getAddressLine1(), result.getAddressLine1());
    }

    @Test
    void getAddressByUser_shouldReturnListOfAddressDTOs() {
        List<Address> addresses = new ArrayList<>();
        addresses.add(address);

        when(authUtil.loggedInUser()).thenReturn(user);
        when(modelMapper.map(any(Address.class), eq(AddressDTO.class))).thenReturn(addressDTO);
        when(addressRepository.findByUser(any(User.class))).thenReturn(addresses);

        List<AddressDTO> result = addressService.getAddressByUser();

        verify(addressRepository, times(1)).findByUser(any(User.class));
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void updateAddress_shouldReturnUpdatedAddressDTO() {
        when(authUtil.loggedInUser()).thenReturn(user);
        when(modelMapper.map(any(Address.class), eq(AddressDTO.class))).thenReturn(addressDTO);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(userRepository.save(any(User.class))).thenReturn(user);

        AddressDTO result = addressService.updateAddress(addressDTO, 1L);

        verify(addressRepository, times(1)).findById(anyLong());
        verify(addressRepository, times(1)).save(any(Address.class));
        verify(userRepository, times(1)).save(any(User.class));
        assertNotNull(result);
    }

    @Test
    void deleteAddress_shouldReturnSuccessMessage() {
        when(authUtil.loggedInUser()).thenReturn(user);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));

        String result = addressService.deleteAddress(1L);

        verify(addressRepository, times(1)).findById(anyLong());
        verify(addressRepository, times(1)).delete(any(Address.class));
        verify(userRepository, times(1)).save(any(User.class));
        assertEquals("Address successfully deleted", result);
    }
}