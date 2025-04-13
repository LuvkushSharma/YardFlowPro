package com.yardflowpro.repository;

import com.yardflowpro.model.Door;
import com.yardflowpro.model.Dock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoorRepository extends JpaRepository<Door, Long> {
    List<Door> findByDock(Dock dock);
    List<Door> findByStatus(Door.DoorStatus status);
}