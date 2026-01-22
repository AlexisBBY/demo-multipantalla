package com.alexis.demo_multipantalla.web

import com.alexis.demo_multipantalla.service.ImageService
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class ImageController(
    private val imageService: ImageService
) {

    @GetMapping("/images/{id}")
    fun getImage(@PathVariable id: Long): ResponseEntity<ByteArray> {
        val img = imageService.getImage(id) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, img.contentType)
            .body(img.data)
    }
}
