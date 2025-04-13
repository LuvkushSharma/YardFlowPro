package com.yardflowpro.repository;

import com.yardflowpro.model.MoveRequest;
import com.yardflowpro.model.Site;
import com.yardflowpro.model.Trailer;
import com.yardflowpro.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MoveRequestRepository extends JpaRepository<MoveRequest, Long> {
    List<MoveRequest> findByStatus(MoveRequest.MoveStatus status);
    List<MoveRequest> findByTrailer(Trailer trailer);
    List<MoveRequest> findByAssignedSpotter(User spotter);

    List<MoveRequest> findBySite(Site site);
    List<MoveRequest> findBySiteAndStatus(Site site, MoveRequest.MoveStatus status);
    List<MoveRequest> findByAssignedSpotterAndSite(User spotter, Site site);
    
    List<MoveRequest> findByTrailerAndStatus(Trailer trailer, MoveRequest.MoveStatus status);
    List<MoveRequest> findByAssignedSpotterAndStatus(User spotter, MoveRequest.MoveStatus status);
}