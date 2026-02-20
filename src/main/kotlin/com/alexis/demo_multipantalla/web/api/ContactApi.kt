package com.alexis.demo_multipantalla.web.api

import com.alexis.demo_multipantalla.domain.ContactMessage
import com.alexis.demo_multipantalla.repo.ContactMessageRepo
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.nio.charset.StandardCharsets
import java.time.*
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/contact")
class ContactApi(
    private val repo: ContactMessageRepo
) {

    @GetMapping
    fun list(
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) from: String?,
        @RequestParam(required = false) to: String?,
        @RequestParam(required = false) limit: Int?
    ): List<ContactMessage> {

        val safeLimit = (limit ?: 5).coerceIn(1, 100)

        val fromTs = from?.let {
            LocalDate.parse(it)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
        }

        val toTs = to?.let {
            LocalDate.parse(it)
                .plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
        }

        return repo.search(q?.trim(), fromTs, toTs, PageRequest.of(0, safeLimit))
    }

    @PostMapping
    fun create(@RequestBody body: ContactMessage): ContactMessage {
        return repo.save(body)
    }

    @GetMapping("/dump")
    fun dump(): ResponseEntity<ByteArray> {
        val items = repo.findAll().sortedByDescending { it.id ?: 0L }
        val json = items.toString()

        val ts = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            .withZone(ZoneId.systemDefault())
            .format(Instant.now())

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dump_$ts.json")
            .contentType(MediaType.APPLICATION_JSON)
            .body(json.toByteArray(StandardCharsets.UTF_8))
    }
}