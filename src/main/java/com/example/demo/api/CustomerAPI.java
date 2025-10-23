package com.example.demo.api;

import com.example.demo.model.dto.AssignmentDTO;
import com.example.demo.model.dto.CustomerDTO;
import com.example.demo.model.request.ChangePasswordRequest;   // [ADDED]
import com.example.demo.model.request.CustomerCreateRequest;
import com.example.demo.model.response.ResponseDTO;
import com.example.demo.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Transactional
@RequestMapping("/api/customer")
public class CustomerAPI {

    @Autowired
    private CustomerService customerService;

    // ================== ADMIN/STAFF FLOW (giữ nguyên) ==================
    @PreAuthorize("hasAnyAuthority('STAFF','MANAGER','ADMIN')")
    @PostMapping
    public ResponseDTO addOrUpdateCustomer(@RequestBody CustomerCreateRequest customerCreateRequest){
        return customerService.save(customerCreateRequest);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER','ADMIN')")
    @PostMapping("/{ids}")
    public ResponseDTO disableActivity(@PathVariable List<Long> ids){
        return customerService.disableActivity(ids);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER','STAFF','ADMIN')")
    @GetMapping("/{id}/staffs")
    public ResponseDTO loadStaffs(@PathVariable Long id){
        return customerService.findStaffsByCustomerId(id);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER','ADMIN')")
    @PostMapping("/assignment")
    public ResponseDTO updateAssignmentCustomer(@RequestBody AssignmentDTO assignmentDTO){
        return customerService.updateAssignmentTable(assignmentDTO);
    }

    // ================== CUSTOMER SELF-SERVICE (profile/password) ==================

    // [ADDED] Cập nhật hồ sơ theo username (fullName/phone/companyname/demand/status...)
    // Được gọi từ: templates/web/customer/profile.html
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/profile/{username}")
    public CustomerDTO updateProfile(@PathVariable String username, @RequestBody CustomerDTO dto) {
        return customerService.updateProfile(username, dto);
    }

    // [ADDED] Đổi mật khẩu theo id (id lấy từ model khi render trang password)
    // Được gọi từ: templates/web/customer/password.html
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/change-password/{id}")
    public String changePassword(@PathVariable Long id, @RequestBody ChangePasswordRequest req) {
        boolean ok = customerService.changePassword(id, req.getOldPassword(), req.getNewPassword());
        return ok ? "update_success" : "change_password_fail";
    }
}
