package com.example.demo.repository.custom.impl;

import com.example.demo.entity.Customer;
import com.example.demo.repository.custom.CustomerRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Primary
public class CustomerRepositoryImpl implements CustomerRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    // Giữ hàm cũ (không phân trang)
    @Override
    public List<Customer> findAll(Map<String, Object> conditions) {
        StringBuilder sql = new StringBuilder("SELECT c.* FROM customer c ");
        joinTable(conditions, sql);
        sql.append(" WHERE 1=1 AND c.is_active = 1 ");
        appendConditions(conditions, sql);
        sql.append(" GROUP BY c.id ");
        Query query = entityManager.createNativeQuery(sql.toString(), Customer.class);
        return query.getResultList();
    }

    // Mới: có phân trang
    @Override
    public List<Customer> findAll(Map<String, Object> conditions, Pageable pageable) {
        StringBuilder sql = new StringBuilder("SELECT c.* FROM customer c ");
        joinTable(conditions, sql);
        sql.append(" WHERE 1=1 AND c.is_active = 1 ");
        appendConditions(conditions, sql);
        sql.append(" GROUP BY c.id ");

        // ORDER BY (ưu tiên từ Pageable)
        if (pageable != null && pageable.getSort().isSorted()) {
            StringBuilder order = new StringBuilder(" ORDER BY ");
            pageable.getSort().forEach(o -> {
                order.append("c.").append(o.getProperty())
                        .append(" ").append(o.getDirection().name())
                        .append(", ");
            });
            int idx = order.lastIndexOf(", ");
            if (idx > 0) order.delete(idx, order.length());
            sql.append(order);
        } else {
            sql.append(" ORDER BY c.id DESC ");
        }

        Query query = entityManager.createNativeQuery(sql.toString(), Customer.class);
        if (pageable != null) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }
        return query.getResultList();
    }

    // Mới: đếm tổng để dựng Page<>
    @Override
    public long count(Map<String, Object> conditions) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT c.id) FROM customer c ");
        joinTable(conditions, sql);
        sql.append(" WHERE 1=1 AND c.is_active = 1 ");
        appendConditions(conditions, sql);
        Query query = entityManager.createNativeQuery(sql.toString());
        Object rs = query.getSingleResult();
        return (rs instanceof Number) ? ((Number) rs).longValue() : Long.parseLong(rs.toString());
    }

    private void joinTable(Map<String, Object> conditions, StringBuilder sql) {
        Object staffId = conditions.get("staffId");
        if (staffId != null && !staffId.toString().trim().isEmpty()) {
            sql.append(" JOIN assignmentcustomer ass ON c.id = ass.customerid ");
        }
    }

    private void appendConditions(Map<String, Object> conditions, StringBuilder sql) {
        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) continue;
            String val = value.toString().trim();
            if (val.isEmpty()) continue;

            if ("staffId".equals(key)) {
                sql.append(" AND ass.staffid = ").append(val).append(" ");
            } else {
                // bỏ các param lạ từ displaytag nếu còn
                if (!"d-3677046-p".equals(key)) {
                    sql.append(" AND c.").append(key).append(" LIKE '%").append(val).append("%' ");
                }
            }
        }
    }
}
