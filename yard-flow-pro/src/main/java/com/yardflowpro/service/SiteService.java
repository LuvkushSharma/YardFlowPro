package com.yardflowpro.service;

import com.yardflowpro.dto.SiteDto;

import java.util.List;

public interface SiteService {
    SiteDto createSite(SiteDto siteDto);
    SiteDto updateSite(Long id, SiteDto siteDto);
    SiteDto getSiteById(Long id);
    List<SiteDto> getAllSites();
    void deleteSite(Long id);
}