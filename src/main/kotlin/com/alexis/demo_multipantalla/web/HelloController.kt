package com.alexis.demo_multipantalla.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HelloController {
    @GetMapping("/hello")
    fun hello(): String = "hello"
}
