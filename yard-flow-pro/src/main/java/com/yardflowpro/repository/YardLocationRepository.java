package com.yardflowpro.repository;

import com.yardflowpro.model.Site;
import com.yardflowpro.model.YardLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface YardLocationRepository extends JpaRepository<YardLocation, Long> {
    List<YardLocation> findBySite(Site site);
    List<YardLocation> findByStatus(YardLocation.LocationStatus status);
}