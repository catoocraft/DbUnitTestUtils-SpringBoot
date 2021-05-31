package com.catoocraft.demo.dbunit.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.dbunit.Assertion;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.catoocraft.demo.dbunit.DbUnitUtils;
import com.catoocraft.demo.dbunit.model.Item;

@SpringBootTest
class ItemServiceTest {

	String[] ignoreColumns = new String[] { "created", "updated" };
	
	@Autowired
	ItemService service;

	@Test
	void query() throws Exception {
		DbUnitUtils.setUpTables("ItemService/setup.xlsx");
		List<Item> actual = service.findAll();
		assertThat(actual.size(), is(1));
		assertThat(actual.get(0).title, is("Apple"));
	}

	@Test
	void save() throws Exception {
		DbUnitUtils.setUpTables("ItemService/setup.xlsx");
		Item item = new Item();
		item.id = 1002;
		item.title = "Orange";
		item.price = 200;
		Item actual = service.save(item);
		assertThat(actual.title, is("Orange"));
		assertThat(actual.price, is(200));
		assertThat(actual.version, is(0));

		// 実行結果
		ITable actualTable = DbUnitUtils.getActualTable("items");

		// 期待値
		IDataSet expectedDataSet = DbUnitUtils.getDataSet("ItemService/expected_save.xlsx");
		ITable expectedTable = expectedDataSet.getTable("items");

		//比較
		Assertion.assertEqualsIgnoreCols(expectedTable, actualTable, ignoreColumns);
	}
	
	@Test
	void update() throws Exception {
		DbUnitUtils.setUpTables("ItemService/setup.xlsx");
		Item item = new Item();
		item.id = 1001;
		item.title = "Apple 2";
		item.price = 220;
		item.version = 1;
		Item actual = service.save(item);
		assertThat(actual.title, is("Apple 2"));
		assertThat(actual.price, is(220));
		assertThat(actual.version, is(2));

		// 実行結果
		ITable actualTable = DbUnitUtils.getActualTable("items");

		// 期待値
		IDataSet expectedDataSet = DbUnitUtils.getDataSet("ItemService/expected_update.xlsx");
		ITable expectedTable = expectedDataSet.getTable("items");

		//比較
		Assertion.assertEqualsIgnoreCols(expectedTable, actualTable, ignoreColumns);
	}

	@BeforeEach
	public void setUp() throws Exception {
		DbUnitUtils.dumpTables("ItemDao/setup.xlsx");
	}

	@AfterEach
	public void tearDown() throws Exception {
		DbUnitUtils.restoreTables();
	}
}
