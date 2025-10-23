package com.example.demo.security;

import com.example.demo.entity.Customer;
import com.example.demo.entity.User;
import com.example.demo.model.dto.MyUserDetail;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1) Ưu tiên tìm trong bảng User (Admin/Staff)
        User userEntity = userRepository.findOneByUserName(username);
        if (userEntity != null) {
            userEntity.getRoles().size(); // ép Hibernate load roles

            if (userEntity.getRoles() == null || userEntity.getRoles().isEmpty()) {
                throw new UsernameNotFoundException("Tài khoản chưa được gán vai trò nào!");
            }

            List<GrantedAuthority> authorities = new ArrayList<>();
            userEntity.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getCode())));

            MyUserDetail myUserDetail = new MyUserDetail(
                    userEntity.getUserName(),
                    userEntity.getPassword(),
                    true, true, true, true,
                    authorities
            );
            myUserDetail.setId(userEntity.getId());
            myUserDetail.setFullName(userEntity.getFullName());
            return myUserDetail;
        }

        // 2) Nếu không có trong bảng User → kiểm tra Customer
        // ---------- [FIX] unwrap Optional thay vì gán trực tiếp ----------
        Customer customer = customerRepository.findByUsername(username).orElse(null);
        // ---------------------------------------------------------------
        if (customer != null) {
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("CUSTOMER"));

            MyUserDetail myUserDetail = new MyUserDetail(
                    customer.getUsername(),
                    customer.getPassword(),
                    true, true, true, true,
                    authorities
            );
            myUserDetail.setFullName(customer.getFullname());
            myUserDetail.setId(customer.getId());
            return myUserDetail;
        }

        // 3) Nếu cả 2 bảng đều không có → báo lỗi
        throw new UsernameNotFoundException("Không tìm thấy tài khoản: " + username);
    }
}
