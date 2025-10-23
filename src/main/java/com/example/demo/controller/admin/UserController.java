package com.example.demo.controller.admin;

import com.example.demo.constant.SystemConstant;
import com.example.demo.model.dto.UserDTO;
import com.example.demo.security.utils.SecurityUtils;
import com.example.demo.service.RoleService;
import com.example.demo.service.UserService;
import com.example.demo.utils.MessageUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','STAFF')")
@Controller(value = "usersControllerOfAdmin")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private MessageUtils messageUtil;

    @GetMapping(value = "/admin/user-list")
    public ModelAndView userList(@ModelAttribute(SystemConstant.MODEL) UserDTO model,
                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                 @RequestParam(name = "size", defaultValue = "5") int size,
                                 HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("admin/user/list");

        Page<UserDTO> userPage = userService.findAllPaged(model.getSearchValue(), PageRequest.of(page, size));
        model.setListResult(userPage.getContent());
        model.setTotalItems((int) userPage.getTotalElements());
        model.setMaxPageItems(size);

        mav.addObject("page", userPage);
        mav.addObject(SystemConstant.MODEL, model);
        initMessageResponse(mav, request);
        return mav;
    }

    @GetMapping(value = "/admin/user-edit")
    public ModelAndView addUser(@ModelAttribute(SystemConstant.MODEL) UserDTO model, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("admin/user/edit");
        model.setRoleDTOs(roleService.getRoles());
        initMessageResponse(mav, request);
        mav.addObject(SystemConstant.MODEL, model);
        return mav;
    }

    @GetMapping(value = "/admin/profile-{username}")
    public ModelAndView updateProfile(@PathVariable("username") String username, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("admin/user/profile");
        UserDTO model = userService.findOneByUserName(username);
        initMessageResponse(mav, request);
        model.setRoleDTOs(roleService.getRoles());
        mav.addObject(SystemConstant.MODEL, model);
        return mav;
    }

    @GetMapping(value = "/admin/user-edit-{id}")
    public ModelAndView updateUser(@PathVariable("id") Long id, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("admin/user/edit");
        UserDTO model = userService.findUserById(id);
        model.setRoleDTOs(roleService.getRoles());
        initMessageResponse(mav, request);
        mav.addObject(SystemConstant.MODEL, model);
        return mav;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','STAFF')")
    @GetMapping(value = "/admin/profile-password")
    public ModelAndView updatePassword(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("admin/user/password");
        UserDTO model = userService.findOneByUserName(SecurityUtils.getPrincipal().getUsername());
        initMessageResponse(mav, request);
        mav.addObject(SystemConstant.MODEL, model);
        return mav;
    }

    private void initMessageResponse(ModelAndView mav, HttpServletRequest request) {
        String message = request.getParameter("message");
        if (message != null && StringUtils.isNotEmpty(message)) {
            Map<String, String> messageMap = messageUtil.getMessage(message);
            mav.addObject(SystemConstant.ALERT, messageMap.get(SystemConstant.ALERT));
            mav.addObject(SystemConstant.MESSAGE_RESPONSE, messageMap.get(SystemConstant.MESSAGE_RESPONSE));
        }
    }
}
