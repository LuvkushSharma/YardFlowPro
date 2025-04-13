package com.yardflowpro.service.impl;

import com.yardflowpro.dto.DockDto;
import com.yardflowpro.exception.ResourceNotFoundException;
import com.yardflowpro.model.Dock;
import com.yardflowpro.model.Site;
import com.yardflowpro.repository.DockRepository;
import com.yardflowpro.repository.DoorRepository;
import com.yardflowpro.repository.SiteRepository;
import com.yardflowpro.service.DockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DockServiceImpl implements DockService {

    private final DockRepository dockRepository;
    private final SiteRepository siteRepository;
   

    @Autowired
    public DockServiceImpl(DockRepository dockRepository, SiteRepository siteRepository, DoorRepository doorRepository) {
        this.dockRepository = dockRepository;
        this.siteRepository = siteRepository;
    }

    @Override
    @Transactional
    public Dock createDock(DockDto dockDto) {
        Site site = siteRepository.findById(dockDto.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + dockDto.getSiteId()));

        Dock dock = new Dock();
        dock.setName(dockDto.getName());
        dock.setCode(dockDto.getCode());
        dock.setSite(site);

        return dockRepository.save(dock);
    }

    @Override
    public Dock getDockById(Long id) {
        return dockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dock not found with id: " + id));
    }

    @Override
    @Transactional
    public Dock updateDock(Long id, DockDto dockDto) {
        Dock dock = dockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dock not found with id: " + id));

        Site site = siteRepository.findById(dockDto.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + dockDto.getSiteId()));

        dock.setName(dockDto.getName());
        dock.setCode(dockDto.getCode());
        dock.setSite(site);

        return dockRepository.save(dock);
    }

    @Override
    @Transactional
    public void deleteDock(Long id) {
        Dock dock = dockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dock not found with id: " + id));

        // Check if dock has any doors
        if (!dock.getDoors().isEmpty()) {
            throw new IllegalStateException("Cannot delete dock with associated doors");
        }

        dockRepository.delete(dock);
    }

    @Override
    public List<Dock> getAllDocks() {
        return dockRepository.findAll();
    }

    @Override
    public List<Dock> getDocksBySite(Long siteId) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));

        return dockRepository.findBySite(site);
    }
}