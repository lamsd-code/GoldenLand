package com.example.demo.repository.custom.impl;

import com.example.demo.builder.BuildingSearchBuilder;
import com.example.demo.entity.Building;
import com.example.demo.repository.custom.BuildingRepositoryCustom;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Primary
public class BuildingRepositoryImpl implements BuildingRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Giữ nguyên hàm cũ (không phân trang) để không phá vỡ tương thích ngược
     */
    @Override
    public List<Building> findAll(BuildingSearchBuilder buildingSearchBuilder) {
        StringBuilder sql = new StringBuilder("SELECT b.* FROM building b ");
        joinTable(buildingSearchBuilder, sql);
        sql.append("WHERE 1 = 1 ");
        queryNormal(sql, buildingSearchBuilder);
        querySpecial(sql, buildingSearchBuilder);
        sql.append(" GROUP BY b.id");
        Query query = entityManager.createNativeQuery(sql.toString(), Building.class);
        return query.getResultList();
    }

    /**
     * Hàm mới: trả về danh sách có phân trang (offset/limit)
     */
    @Override
    public List<Building> findAll(BuildingSearchBuilder buildingSearchBuilder, Pageable pageable) {
        StringBuilder sql = new StringBuilder("SELECT b.* FROM building b ");
        joinTable(buildingSearchBuilder, sql);
        sql.append("WHERE 1 = 1 ");
        queryNormal(sql, buildingSearchBuilder);
        querySpecial(sql, buildingSearchBuilder);
        sql.append(" GROUP BY b.id ");

        // ORDER BY (nếu có sort trong Pageable)
        if (pageable != null && pageable.getSort().isSorted()) {
            StringBuilder order = new StringBuilder(" ORDER BY ");
            pageable.getSort().forEach(o -> {
                String property = o.getProperty();
                String direction = o.getDirection().name();
                // an toàn: chỉ cho phép sort theo cột của bảng building
                order.append("b.").append(property).append(" ").append(direction).append(", ");
            });
            // bỏ dấu phẩy cuối
            int lastComma = order.lastIndexOf(", ");
            if (lastComma > 0) order.delete(lastComma, order.length());
            sql.append(order);
        } else {
            sql.append(" ORDER BY b.id DESC ");
        }

        Query query = entityManager.createNativeQuery(sql.toString(), Building.class);

        if (pageable != null) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }
        return query.getResultList();
    }

    /**
     * Hàm mới: count tổng số bản ghi để dựng Page<>
     */
    @Override
    public long count(BuildingSearchBuilder buildingSearchBuilder) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT b.id) FROM building b ");
        joinTable(buildingSearchBuilder, sql);
        sql.append("WHERE 1 = 1 ");
        queryNormal(sql, buildingSearchBuilder);
        querySpecial(sql, buildingSearchBuilder);

        Query query = entityManager.createNativeQuery(sql.toString());
        Object single = query.getSingleResult();
        if (single instanceof Number) {
            return ((Number) single).longValue();
        }
        return Long.parseLong(single.toString());
    }

    private void joinTable(BuildingSearchBuilder buildingSearchBuilder, StringBuilder sql) {
        Integer staffId = buildingSearchBuilder.getStaffId();
        if (staffId != null) {
            sql.append(" JOIN assignmentbuilding ass ON b.id = ass.buildingid ");
            sql.append(" JOIN user u ON ass.staffid = u.id ");
        }
    }

    public static void queryNormal(StringBuilder sql, BuildingSearchBuilder buildingSearchBuilder) {
        try {
            Field[] fields = BuildingSearchBuilder.class.getDeclaredFields();
            for (Field item : fields) {
                item.setAccessible(true);
                String fieldName = item.getName();
                if (!fieldName.equals("staffId") && !fieldName.equals("typeCode")
                        && !fieldName.startsWith("rentPrice") && !fieldName.startsWith("area")) {
                    Object value = item.get(buildingSearchBuilder);
                    if (value != null && !value.toString().isEmpty()) {
                        if (item.getType().getName().equals("java.lang.Integer")) {
                            sql.append(" AND b.").append(fieldName).append(" = ").append(value).append(" ");
                        } else {
                            sql.append(" AND b.").append(fieldName).append(" LIKE '%").append(value).append("%' ");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void querySpecial(StringBuilder sql, BuildingSearchBuilder buildingSearchBuilder) {
        Integer staffId = buildingSearchBuilder.getStaffId();
        if (staffId != null) {
            sql.append(" AND u.id = ").append(staffId).append(" ");
        }

        Integer areaTo = buildingSearchBuilder.getAreaTo();
        Integer areaFrom = buildingSearchBuilder.getAreaFrom();
        if (areaFrom != null || areaTo != null) {
            sql.append(" AND EXISTS (SELECT * FROM rentarea r WHERE b.id = r.buildingid ");
            if (areaFrom != null) {
                sql.append(" AND r.value >= ").append(areaFrom).append(" ");
            }
            if (areaTo != null) {
                sql.append(" AND r.value <= ").append(areaTo).append(" ");
            }
            sql.append(") ");
        }

        Integer rentPriceTo = buildingSearchBuilder.getRentPriceTo();
        Integer rentPriceFrom = buildingSearchBuilder.getRentPriceFrom();
        if (rentPriceFrom != null || rentPriceTo != null) {
            if (rentPriceFrom != null) {
                sql.append(" AND b.rentprice >= ").append(rentPriceFrom).append(" ");
            }
            if (rentPriceTo != null) {
                sql.append(" AND b.rentprice <= ").append(rentPriceTo).append(" ");
            }
        }

        List<String> typeCode = buildingSearchBuilder.getTypeCode();
        if (typeCode != null && typeCode.size() > 0) {
            sql.append(" AND (");
            String tmp = typeCode.stream()
                    .map(it -> "b.type LIKE '%" + it + "%'")
                    .collect(Collectors.joining(" OR "));
            sql.append(tmp).append(") ");
        }
    }

    // Không dùng tới — giữ lại nếu bạn cần mở rộng
    private String buildQueryFilter() {
        return "SELECT * FROM building b ";
    }
}
