package com.alexis.demo_multipantalla.web

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.math.BigDecimal
import java.math.RoundingMode

@Controller
@RequestMapping("/calculator")
class CalculatorController {

    // Solo números positivos, opcional decimal con punto. (sin +, -, e, comas, espacios, etc.)
    // Ajusta {1,10} si quieres más/menos decimales permitidos.
    private val NUMBER_REGEX = Regex("""^\d+(\.\d{1,10})?$""")
    private val MAX_LEN = 20

    @GetMapping
    fun view(): String = "calculator"

    @PostMapping("/sum")
    fun sum(
        @RequestParam(required = false) a: String?,
        @RequestParam(required = false) b: String?,
        model: Model
    ): String {
        val parsed = parseTwoNumbers(a, b)
        if (parsed.error != null) {
            model.addAttribute("error", parsed.error)
            model.addAttribute("result", null)
            // no regresamos valores inválidos
            model.addAttribute("a", parsed.safeA)
            model.addAttribute("b", parsed.safeB)
            model.addAttribute("op", "sum")
            return "calculator"
        }

        val result = parsed.a!!.add(parsed.b!!)
        model.addAttribute("error", null)
        model.addAttribute("result", result.stripTrailingZeros().toPlainString())
        model.addAttribute("a", parsed.safeA)
        model.addAttribute("b", parsed.safeB)
        model.addAttribute("op", "sum")
        return "calculator"
    }

    @PostMapping("/divide")
    fun divide(
        @RequestParam(required = false) a: String?,
        @RequestParam(required = false) b: String?,
        model: Model
    ): String {
        val parsed = parseTwoNumbers(a, b)
        if (parsed.error != null) {
            model.addAttribute("error", parsed.error)
            model.addAttribute("result", null)
            model.addAttribute("a", parsed.safeA)
            model.addAttribute("b", parsed.safeB)
            model.addAttribute("op", "div")
            return "calculator"
        }

        if (parsed.b!!.compareTo(BigDecimal.ZERO) == 0) {
            model.addAttribute("error", "No se puede dividir entre 0")
            model.addAttribute("result", null)
            model.addAttribute("a", parsed.safeA)
            model.addAttribute("b", parsed.safeB)
            model.addAttribute("op", "div")
            return "calculator"
        }

        // Ajusta scale (10) si quieres más/menos decimales en el resultado
        val result = parsed.a!!.divide(parsed.b!!, 10, RoundingMode.HALF_UP)
        model.addAttribute("error", null)
        model.addAttribute("result", result.stripTrailingZeros().toPlainString())
        model.addAttribute("a", parsed.safeA)
        model.addAttribute("b", parsed.safeB)
        model.addAttribute("op", "div")
        return "calculator"
    }

    @PostMapping("/clear")
    fun clear(): String = "redirect:/calculator"

    // ===== Helpers =====

    private data class Parsed(
        val a: BigDecimal?,
        val b: BigDecimal?,
        val safeA: String, // lo que sí se puede re-mostrar en el input
        val safeB: String,
        val error: String?
    )

    private fun parseTwoNumbers(aRaw: String?, bRaw: String?): Parsed {
        val aTrim = aRaw?.trim().orEmpty()
        val bTrim = bRaw?.trim().orEmpty()

        // No regresamos nada raro al HTML; solo regresamos lo que pase validación
        val aOk = isValidNumber(aTrim)
        val bOk = isValidNumber(bTrim)

        val safeA = if (aOk) aTrim else ""
        val safeB = if (bOk) bTrim else ""

        if (aTrim.isEmpty() || bTrim.isEmpty()) {
            return Parsed(null, null, safeA, safeB, "Ingresa ambos números")
        }

        if (!aOk || !bOk) {
            return Parsed(null, null, safeA, safeB, "Solo se permiten números (ej: 10 o 10.5)")
        }

        // ya validado por regex, parseo seguro
        val a = aTrim.toBigDecimal()
        val b = bTrim.toBigDecimal()
        return Parsed(a, b, safeA, safeB, null)
    }

    private fun isValidNumber(s: String): Boolean {
        if (s.isEmpty()) return false
        if (s.length > MAX_LEN) return false
        return NUMBER_REGEX.matches(s)
    }
}
