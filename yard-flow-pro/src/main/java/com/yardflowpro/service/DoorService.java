package com.yardflowpro.service;

import com.yardflowpro.dto.DoorDto;
import com.yardflowpro.model.Door;

import java.util.List;

public interface DoorService {
    Door createDoor(DoorDto doorDto);
    Door getDoorById(Long id);
    Door updateDoor(Long id, DoorDto doorDto);
    void deleteDoor(Long id);
    List<Door> getAllDoors();
    List<Door> getDoorsByDock(Long dockId);
    List<Door> getDoorsByStatus(Door.DoorStatus status);
    Door updateDoorStatus(Long id, Door.DoorStatus status);
}