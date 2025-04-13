package com.yardflowpro.service;

import com.yardflowpro.dto.CarrierDto;

import java.util.List;

public interface CarrierService {
    CarrierDto createCarrier(CarrierDto carrierDto);
    CarrierDto updateCarrier(Long id, CarrierDto carrierDto);
    CarrierDto getCarrierById(Long id);
    List<CarrierDto> getAllCarriers();
    void deleteCarrier(Long id);
}