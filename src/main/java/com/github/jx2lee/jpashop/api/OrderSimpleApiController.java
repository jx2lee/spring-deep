package com.github.jx2lee.jpashop.api;

import com.github.jx2lee.jpashop.domain.Address;
import com.github.jx2lee.jpashop.domain.Order;
import com.github.jx2lee.jpashop.domain.OrderStatus;
import com.github.jx2lee.jpashop.repository.OrderRepository;
import com.github.jx2lee.jpashop.repository.OrderSearch;
import com.github.jx2lee.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import com.github.jx2lee.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne
 * Order
 * Order -> Member
 * Order -> Delivery
 */

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /**
     * V1. 엔티티 직접 노출
     * - Hibernate5Module 등록, Lazy=null 처리
     * - 양방향 문제 발생 -> @JsonIgnore
     */

    @GetMapping("/api/v1/simple-orders")
    private List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll();
        for (Order order : all) {
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getAddress(); // Lazy 강제 초기화
        }

        return all;
    }

    /**
     * V2. Entity -> Dto 변환
     * - 1 + N 문제 (N: 주문수)
     * - 1 + 회원 N + 배송 N
     * - 쿼리가 다수 발생하는 문제
     * - 만약 같은 유저가 주문한 경우에는 두 번째 주문에서 배송 조회 쿼리를 날리지 않음(-1)
     */
    @GetMapping("/api/v2/simple-orders")
    private List<SimpleOrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate; //주문시간
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }

    /**
     * V3. Fetch Join
     * - N + 1 문제 해결!@!
     * - 해당 API 수행 시 1개 쿼리만 수행
     * - 실무에서 fetch join 은 자주 사용하기 때문에, LAZY 로딩으로 엔티티를 설정하고 이와 같은 방식으로 쿼리 최적화
     * - select column 부분 최적화는 V4 에서!
     */
    @GetMapping("/api/v3/simple-orders")
    private List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return result;
    }

    /**
     * V4.
     * - fetch 조인 보다는 column 을 원하는 만큼 select
     * - V3 vs V4
     * - 우열을 가리기 힘듬. 서로 장단점이 있음!
     * - V3: 원하는 column 만 찾아 join
     * - V4: 재사용성 X
     * - 영한쌤 개인적 의견: V3와 V4 쿼리가 그렇게 차이가 나지 않음. 단, 데이터가 큰 경우에는 성능에 영향을 줄 수 있지만
     * 보통의 경우 from / where 절에서 성능이 판가름 난다.
     *
     * @return
     */
    @GetMapping("/api/v4/simple-orders")
    public Result ordersV4() {
        List<OrderSimpleQueryDto> findOrders = orderSimpleQueryRepository.findOrderDtos();
        List<OrderSimpleQueryDto> collect = findOrders.stream()
                .map(m -> new OrderSimpleQueryDto(m.getOrderId(), m.getName(), m.getOrderDate(),
                        m.getOrderStatus(), m.getAddress()))
                .collect(Collectors.toList());

        return new Result(collect.size(), collect);
    }

    @Data
    @AllArgsConstructor
    class Result<T> {
        private int count;
        private T order_info;
    }

}
