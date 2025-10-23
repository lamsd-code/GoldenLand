package com.example.demo.repository.custom;

import com.example.demo.builder.BuildingSearchBuilder;
import com.example.demo.entity.Building;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BuildingRepositoryCustom {

    // Tương thích cũ (không phân trang)
    List<Building> findAll(BuildingSearchBuilder buildingSearchBuilder);

    // Mới: lấy danh sách có phân trang
    List<Building> findAll(BuildingSearchBuilder buildingSearchBuilder, Pageable pageable);

    // Mới: đếm tổng số bản ghi để dựng Page<>
    long count(BuildingSearchBuilder buildingSearchBuilder);
}
