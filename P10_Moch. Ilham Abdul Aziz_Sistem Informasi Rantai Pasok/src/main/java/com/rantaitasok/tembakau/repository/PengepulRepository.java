package com.rantaitasok.tembakau.repository;

import com.rantaitasok.tembakau.entity.Pengepul;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface untuk entitas Pengepul («Kind» / «Role»).
 */
@Repository
public interface PengepulRepository extends JpaRepository<Pengepul, Long> {

    Optional<Pengepul> findFirstByOrderByIdAsc();
}
