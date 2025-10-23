package com.example.demo.repository.custom;

import com.example.demo.entity.Customer;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface CustomerRepositoryCustom {
    // Giữ hàm cũ (không phân trang) cho tương thích ngược
    List<Customer> findAll(Map<String, Object> conditions);

    // Mới: lấy danh sách có phân trang
    List<Customer> findAll(Map<String, Object> conditions, Pageable pageable);

    // Mới: đếm tổng để dựng Page<>
    long count(Map<String, Object> conditions);
}
