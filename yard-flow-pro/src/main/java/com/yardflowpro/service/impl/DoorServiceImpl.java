package com.yardflowpro.service.impl;

import com.yardflowpro.dto.DoorDto;
import com.yardflowpro.exception.ResourceNotFoundException;
import com.yardflowpro.model.Door;
import com.yardflowpro.model.Dock;
import com.yardflowpro.repository.DockRepository;
import com.yardflowpro.repository.DoorRepository;
import com.yardflowpro.service.DoorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DoorServiceImpl implements DoorService {

    private final DoorRepository doorRepository;
    private final DockRepository dockRepository;

    @Autowired
    public DoorServiceImpl(DoorRepository doorRepository, DockRepository dockRepository) {
        this.doorRepository = doorRepository;
        this.dockRepository = dockRepository;
    }

    @Override
    @Transactional
    public Door createDoor(DoorDto doorDto) {
        Dock dock = dockRepository.findById(doorDto.getDockId())
                .orElseThrow(() -> new ResourceNotFoundException("Dock not found with id: " + doorDto.getDockId()));

        Door door = new Door();
        door.setName(doorDto.getName());
        door.setCode(doorDto.getCode());
        door.setStatus(Door.DoorStatus.AVAILABLE);
        door.setDock(dock);

        return doorRepository.save(door);
    }

    @Override
    public Door getDoorById(Long id) {
        return doorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Door not found with id: " + id));
    }

    @Override
    @Transactional
    public Door updateDoor(Long id, DoorDto doorDto) {
        Door door = doorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Door not found with id: " + id));

        Dock dock = dockRepository.findById(doorDto.getDockId())
                .orElseThrow(() -> new ResourceNotFoundException("Dock not found with id: " + doorDto.getDockId()));

        door.setName(doorDto.getName());
        door.setCode(doorDto.getCode());
        door.setDock(dock);

        return doorRepository.save(door);
    }

    @Override
    @Transactional
    public void deleteDoor(Long id) {
        Door door = doorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Door not found with id: " + id));

        if (door.getCurrentTrailer() != null) {
            throw new IllegalStateException("Cannot delete door with assigned trailer");
        }

        doorRepository.delete(door);
    }

    @Override
    public List<Door> getAllDoors() {
        return doorRepository.findAll();
    }

    @Override
    public List<Door> getDoorsByDock(Long dockId) {
        Dock dock = dockRepository.findById(dockId)
                .orElseThrow(() -> new ResourceNotFoundException("Dock not found with id: " + dockId));

        return doorRepository.findByDock(dock);
    }

    @Override
    public List<Door> getDoorsByStatus(Door.DoorStatus status) {
        return doorRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public Door updateDoorStatus(Long id, Door.DoorStatus status) {
        Door door = doorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Door not found with id: " + id));

        // Validate status change
        if (status == Door.DoorStatus.AVAILABLE && door.getCurrentTrailer() != null) {
            throw new IllegalStateException("Cannot set status to AVAILABLE when door has an assigned trailer");
        }

        door.setStatus(status);
        return doorRepository.save(door);
    }
}