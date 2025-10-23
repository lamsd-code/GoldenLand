package com.example.demo.service;

import com.example.demo.entity.Customer;
import com.example.demo.model.dto.AssignmentDTO;
import com.example.demo.model.dto.CustomerDTO;
import com.example.demo.model.request.CustomerCreateRequest;
import com.example.demo.model.response.ResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface CustomerService {
    // Giữ hàm cũ (không phân trang)
    List<CustomerDTO> findAll(Map<String, Object> conditions);

    // Mới: phân trang
    Page<CustomerDTO> findAllPaged(Map<String, Object> conditions, Pageable pageable);

    ResponseDTO save(CustomerCreateRequest customerCreateRequest);
    CustomerCreateRequest findOneById(Long id);
    ResponseDTO disableActivity(List<Long> id);
    ResponseDTO findStaffsByCustomerId(Long id);
    ResponseDTO updateAssignmentTable(AssignmentDTO assignmentDTO);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    /** Lưu trực tiếp entity Customer tối giản cho luồng đăng ký */
    Customer saveDirect(Customer customer);
    CustomerDTO findOneByUsername(String username);
    CustomerDTO updateProfile(String username, CustomerDTO dto);
    boolean changePassword(Long customerId, String oldPassword, String newPassword);
}
