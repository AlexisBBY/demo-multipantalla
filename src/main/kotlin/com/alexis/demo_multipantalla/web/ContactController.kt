package com.alexis.demo_multipantalla.web

import com.alexis.demo_multipantalla.domain.ContactMessage
import com.alexis.demo_multipantalla.repo.ContactMessageRepo
import com.alexis.demo_multipantalla.web.dto.ContactForm
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.time.LocalDate
import java.time.Period

@Controller
@RequestMapping("/contact")
class ContactController(
    private val repo: ContactMessageRepo
) {
    private val NAME_REGEX = Regex("""^[\p{L}][\p{L}\s'\-]{1,69}$""")
    private val EMAIL_REGEX = Regex(
        """^[A-Za-z0-9._%+\-]{1,64}@[A-Za-z0-9](?:[A-Za-z0-9\-]{0,61}[A-Za-z0-9])?(?:\.[A-Za-z0-9](?:[A-Za-z0-9\-]{0,61}[A-Za-z0-9])?)*\.[A-Za-z]{2,63}$"""
    )
    private val PHONE_REGEX = Regex("""^[0-9+\-\s()]{7,20}$""")
    private val MAX_MESSAGE = 500

    @GetMapping
    fun view(model: Model): String {
        model.addAttribute("contactForm", ContactForm())
        return "contact"
    }

    @PostMapping
    fun submit(
        @Valid @ModelAttribute("contactForm") contactForm: ContactForm,
        binding: BindingResult,
        model: Model
    ): String {

        // Limpieza defensiva (sin reasignar al DTO)
        val fullName = clean(contactForm.fullName)
        val email = clean(contactForm.email).lowercase()
        val phone = clean(contactForm.phone)
        val message = clean(contactForm.message)

        // Bloquear HTML / caracteres raros
        if (containsHtmlLike(fullName) || containsHtmlLike(email) || containsHtmlLike(phone) || containsHtmlLike(message)) {
            binding.reject("invalidChars", "No se permiten etiquetas HTML ni caracteres extraños.")
        }

        // Validaciones fuertes
        if (fullName.isBlank() || !NAME_REGEX.matches(fullName)) {
            binding.rejectValue("fullName", "name", "Nombre inválido (solo letras y espacios).")
        }

        if (email.isBlank() || !EMAIL_REGEX.matches(email)) {
            binding.rejectValue("email", "email", "Email inválido (ej: usuario@dominio.com.mx).")
        }

        if (phone.isBlank() || !PHONE_REGEX.matches(phone)) {
            binding.rejectValue("phone", "phone", "Teléfono inválido (usa números y + - ( )).")
        }

        if (message.isBlank()) {
            binding.rejectValue("message", "message", "Escribe un mensaje.")
        } else if (message.length > MAX_MESSAGE) {
            binding.rejectValue("message", "messageLen", "El mensaje no debe pasar de $MAX_MESSAGE caracteres.")
        }

        // Validación birthDate
        val bd = contactForm.birthDate
        if (bd == null) {
            binding.rejectValue("birthDate", "required", "La fecha de nacimiento es obligatoria.")
        } else {
            val today = LocalDate.now()
            val age = Period.between(bd, today).years
            if (age !in 18..120) {
                binding.rejectValue("birthDate", "age", "Debes tener entre 18 y 120 años")
            }
        }

        if (binding.hasErrors()) {
            // ✅ Resumen de campos con error (para mostrar arriba)
            model.addAttribute("errorFields", binding.fieldErrors.map { it.field }.distinct())
            return "contact"
        }

        repo.save(
            ContactMessage(
                fullName = fullName,
                email = email,
                phone = phone,
                birthDate = bd!!,
                message = message
            )
        )

        return "redirect:/contact?sent=1"
    }

    private fun clean(s: String?): String {
        if (s == null) return ""
        val noCtrl = s.replace(Regex("""[\u0000-\u001F\u007F]"""), "")
        return noCtrl.trim().replace(Regex("""\s+"""), " ")
    }

    private fun containsHtmlLike(s: String): Boolean {
        if (s.contains('<') || s.contains('>')) return true
        if (s.contains('\u0000')) return true
        return false
    }
}
