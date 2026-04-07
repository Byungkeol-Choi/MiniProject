package com.cafe.kiosk.repository;

import com.cafe.kiosk.domain.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {

    @EntityGraph(attributePaths = {"member", "orderItem", "orderItem.menu"})
    @Query("select o from Orders o where o.id = :id")
    Optional<Orders> findWithLineItemsById(@Param("id") Long id);

    // 최병걸 추가 시작
    @EntityGraph(attributePaths = {"member", "orderItem", "orderItem.menu"})
    @Query("select o from Orders o")
    Page<Orders> findPageWithLines(Pageable pageable);

    @EntityGraph(attributePaths = {"member", "orderItem", "orderItem.menu"})
    Page<Orders> findByStatus(Orders.Status status, Pageable pageable);

    @Query(value = """
        SELECT COALESCE(SUM(total_amount - discount_amount), 0)
        FROM orders
        WHERE created_at >= :start
          AND created_at < :end
          AND status <> 'CANCELLED'
        """, nativeQuery = true)
    Long sumNetSalesBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    default Long sumNetSalesForDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return sumNetSalesBetween(start, end);
    }

    @Query(value = """
        SELECT COUNT(*)
        FROM orders
        WHERE created_at >= :start
          AND created_at < :end
          AND status = :status
        """, nativeQuery = true)
    long countByStatusBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") String status);
    default long countByStatusForDate(LocalDate date, String status) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return countByStatusBetween(start, end, status);
    }

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update Orders o set o.status = :status where o.id = :id")
    int updateStatusById(@Param("id") Long id, @Param("status") Orders.Status status);
    // 최병걸 추가 종료
}
