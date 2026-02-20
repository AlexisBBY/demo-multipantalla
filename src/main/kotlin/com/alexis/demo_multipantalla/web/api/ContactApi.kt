package com.alexis.demo_multipantalla.web.api

import com.alexis.demo_multipantalla.domain.ContactMessage
import com.alexis.demo_multipantalla.repo.ContactMessageRepo
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/contact")
class ContactApi(
    private val repo: ContactMessageRepo
) {
    private val NAME_REGEX = Regex("""^[\p{L}][\p{L}\s'\-]{1,69}$""")
    private val EMAIL_REGEX = Regex(
        """^[A-Za-z0-9._%+\-]{1,64}@[A-Za-z0-9](?:[A-Za-z0-9\-]{0,61}[A-Za-z0-9])?(?:\.[A-Za-z0-9](?:[A-Za-z0-9\-]{0,61}[A-Za-z0-9])?)*\.[A-Za-z]{2,63}$"""
    )
    private val PHONE_REGEX = Regex("""^[0-9+\-\s()]{7,20}$""")
    private val MAX_MESSAGE = 500

    // ✅ GET /api/contact?limit=5
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun list(@RequestParam("limit", required = false) limit: Int?): List<ContactMessageDto> {
        val items = when {
            limit == null -> repo.findAll().sortedByDescending { it.id ?: 0L }
            limit <= 0 -> emptyList()
            limit <= 5 -> repo.findTop5ByOrderByIdDesc().take(limit)
            else -> repo.findAll().sortedByDescending { it.id ?: 0L }.take(limit)
        }
        return items.map { it.toDto() }
    }

    // ✅ POST /api/contact (JSON)
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun create(@RequestBody body: ContactCreateRequest): ResponseEntity<Any> {
        val fullName = clean(body.fullName)
        val email = clean(body.email).lowercase()
        val phone = clean(body.phone)
        val message = clean(body.message)
        val birthDate = body.birthDate

        val errors = linkedMapOf<String, String>()

        // Anti-HTML chars
        if (containsHtmlLike(fullName) || containsHtmlLike(email) || containsHtmlLike(phone) || containsHtmlLike(message)) {
            errors["global"] = "No se permiten etiquetas HTML ni caracteres extraños."
        }

        if (fullName.isBlank() || !NAME_REGEX.matches(fullName)) {
            errors["fullName"] = "Nombre inválido (solo letras y espacios)."
        }

        if (email.isBlank() || !EMAIL_REGEX.matches(email)) {
            errors["email"] = "Email inválido (ej: usuario@dominio.com.mx)."
        }

        if (phone.isBlank() || !PHONE_REGEX.matches(phone)) {
            errors["phone"] = "Teléfono inválido (usa números y + - ( ))."
        }

        if (message.isBlank()) {
            errors["message"] = "Escribe un mensaje."
        } else if (message.length > MAX_MESSAGE) {
            errors["message"] = "El mensaje no debe pasar de $MAX_MESSAGE caracteres."
        }

        if (birthDate == null) {
            errors["birthDate"] = "La fecha de nacimiento es obligatoria."
        } else {
            val today = LocalDate.now()
            val age = Period.between(birthDate, today).years
            if (age !in 18..120) {
                errors["birthDate"] = "Debes tener entre 18 y 120 años."
            }
        }

        if (errors.isNotEmpty()) {
            return ResponseEntity.badRequest().body(mapOf("ok" to false, "errors" to errors))
        }

        val saved = repo.save(
            ContactMessage(
                fullName = fullName,
                email = email,
                phone = phone,
                birthDate = birthDate!!,
                message = message
            )
        )

        return ResponseEntity.status(201).body(mapOf("ok" to true, "item" to saved.toDto()))
    }

    // ✅ GET /api/contact/dump -> descarga JSON
    @GetMapping("/dump", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun dump(): ResponseEntity<ByteArray> {
        val items = repo.findAll().sortedByDescending { it.id ?: 0L }.map { it.toDto() }
        val json = toJson(items)

        val ts = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            .withZone(ZoneId.systemDefault())
            .format(Instant.now())

        val bytes = json.toByteArray(StandardCharsets.UTF_8)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contact_messages_dump_$ts.json")
            .contentType(MediaType.APPLICATION_JSON)
            .body(bytes)
    }

    // -------- DTOs --------
    data class ContactCreateRequest(
        val fullName: String? = null,
        val email: String? = null,
        val phone: String? = null,
        val birthDate: LocalDate? = null,
        val message: String? = null,
    )

    data class ContactMessageDto(
        val id: Long,
        val fullName: String,
        val email: String,
        val phone: String,
        val birthDate: LocalDate,
        val message: String,
        val createdAt: Instant,
    )

    private fun ContactMessage.toDto() = ContactMessageDto(
        id = this.id ?: 0L,
        fullName = this.fullName,
        email = this.email,
        phone = this.phone,
        birthDate = this.birthDate,
        message = this.message,
        createdAt = this.createdAt
    )

    // -------- Helpers --------
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

    // JSON simple (sin dependencias extra)
    private fun toJson(items: List<ContactMessageDto>): String {
        fun esc(s: String) = s
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")

        val sb = StringBuilder()
        sb.append('[')
        items.forEachIndexed { i, it ->
            if (i > 0) sb.append(',')
            sb.append('{')
            sb.append("\"id\":").append(it.id).append(',')
            sb.append("\"fullName\":\"").append(esc(it.fullName)).append("\",")
            sb.append("\"email\":\"").append(esc(it.email)).append("\",")
            sb.append("\"phone\":\"").append(esc(it.phone)).append("\",")
            sb.append("\"birthDate\":\"").append(it.birthDate.toString()).append("\",")
            sb.append("\"message\":\"").append(esc(it.message)).append("\",")
            sb.append("\"createdAt\":\"").append(it.createdAt.toString()).append("\"")
            sb.append('}')
        }
        sb.append(']')
        return sb.toString()
    }
}