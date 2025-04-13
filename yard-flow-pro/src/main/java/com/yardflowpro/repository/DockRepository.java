package com.yardflowpro.repository;

import com.yardflowpro.model.Dock;
import com.yardflowpro.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DockRepository extends JpaRepository<Dock, Long> {
    List<Dock> findBySite(Site site);
    List<Dock> findByCode(String code);
}