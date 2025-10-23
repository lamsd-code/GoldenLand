package com.example.demo.service;

import com.example.demo.model.dto.AssignmentDTO;
import com.example.demo.model.dto.BuildingDTO;
import com.example.demo.model.response.BuildingSearchResponse;
import com.example.demo.model.response.ResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface BuildingService {

    // Giữ nguyên: không phân trang (tương thích ngược)
    List<BuildingSearchResponse> findAll(Map<String, Object> conditions, List<String> typeCode);

    // Mới: trả về Page cho phân trang admin
    Page<BuildingSearchResponse> findAll(Map<String, Object> conditions, List<String> typeCode, Pageable pageable);

    ResponseDTO save(BuildingDTO buildingDTO);
    BuildingDTO findBuildingById(Long id);
    ResponseDTO deleteBuildings(List<Long> buildingIds);
    ResponseDTO findStaffsByBuildingId(Long buildingId);
    ResponseDTO updateAssignmentTable(AssignmentDTO assignmentBuildingDTO);
}
