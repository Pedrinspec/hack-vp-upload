package com.fiap.vp_upload.infra.adapter.input;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    @GetMapping("/upload")
    public String upload() {
        return "upload";
    }
}
