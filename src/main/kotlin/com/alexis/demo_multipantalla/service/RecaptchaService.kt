package com.alexis.demo_multipantalla.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@Service
class RecaptchaService(
    // Lee desde app.recaptcha.secret y si no existe, usa ""
    @Value("\${app.recaptcha.secret:}") private val secret: String
) {
    private val rest = RestTemplate()

    fun verify(token: String, remoteIp: String?): Boolean {
        // Si no hay secret o no hay token, no pasa
        if (secret.isBlank()) return false
        if (token.isBlank()) return false

        val url = "https://www.google.com/recaptcha/api/siteverify"
        val form = LinkedMultiValueMap<String, String>().apply {
            add("secret", secret)
            add("response", token)
            if (!remoteIp.isNullOrBlank()) add("remoteip", remoteIp)
        }

        val resp = rest.postForObject(url, form, Map::class.java) ?: return false
        return (resp["success"] as? Boolean) == true
    }
}
