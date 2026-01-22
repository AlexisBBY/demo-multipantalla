package com.alexis.demo_multipantalla.web.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegistrationForm(
    @field:NotBlank(message = "El dato es obligatorio")
    @field:Size(min = 2, max = 50, message = "Entre 2 y 50 caracteres")
    var username: String = ""
)
