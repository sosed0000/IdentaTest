package com.identa.denisov.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaffController {

    @GetMapping("/staff")
    public String staffHome() {
        return "staff_home";
    }
}
