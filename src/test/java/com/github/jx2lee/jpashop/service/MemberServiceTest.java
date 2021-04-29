package com.github.jx2lee.jpashop.service;

import com.github.jx2lee.jpashop.domain.Member;
import com.github.jx2lee.jpashop.repository.MemberRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired EntityManager em;

    @Test
//    @Rollback(false) insert 문을 보고 싶으면 해당 어노테이션 활성화, H2 확인하면 해당 row 검색이 된다.
//    만약 insert query 만 보고 싶다면, @Autowired EntityManager em; 선언 이후 then 부분에 em.flush() 를 추가
//    flush: db에 쿼리를 날린다.
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("jx2lee");

        //when
        Long savedId = memberService.join(member);

        //then
        em.flush();
        assertEquals(member, memberRepository.findOne(savedId));
    }

    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("jx2lee");

        Member member2 = new Member();
        member2.setName("jx2lee");

        //when
        memberService.join(member1);
        memberService.join(member2); // Exception 발생!

        //then
        fail("예외가 발생해야 한다.");
    }
}
