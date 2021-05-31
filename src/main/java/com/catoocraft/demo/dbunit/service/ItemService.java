package com.catoocraft.demo.dbunit.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.catoocraft.demo.dbunit.model.Item;
import com.catoocraft.demo.dbunit.repository.ItemRepository;

@Service
@Transactional
public class ItemService {

	@Autowired
	ItemRepository itemRepository;
	
	public List<Item> findAll() {
		return itemRepository.findAll();
	}
	
	public Item save(Item item) {
		return itemRepository.save(item);
	}
	
}
