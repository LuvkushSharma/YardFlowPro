package com.yardflowpro.service;

import com.yardflowpro.dto.DockDto;
import com.yardflowpro.model.Dock;

import java.util.List;

public interface DockService {
    Dock createDock(DockDto dockDto);
    Dock getDockById(Long id);
    Dock updateDock(Long id, DockDto dockDto);
    void deleteDock(Long id);
    List<Dock> getAllDocks();
    List<Dock> getDocksBySite(Long siteId);
}