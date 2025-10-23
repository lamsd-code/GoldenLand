package com.example.demo.repository.custom.impl;

import com.example.demo.entity.User;
import com.example.demo.repository.custom.UserRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<User> findByRole(String roleCode) {
        // native: lấy user theo role code, chỉ user đang hoạt động (status=1)
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT u.* ")
                .append(" FROM user u ")
                .append(" JOIN user_role ur ON u.id = ur.userid ")
                .append(" JOIN role r ON ur.roleid = r.id ")
                .append(" WHERE u.status = 1 AND r.code = :roleCode ");

        Query query = entityManager.createNativeQuery(sql.toString(), User.class);
        query.setParameter("roleCode", roleCode);
        return query.getResultList();
    }

    @Override
    public List<User> getAllUsers(Pageable pageable) {
        StringBuilder sql = new StringBuilder(buildQueryFilter());
        sql.append(" ORDER BY u.id DESC ");
        sql.append(" LIMIT ").append(pageable.getPageSize())
                .append(" OFFSET ").append(pageable.getOffset());

        Query query = entityManager.createNativeQuery(sql.toString(), User.class);
        return query.getResultList();
    }

    @Override
    public int countTotalItem() {
        String sql = "SELECT COUNT(*) FROM user u WHERE u.status = 1";
        Query query = entityManager.createNativeQuery(sql);
        Object single = query.getSingleResult();
        if (single instanceof Number) {
            return ((Number) single).intValue();
        }
        return Integer.parseInt(single.toString());
    }

    private String buildQueryFilter() {
        // chỉ lấy user đang hoạt động
        return "SELECT * FROM user u WHERE u.status = 1";
    }
}
