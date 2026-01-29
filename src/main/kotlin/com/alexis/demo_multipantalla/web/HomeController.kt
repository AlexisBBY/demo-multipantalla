package com.alexis.demo_multipantalla.web

import com.alexis.demo_multipantalla.domain.Registration
import com.alexis.demo_multipantalla.repo.RegistrationRepo
import com.alexis.demo_multipantalla.service.RecaptchaService
import com.alexis.demo_multipantalla.web.dto.RegistrationForm
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataIntegrityViolationException
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

    @Value("\${app.recaptcha.enabled:false}") private val recaptchaEnabled: Boolean,
    @Value("\${app.recaptcha.site-key:}") private val recaptchaSiteKey: String
) {

    // Username: letras/números/._- sin espacios, 3 a 30 chars
    // (ajusta si tú quieres otra regla)
    private val USERNAME_REGEX = Regex("""^[A-Za-z0-9._-]{3,30}$""")

    @GetMapping("/")
    fun home(model: Model): String {
        model.addAttribute("registrationForm", RegistrationForm())
        addRecaptcha(model)
        return "home"
    }

    @PostMapping("/register")
    fun register(
        @Valid @ModelAttribute("registrationForm") form: RegistrationForm,
        binding: BindingResult,
        model: Model,
        request: HttpServletRequest
    ): String {

        // Siempre agrega config para que la vista no falle
        addRecaptcha(model)

        // Limpieza defensiva
        val usernameClean = clean(form.username)

        // Reinyecta al form para que si hay error se vea el valor limpio
        form.username = usernameClean

        // Bloquea HTML/raros (defensa extra aunque uses th:text)
        if (containsHtmlLike(usernameClean)) {
            binding.rejectValue("username", "badChars", "No se permiten caracteres extraños ni HTML.")
        }

        // Valida formato permitido
        if (usernameClean.isBlank() || !USERNAME_REGEX.matches(usernameClean)) {
            binding.rejectValue("username", "username", "Usuario inválido (3-30: letras, números, . _ -).")
        }

        // 1) Validación del form
        if (binding.hasErrors()) {
            return "home"
        }

        // 2) Validación reCAPTCHA (solo si está habilitado)
        if (recaptchaEnabled) {
            // Si no hay site-key configurada, no tiene caso validar (mejor mostrar error claro)
            if (recaptchaSiteKey.isBlank()) {
                model.addAttribute("recaptchaError", "reCAPTCHA no está configurado correctamente.")
                return "home"
            }

            val token = (request.getParameter("g-recaptcha-response") ?: "").trim()
            if (token.isBlank()) {
                model.addAttribute("recaptchaError", "Completa el reCAPTCHA correctamente.")
                return "home"
            }

            val ok = try {
                recaptchaService.verify(token, request.remoteAddr)
            } catch (e: Exception) {
                false
            }

            if (!ok) {
                model.addAttribute("recaptchaError", "Completa el reCAPTCHA correctamente.")
                return "home"
            }
        }

        // 3) Guardar en DB (sin reventar por constraint unique, etc.)
        return try {
            registrationRepo.save(Registration(username = usernameClean))
            "redirect:/features"
        } catch (e: DataIntegrityViolationException) {
            // Ej: username duplicado o constraint
            model.addAttribute("dbError", "Ese usuario ya existe o no se pudo guardar.")
            "home"
        } catch (e: Exception) {
            model.addAttribute("dbError", "Ocurrió un error al guardar. Intenta de nuevo.")
            "home"
        }
    }

    private fun addRecaptcha(model: Model) {
        model.addAttribute("recaptchaEnabled", recaptchaEnabled)
        model.addAttribute("recaptchaSiteKey", recaptchaSiteKey)
    }

    private fun clean(s: String?): String {
        if (s == null) return ""
        // quita control chars (incluye null byte) y normaliza espacios
        val noCtrl = s.replace(Regex("""[\u0000-\u001F\u007F]"""), "")
        return noCtrl.trim().replace(Regex("""\s+"""), "")
    }

    private fun containsHtmlLike(s: String): Boolean {
        if (s.contains('<') || s.contains('>')) return true
        if (s.contains('\u0000')) return true
        return false
    }
}
