package com.rantaitasok.tembakau.repository;

import com.rantaitasok.tembakau.entity.Lahan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface untuk entitas Lahan («Subkind» / «Role»).
 */
@Repository
public interface LahanRepository extends JpaRepository<Lahan, Long> {

    List<Lahan> findByPetaniId(Long petaniId);
}
