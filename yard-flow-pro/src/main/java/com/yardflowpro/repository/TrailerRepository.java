package com.yardflowpro.repository;

import com.yardflowpro.model.Carrier;
import com.yardflowpro.model.Trailer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrailerRepository extends JpaRepository<Trailer, Long> {
    Optional<Trailer> findByTrailerNumber(String trailerNumber);
    List<Trailer> findByCarrier(Carrier carrier);
    List<Trailer> findByProcessStatus(Trailer.ProcessStatus status);
    
    @Query("SELECT t FROM Trailer t WHERE t.detentionActive = true")
    List<Trailer> findAllWithActiveDetention();
}