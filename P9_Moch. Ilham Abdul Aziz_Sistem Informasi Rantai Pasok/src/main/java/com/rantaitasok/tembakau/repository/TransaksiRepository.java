package com.rantaitasok.tembakau.repository;

import com.rantaitasok.tembakau.entity.Transaksi;
import com.rantaitasok.tembakau.model.enums.StatusTransaksi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface untuk entitas Transaksi («Relator»).
 */
@Repository
public interface TransaksiRepository extends JpaRepository<Transaksi, Long> {

    Optional<Transaksi> findByPanenId(Long panenId);

    boolean existsByPanenId(Long panenId);

    List<Transaksi> findByStatus(StatusTransaksi status);

    List<Transaksi> findByPengepulId(Long pengepulId);
}
