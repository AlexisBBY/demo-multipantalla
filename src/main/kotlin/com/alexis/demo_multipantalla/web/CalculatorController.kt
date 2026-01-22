package com.alexis.demo_multipantalla.web

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/calculator")
class CalculatorController {

    @GetMapping
    fun view(): String = "calculator"

    @PostMapping("/sum")
    fun sum(@RequestParam a: Double?, @RequestParam b: Double?, model: Model): String {
        val result = if (a != null && b != null) a + b else null
        model.addAttribute("result", result)
        model.addAttribute("a", a)
        model.addAttribute("b", b)
        model.addAttribute("op", "sum")
        return "calculator"
    }

    @PostMapping("/divide")
    fun divide(@RequestParam a: Double?, @RequestParam b: Double?, model: Model): String {
        val error: String?
        val result: Double?

        if (a == null || b == null) {
            error = "Ingresa ambos números"
            result = null
        } else if (b == 0.0) {
            error = "No se puede dividir entre 0"
            result = null
        } else {
            error = null
            result = a / b
        }

        model.addAttribute("error", error)
        model.addAttribute("result", result)
        model.addAttribute("a", a)
        model.addAttribute("b", b)
        model.addAttribute("op", "div")
        return "calculator"
    }

    @PostMapping("/clear")
    fun clear(): String = "redirect:/calculator"
}
