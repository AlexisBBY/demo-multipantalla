package com.alexis.demo_multipantalla.web

import com.alexis.demo_multipantalla.domain.Registration
import com.alexis.demo_multipantalla.repo.RegistrationRepo
import com.alexis.demo_multipantalla.service.RecaptchaService
import com.alexis.demo_multipantalla.web.dto.RegistrationForm
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping

@Controller
class HomeController(
    private val registrationRepo: RegistrationRepo,
    private val recaptchaService: RecaptchaService,

    // Si no existe, default false
    @Value("\${app.recaptcha.enabled:false}") private val recaptchaEnabled: Boolean,

    // Si no existe, default ""
    @Value("\${app.recaptcha.site-key:}") private val recaptchaSiteKey: String
) {

    @GetMapping("/")
    fun home(model: Model): String {
        model.addAttribute("registrationForm", RegistrationForm())
        model.addAttribute("recaptchaEnabled", recaptchaEnabled)
        model.addAttribute("recaptchaSiteKey", recaptchaSiteKey)
        return "home"
    }

    @PostMapping("/register")
    fun register(
        @Valid @ModelAttribute("registrationForm") form: RegistrationForm,
        binding: BindingResult,
        model: Model,
        request: HttpServletRequest
    ): String {

        // 1) Validación del form
        if (binding.hasErrors()) {
            model.addAttribute("recaptchaEnabled", recaptchaEnabled)
            model.addAttribute("recaptchaSiteKey", recaptchaSiteKey)
            return "home"
        }

        // 2) Validación reCAPTCHA (solo si está habilitado)
        if (recaptchaEnabled) {
            val token = request.getParameter("g-recaptcha-response") ?: ""
            val ok = recaptchaService.verify(token, request.remoteAddr)

            if (!ok) {
                model.addAttribute("recaptchaEnabled", recaptchaEnabled)
                model.addAttribute("recaptchaSiteKey", recaptchaSiteKey)
                model.addAttribute("recaptchaError", "Completa el reCAPTCHA correctamente.")
                return "home"
            }
        }

        // 3) Guardar en DB
        registrationRepo.save(
            Registration(username = form.username.trim())
        )

        return "redirect:/features"
    }
}
