package com.github.jx2lee.jpashop.repository;

import com.github.jx2lee.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {
        if (item.getId() == null) {
            em.persist(item);
        } else {
            em.merge(item); // == update 와 비슷하다고 생각
        }
    }

    // 단건 조회
    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    // 전체 조회
    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }

}
