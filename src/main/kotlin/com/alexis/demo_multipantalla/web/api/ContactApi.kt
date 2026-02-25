package com.alexis.demo_multipantalla.web.api

import com.alexis.demo_multipantalla.repo.ContactMessageRepo
import com.alexis.demo_multipantalla.domain.ContactMessage
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/contact")
class ContactApi(
    private val repo: ContactMessageRepo
) {

    // Método para listar los registros con filtros (búsqueda por texto y fechas)
    @GetMapping
    fun list(
        @RequestParam(required = false) q: String?,     // Búsqueda por nombre/email/teléfono
        @RequestParam(required = false) from: String?,   // Fecha desde
        @RequestParam(required = false) to: String?,     // Fecha hasta
        @RequestParam(required = false) limit: Int?      // Límite de resultados
    ): List<ContactMessage> {

        // Limitar el número de resultados
        val safeLimit = (limit ?: 5).coerceIn(1, 100)

        // Convertir las fechas de cadena a Instant si se proporcionan
        val fromTs = from?.takeIf { it.isNotBlank() }?.let {
            Instant.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(it))
        }

        val toTs = to?.takeIf { it.isNotBlank() }?.let {
            Instant.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(it))
        }

        // Llamar al repositorio para obtener los registros filtrados
        return repo.search(q, fromTs, toTs, PageRequest.of(0, safeLimit))
    }

    // Método para crear un nuevo contacto (envío de formulario o fetch POST)
    @PostMapping
    fun create(@RequestBody dto: ContactCreateDto): ContactMessage {
        val entity = ContactMessage(
            id = null,
            fullName = dto.fullName.trim(),
            email = dto.email.trim(),
            phone = dto.phone.trim(),
            birthDate = dto.birthDate,
            message = dto.message.trim(),
            createdAt = Instant.now()
        )
        return repo.save(entity)
    }

    // DTO para la creación de un nuevo contacto
    data class ContactCreateDto(
        val fullName: String,
        val email: String,
        val phone: String,
        val birthDate: String,
        val message: String
    )
}