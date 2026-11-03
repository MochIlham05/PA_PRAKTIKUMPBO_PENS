package com.rantaitasok.tembakau.repository;

import com.rantaitasok.tembakau.entity.Panen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface untuk entitas Panen («Relator»).
 */
@Repository
public interface PanenRepository extends JpaRepository<Panen, Long> {

    List<Panen> findByPetaniId(Long petaniId);

    List<Panen> findByLahanId(Long lahanId);

    /**
     * Mengambil panen yang sudah selesai melalui proses grading kualitas.
     */
    @Query("SELECT p FROM Panen p WHERE p.hasilGrading IS NOT NULL")
    List<Panen> findAllSudahGrading();

    /**
     * Mengambil panen yang belum di-grading (pending grading).
     */
    @Query("SELECT p FROM Panen p WHERE p.hasilGrading IS NULL")
    List<Panen> findAllBelumGrading();
}
