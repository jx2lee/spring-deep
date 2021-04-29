package com.github.jx2lee.jpashop.service;

import com.github.jx2lee.jpashop.domain.item.Item;
import com.github.jx2lee.jpashop.repository.ItemRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class ItemServiceTest {

    @Autowired ItemService itemService;
    @Autowired ItemRepository itemRepository;
    @Autowired EntityManager em;

    @Test
    public void 상품등록() throws Exception {
        //given
        Item item = new Item();
        item.setName("세탁기");

        //when
        Long saveId = itemService.saveItem(item);

        //then
        em.flush();
        assertEquals(item, itemRepository.findOne(saveId));

    }

}
