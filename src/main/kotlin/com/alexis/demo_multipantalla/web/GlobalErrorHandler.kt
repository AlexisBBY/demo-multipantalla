package com.alexis.demo_multipantalla.web

import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class GlobalErrorHandler : ErrorController {

    @RequestMapping("/error")
    fun handleError(request: HttpServletRequest, model: Model): String {
        val status = request.getAttribute("jakarta.servlet.error.status_code") as? Int ?: 500
        model.addAttribute("status", status)
        return "error"
    }
}
