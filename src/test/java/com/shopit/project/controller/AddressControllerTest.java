package com.shopit.project.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopit.project.payload.AddressDTO;
import com.shopit.project.service.AddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class AddressControllerTest {

    @Mock
    private AddressService addressService;

    @InjectMocks
    private AddressController addressController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper; // For converting objects to JSON

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(addressController).build();
    }

    @Test
    void testAddAddress() throws Exception {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressLine1("123 Main St");
        addressDTO.setAddressLine2("Anytown");
        addressDTO.setPincode("12345");

        when(addressService.addAddress(any(AddressDTO.class))).thenReturn(addressDTO);

        mockMvc.perform(post("/api/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addressDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.addressLine1").value("123 Main St"))
                .andExpect(jsonPath("$.addressLine2").value("Anytown"))
                .andExpect(jsonPath("$.pincode").value("12345"));

        verify(addressService, times(1)).addAddress(any(AddressDTO.class));
    }

    @Test
    void testGetAllAddresses() throws Exception {
        List<AddressDTO> addressList = new ArrayList<>();
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressLine1("123 Main St");
        addressDTO.setAddressLine2("Anytown");
        addressDTO.setPincode("12345");
        addressList.add(addressDTO);

        when(addressService.getAllAddresses()).thenReturn(addressList);

        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].addressLine1").value("123 Main St"))
                .andExpect(jsonPath("$[0].addressLine2").value("Anytown"))
                .andExpect(jsonPath("$[0].pincode").value("12345"));

        verify(addressService, times(1)).getAllAddresses();
    }

    @Test
    void testGetAddressById() throws Exception {
        Long addressId = 1L;
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressLine1("123 Main St");
        addressDTO.setAddressLine2("Anytown");
        addressDTO.setPincode("12345");

        when(addressService.getAddressById(addressId)).thenReturn(addressDTO);

        mockMvc.perform(get("/api/addresses/{addressId}", addressId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addressLine1").value("123 Main St"))
                .andExpect(jsonPath("$.addressLine2").value("Anytown"))
                .andExpect(jsonPath("$.pincode").value("12345"));

        verify(addressService, times(1)).getAddressById(addressId);
    }

    @Test
    void testGetAddressByUser() throws Exception {
        List<AddressDTO> addressList = new ArrayList<>();
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressLine1("123 Main St");
        addressDTO.setAddressLine2("Anytown");
        addressDTO.setPincode("12345");
        addressList.add(addressDTO);

        when(addressService.getAddressByUser()).thenReturn(addressList);

        mockMvc.perform(get("/api/addresses/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].addressLine1").value("123 Main St"))
                .andExpect(jsonPath("$[0].addressLine2").value("Anytown"))
                .andExpect(jsonPath("$[0].pincode").value("12345"));

        verify(addressService, times(1)).getAddressByUser();
    }

    @Test
    void testUpdateAddress() throws Exception {
        Long addressId = 1L;
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressLine1("123 Main St");
        addressDTO.setAddressLine2("Anytown");
        addressDTO.setPincode("12345");

        when(addressService.updateAddress(any(AddressDTO.class), eq(addressId))).thenReturn(addressDTO);

        mockMvc.perform(put("/api/addresses/{addressId}", addressId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addressDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addressLine1").value("123 Main St"))
                .andExpect(jsonPath("$.addressLine2").value("Anytown"))
                .andExpect(jsonPath("$.pincode").value("12345"));

        verify(addressService, times(1)).updateAddress(any(AddressDTO.class), eq(addressId));
    }

    @Test
    void testDeleteAddress() throws Exception {
        Long addressId = 1L;
        String expectedResponse = "Address deleted successfully";

        when(addressService.deleteAddress(addressId)).thenReturn(expectedResponse);

        mockMvc.perform(delete("/api/addresses/{addressId}", addressId))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));

        verify(addressService, times(1)).deleteAddress(addressId);
    }
}