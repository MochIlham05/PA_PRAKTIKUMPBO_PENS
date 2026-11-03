package com.rantaitasok.tembakau.repository;

import com.rantaitasok.tembakau.entity.Petani;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface untuk entitas Petani («Kind»).
 */
@Repository
public interface PetaniRepository extends JpaRepository<Petani, Long> {

    Optional<Petani> findByNik(String nik);

    boolean existsByNik(String nik);
}
