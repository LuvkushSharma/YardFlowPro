package com.yardflowpro.service;

import com.yardflowpro.model.Trailer;

import java.util.List;

public interface TrailerService {
    Trailer getTrailerById(Long id);
    Trailer getTrailerByNumber(String trailerNumber);
    List<Trailer> getTrailersBySiteId(Long siteId);
    List<Trailer> getTrailersByStatus(Trailer.ProcessStatus status);
    Trailer updateTrailerStatus(Long id, Trailer.ProcessStatus newStatus);
    Trailer assignTrailerToDoor(Long trailerId, Long doorId);
    Trailer assignTrailerToYardLocation(Long trailerId, Long yardLocationId);
    void updateDetentionStatus(Long trailerId);
}