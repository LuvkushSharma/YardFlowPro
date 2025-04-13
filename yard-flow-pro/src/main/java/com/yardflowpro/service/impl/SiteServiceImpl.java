package com.yardflowpro.service.impl;

import com.yardflowpro.dto.SiteDto;
import com.yardflowpro.exception.ResourceNotFoundException;
import com.yardflowpro.model.Site;
import com.yardflowpro.repository.SiteRepository;
import com.yardflowpro.service.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SiteServiceImpl implements SiteService {

    private final SiteRepository siteRepository;

    @Autowired
    public SiteServiceImpl(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @Override
    public SiteDto createSite(SiteDto siteDto) {
        Site site = new Site();
        site.setName(siteDto.getName());
        site.setCode(siteDto.getCode());
        site.setAddress(siteDto.getAddress());
        site.setCity(siteDto.getCity());
        site.setState(siteDto.getState());
        site.setZipCode(siteDto.getZipCode());
        
        Site savedSite = siteRepository.save(site);
        return convertToDto(savedSite);
    }

    @Override
    public SiteDto updateSite(Long id, SiteDto siteDto) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + id));
        
        site.setName(siteDto.getName());
        site.setCode(siteDto.getCode());
        site.setAddress(siteDto.getAddress());
        site.setCity(siteDto.getCity());
        site.setState(siteDto.getState());
        site.setZipCode(siteDto.getZipCode());
        
        Site updatedSite = siteRepository.save(site);
        return convertToDto(updatedSite);
    }

    @Override
    public SiteDto getSiteById(Long id) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + id));
        return convertToDto(site);
    }

    @Override
    public List<SiteDto> getAllSites() {
        return siteRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSite(Long id) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + id));
        siteRepository.delete(site);
    }
    
    private SiteDto convertToDto(Site site) {
        SiteDto dto = new SiteDto();
        dto.setId(site.getId());
        dto.setName(site.getName());
        dto.setCode(site.getCode());
        dto.setAddress(site.getAddress());
        dto.setCity(site.getCity());
        dto.setState(site.getState());
        dto.setZipCode(site.getZipCode());
        return dto;
    }
}