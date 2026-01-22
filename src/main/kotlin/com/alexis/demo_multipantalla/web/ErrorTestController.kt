package com.alexis.demo_multipantalla.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ErrorTestController {
    @GetMapping("/dev/boom")
    fun boom(): String {
        error("Error intencional para probar pantalla de error")
    }
}
