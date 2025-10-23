package com.example.demo.controller.admin;

import com.example.demo.constant.SystemConstant;
import com.example.demo.model.dto.CustomerDTO;
import com.example.demo.security.utils.SecurityUtils;
import com.example.demo.service.CustomerService;
import com.example.demo.utils.MessageUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller("adminCustomerController")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private MessageUtils messageUtil;

    // /customer/profile-{username}
    @GetMapping("/customer/profile-{username}")
    public ModelAndView viewProfile(@PathVariable("username") String username, HttpServletRequest request) {
        // [CHANGED] trỏ về thư mục đang có file: templates/admin/customer/profile.html
        ModelAndView mav = new ModelAndView("admin/customer/profile");
        CustomerDTO model = customerService.findOneByUsername(username);
        initMessageResponse(mav, request);
        mav.addObject(SystemConstant.MODEL, model);
        return mav;
    }

    // /customer/profile-password
    @GetMapping("/customer/profile-password")
    public ModelAndView changePasswordPage(HttpServletRequest request) {
        // [CHANGED] trỏ về templates/admin/customer/password.html
        ModelAndView mav = new ModelAndView("admin/customer/password");
        String username = SecurityUtils.getPrincipal().getUsername();
        CustomerDTO model = customerService.findOneByUsername(username); // có sẵn id/username
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
