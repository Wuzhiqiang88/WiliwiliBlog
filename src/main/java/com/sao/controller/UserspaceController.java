package com.sao.controller;

import com.sao.domain.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 用户主页控制器
 */
@Controller
@RequestMapping("/u")
public class UserspaceController {

    @GetMapping("/blogedit")
    public String blogEdit(){
        return "/userspace/blogedit";
    }
}
