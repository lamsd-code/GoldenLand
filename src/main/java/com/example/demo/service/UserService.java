package com.example.demo.service;

import com.example.demo.model.dto.PasswordDTO;
import com.example.demo.model.dto.UserDTO;
import com.example.demo.exception.MyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface UserService {
    UserDTO findOneByUserNameAndStatus(String name, int status);
    List<UserDTO> getUsers(String searchValue, Pageable pageable); // giữ API cũ
    Page<UserDTO> findAllPaged(String searchValue, Pageable pageable); // mới: tiện cho pagination
    int getTotalItems(String searchValue);
    UserDTO findOneByUserName(String userName);
    UserDTO findUserById(long id);
    UserDTO insert(UserDTO userDTO);
    UserDTO update(Long id, UserDTO userDTO);
    void updatePassword(long id, PasswordDTO userDTO) throws MyException;
    UserDTO resetPassword(long id);
    UserDTO updateProfileOfUser(String id, UserDTO userDTO);
    UserDTO createUser(UserDTO userDTO);
    void delete(long[] ids);
    List<UserDTO> getAllUsers(Pageable pageable);
    Map<Long, String> getStaffs();
    int countTotalItems();
}
