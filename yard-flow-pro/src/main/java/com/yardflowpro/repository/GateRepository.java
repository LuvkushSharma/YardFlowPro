package com.yardflowpro.repository;

import com.yardflowpro.model.Gate;
import com.yardflowpro.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GateRepository extends JpaRepository<Gate, Long> {
    List<Gate> findBySite(Site site);
    Optional<Gate> findByCode(String code);
    List<Gate> findByFunction(Gate.GateFunction function);
    List<Gate> findBySiteAndFunction(Site site, Gate.GateFunction function);
}