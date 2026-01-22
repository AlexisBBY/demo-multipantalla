package com.alexis.demo_multipantalla.web.dto

import jakarta.validation.constraints.*
import java.time.LocalDate

data class ContactForm(
    @field:NotBlank
    @field:Size(min = 2, max = 50)
    val fullName: String = "",

    @field:NotBlank
    @field:Email
    @field:Size(max = 100)
    val email: String = "",

    @field:NotBlank
    @field:Pattern(regexp = "\\d{10}", message = "Debe tener exactamente 10 dígitos")
    val phone: String = "",

    @field:NotNull(message = "La fecha es obligatoria")
    val birthDate: LocalDate? = null,

    @field:NotBlank
    @field:Size(min = 10, max = 500)
    val message: String = ""
)

