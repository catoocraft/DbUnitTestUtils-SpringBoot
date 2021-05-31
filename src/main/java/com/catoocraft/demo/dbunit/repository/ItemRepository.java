package com.catoocraft.demo.dbunit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.catoocraft.demo.dbunit.model.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

}
