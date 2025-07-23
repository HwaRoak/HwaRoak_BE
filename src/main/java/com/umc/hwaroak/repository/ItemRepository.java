package com.umc.hwaroak.repository;

import com.umc.hwaroak.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    
    Optional<Item> findByLevel(Integer level);

}
