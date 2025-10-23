package com.example.demo.converter;

import com.example.demo.entity.Customer;
import com.example.demo.model.dto.CustomerDTO;
import com.example.demo.model.request.CustomerCreateRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomerConverter {

    @Autowired
    private ModelMapper modelMapper;

    // DTO <- Entity
    public CustomerDTO toCustomerDTO(Customer customerEntity) {
        return modelMapper.map(customerEntity, CustomerDTO.class);
    }

    // Entity <- CreateRequest (đăng ký/khởi tạo)
    public Customer toCustomerEntity(CustomerCreateRequest req) {
        Customer entity = modelMapper.map(req, Customer.class);

        // Giữ lại thiết lập tường minh như bạn đang có
        entity.setFullname(req.getFullname());
        entity.setPhone(req.getPhone());
        entity.setEmail(req.getEmail());
        entity.setDemand(req.getDemand());

        // Nếu có username/password trong request (tùy form đăng ký của bạn)
        try {
            var u = req.getClass().getMethod("getUsername").invoke(req);
            if (u instanceof String && !((String) u).trim().isEmpty()) {
                entity.setUsername(((String) u).trim());
            }
        } catch (Exception ignored) {}

        try {
            var p = req.getClass().getMethod("getPassword").invoke(req);
            if (p instanceof String && !((String) p).trim().isEmpty()) {
                entity.setPassword(((String) p).trim()); // ⚠️ nhớ encode ở Service trước khi save
            }
        } catch (Exception ignored) {}

        if (req.getCompanyname() == null || req.getCompanyname().trim().isEmpty()) {
            entity.setCompanyname("Chưa cập nhật");
        } else {
            entity.setCompanyname(req.getCompanyname().trim());
        }

        if (req.getStatus() == null || req.getStatus().trim().isEmpty()) {
            entity.setStatus("Chưa xử lý");
        } else {
            entity.setStatus(req.getStatus().trim());
        }

        entity.setIsActive(1);
        return entity;
    }

    public CustomerCreateRequest toCustomerCreateRequest(Customer customerEntity) {
        return modelMapper.map(customerEntity, CustomerCreateRequest.class);
    }

    // cập nhật hồ sơ an toàn (không đụng username/password/email nếu không cho phép)
    public void applyProfileUpdate(Customer entity, CustomerDTO dto) {
        if (dto == null) return;

        if (dto.getFullname() != null) entity.setFullname(dto.getFullname().trim());
        if (dto.getPhone() != null) entity.setPhone(dto.getPhone().trim());
        // Cho phép đổi email? mở comment nếu cần
        // if (dto.getEmail() != null) entity.setEmail(dto.getEmail().trim());

        if (dto.getCompanyname() != null) {
            String val = dto.getCompanyname().trim();
            entity.setCompanyname(val.isEmpty() ? "Chưa cập nhật" : val);
        }
        if (dto.getDemand() != null) entity.setDemand(dto.getDemand().trim());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus().trim());
        if (dto.getIsActive() != null) entity.setIsActive(dto.getIsActive());
    }
}
