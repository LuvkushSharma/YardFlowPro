package com.yardflowpro.service;

import com.yardflowpro.dto.MoveRequestDto;
import com.yardflowpro.model.MoveRequest;
import com.yardflowpro.model.Site;

import java.util.List;

public interface MoveRequestService {
    MoveRequest createMoveRequest(MoveRequestDto moveRequestDto, Long requestedById);
    MoveRequest assignMoveRequest(Long moveRequestId, Long spotterId);
    MoveRequest startMoveRequest(Long moveRequestId);
    MoveRequest completeMoveRequest(Long moveRequestId);
    MoveRequest cancelMoveRequest(Long moveRequestId);
    List<MoveRequest> getPendingMoveRequests();
    List<MoveRequest> getMoveRequestsBySpotterId(Long spotterId);
    MoveRequest getMoveRequestById(Long id);
    List<MoveRequest> getMoveRequestsBySite(Site site);
    List<MoveRequest> getPendingMoveRequestsBySite(Site site);
}