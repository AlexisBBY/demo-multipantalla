package com.alexis.demo_multipantalla.web.api

import com.alexis.demo_multipantalla.domain.ContactMessage
import com.alexis.demo_multipantalla.repo.ContactMessageRepo
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.nio.charset.StandardCharsets
import java.time.*
import java.time.format.DateTimeFormatter

@Validated
@RestController
@RequestMapping("/api/contact")
class ContactApi(
    private val repo: ContactMessageRepo,
    private val objectMapper: ObjectMapper
) {

    data class ContactCreateDto(
        @field:NotBlank(message = "El nombre es obligatorio")
        @field:Size(max = 70, message = "Nombre máximo 70 caracteres")
        val fullName: String,

        @field:NotBlank(message = "El correo es obligatorio")
        @field:Email(message = "Correo inválido")
        @field:Size(max = 254, message = "Correo máximo 254 caracteres")
        val email: String,

        @field:NotBlank(message = "El teléfono es obligatorio")
        @field:Size(max = 20, message = "Teléfono máximo 20 caracteres")
        @field:Pattern(
            regexp = "^[0-9+()\\-\\s]{7,20}$",
            message = "Teléfono inválido (solo números y +()- espacios)"
        )
        val phone: String,

        val birthDate: LocalDate,

        @field:NotBlank(message = "El mensaje es obligatorio")
        @field:Size(max = 500, message = "Mensaje máximo 500 caracteres")
        val message: String
    )

    @GetMapping
    fun list(
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) from: String?,
        @RequestParam(required = false) to: String?,
        @RequestParam(required = false) limit: Int?
    ): List<ContactMessage> {

        val safeLimit = (limit ?: 5).coerceIn(1, 100)

        val fromTs = from?.takeIf { it.isNotBlank() }?.let {
            LocalDate.parse(it).atStartOfDay(ZoneId.systemDefault()).toInstant()
        }

        val toTs = to?.takeIf { it.isNotBlank() }?.let {
            LocalDate.parse(it).plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        }

        val qClean = q?.trim()

        return repo.search(qClean, fromTs, toTs, PageRequest.of(0, safeLimit))
    }

    @PostMapping
    fun create(@Valid @RequestBody dto: ContactCreateDto): ContactMessage {
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

    @GetMapping("/dump")
    fun dump(): ResponseEntity<ByteArray> {
        val items = repo.findAll().sortedByDescending { it.id ?: 0L }
        val bytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(items)

        val ts = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            .withZone(ZoneId.systemDefault())
            .format(Instant.now())

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contact_dump_$ts.json")
            .contentType(MediaType.APPLICATION_JSON)
            .contentLength(bytes.size.toLong())
            .body(bytes)
    }
}