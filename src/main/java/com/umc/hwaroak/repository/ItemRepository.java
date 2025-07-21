package com.umc.hwaroak.repository;

import com.umc.hwaroak.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    
    Item findByLevel(Integer level);

}
