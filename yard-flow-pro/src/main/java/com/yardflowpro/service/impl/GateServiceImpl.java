package com.yardflowpro.service.impl;

import com.yardflowpro.dto.GateDto;
import com.yardflowpro.exception.ResourceNotFoundException;
import com.yardflowpro.model.Gate;
import com.yardflowpro.model.Site;
import com.yardflowpro.repository.GateRepository;
import com.yardflowpro.repository.SiteRepository;
import com.yardflowpro.service.GateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GateServiceImpl implements GateService {

    private final GateRepository gateRepository;
    private final SiteRepository siteRepository;

    @Autowired
    public GateServiceImpl(GateRepository gateRepository, SiteRepository siteRepository) {
        this.gateRepository = gateRepository;
        this.siteRepository = siteRepository;
    }

    @Override
    @Transactional
    public Gate createGate(GateDto gateDto) {
        Site site = siteRepository.findById(gateDto.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + gateDto.getSiteId()));

        Gate gate = new Gate();
        gate.setName(gateDto.getName());
        gate.setCode(gateDto.getCode());
        gate.setFunction(gateDto.getFunction());
        gate.setSite(site);

        return gateRepository.save(gate);
    }

    @Override
    public Gate getGateById(Long id) {
        return gateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gate not found with id: " + id));
    }

    @Override
    public List<Gate> getAllGates() {
        return gateRepository.findAll();
    }

    @Override
    public List<Gate> getGatesBySite(Long siteId) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));
        
        return gateRepository.findBySite(site);
    }

    @Override
    public List<Gate> getGatesByFunction(Gate.GateFunction function) {
        return gateRepository.findByFunction(function);
    }

    @Override
    public List<Gate> getGatesBySiteAndFunction(Long siteId, Gate.GateFunction function) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));
        
        return gateRepository.findBySiteAndFunction(site, function);
    }

    @Override
    @Transactional
    public Gate updateGate(Long id, GateDto gateDto) {
        Gate gate = gateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gate not found with id: " + id));
        
        Site site = siteRepository.findById(gateDto.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + gateDto.getSiteId()));

        gate.setName(gateDto.getName());
        gate.setCode(gateDto.getCode());
        gate.setFunction(gateDto.getFunction());
        gate.setSite(site);

        return gateRepository.save(gate);
    }

    @Override
    @Transactional
    public void deleteGate(Long id) {
        Gate gate = gateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gate not found with id: " + id));
        
        gateRepository.delete(gate);
    }
}