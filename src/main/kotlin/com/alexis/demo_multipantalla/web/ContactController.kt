package com.alexis.demo_multipantalla.web

import com.alexis.demo_multipantalla.domain.ContactMessage
import com.alexis.demo_multipantalla.repo.ContactMessageRepo
import com.alexis.demo_multipantalla.web.dto.ContactForm
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/contact")
class ContactController(
    private val repo: ContactMessageRepo
) {
    @GetMapping
    fun view(model: Model): String {
        model.addAttribute("contactForm", ContactForm())
        return "contact"
    }

    @PostMapping
    fun submit(
        @Valid @ModelAttribute contactForm: ContactForm,
        binding: BindingResult,
        model: Model
    ): String {
        // Validación de edad 18-120
        val bd = contactForm.birthDate
        if (bd != null) {
            val today = java.time.LocalDate.now()
            val age = java.time.Period.between(bd, today).years
            if (age !in 18..120) {
                binding.rejectValue("birthDate", "age", "Debes tener entre 18 y 120 años")
            }
        }

        if (binding.hasErrors()) return "contact"

        repo.save(
            ContactMessage(
                fullName = contactForm.fullName.trim(),
                email = contactForm.email.trim(),
                phone = contactForm.phone.trim(),
                birthDate = contactForm.birthDate!!,
                message = contactForm.message.trim()
            )
        )

        return "redirect:/contact?sent=1"
    }
}
