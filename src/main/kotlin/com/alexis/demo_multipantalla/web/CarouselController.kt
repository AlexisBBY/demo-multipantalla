package com.alexis.demo_multipantalla.web

import com.alexis.demo_multipantalla.service.ImageService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@Controller
class CarouselController(
    private val imageService: ImageService
) {
    private val MAX_BYTES = 5L * 1024L * 1024L // 5MB

    private val ALLOWED_MIME = setOf(
        "image/png",
        "image/jpeg",
        "image/gif",
        "image/webp"
    )

    private val ALLOWED_EXT = setOf("png", "jpg", "jpeg", "gif", "webp")

    @GetMapping("/carousel")
    fun view(model: Model): String {
        model.addAttribute("images", imageService.findAll())
        return "carousel"
    }

    @PostMapping("/images/upload")
    fun upload(@RequestParam("file") file: MultipartFile, model: Model): String {
        val error = validateImage(file)

        if (error != null) {
            // Recargar la galería y mostrar error en la misma vista
            model.addAttribute("error", error)
            model.addAttribute("images", imageService.findAll())
            return "carousel"
        }

        imageService.saveImage(file)
        return "redirect:/carousel"
    }

    private fun validateImage(file: MultipartFile?): String? {
        if (file == null || file.isEmpty) return "Selecciona una imagen."

        if (file.size <= 0) return "Archivo inválido."
        if (file.size > MAX_BYTES) return "La imagen excede 5 MB."

        val originalName = (file.originalFilename ?: "").trim()
        val ext = extractExt(originalName)
        if (ext !in ALLOWED_EXT) {
            return "Formato no permitido. Usa PNG, JPG/JPEG, GIF o WEBP."
        }

        val contentType = (file.contentType ?: "").lowercase()
        if (contentType !in ALLOWED_MIME) {
            return "Tipo de archivo inválido. Sube una imagen real (PNG/JPG/GIF/WEBP)."
        }

        // Validación real por firma (magic bytes)
        val bytes = try {
            file.bytes
        } catch (e: Exception) {
            return "No se pudo leer el archivo."
        }

        if (!looksLikeAllowedImage(bytes)) {
            return "El archivo no parece una imagen válida (contenido incorrecto)."
        }

        return null
    }

    private fun extractExt(filename: String): String {
        val clean = filename.substringAfterLast('/').substringAfterLast('\\')
        val i = clean.lastIndexOf('.')
        if (i < 0 || i == clean.length - 1) return ""
        return clean.substring(i + 1).lowercase()
    }

    private fun looksLikeAllowedImage(bytes: ByteArray): Boolean {
        if (bytes.size < 12) return false

        // PNG: 89 50 4E 47 0D 0A 1A 0A
        val isPng =
            bytes.size >= 8 &&
            bytes[0] == 0x89.toByte() &&
            bytes[1] == 0x50.toByte() &&
            bytes[2] == 0x4E.toByte() &&
            bytes[3] == 0x47.toByte() &&
            bytes[4] == 0x0D.toByte() &&
            bytes[5] == 0x0A.toByte() &&
            bytes[6] == 0x1A.toByte() &&
            bytes[7] == 0x0A.toByte()

        // JPEG: FF D8 ... FF D9 (al final)
        val isJpeg =
            bytes.size >= 4 &&
            bytes[0] == 0xFF.toByte() &&
            bytes[1] == 0xD8.toByte()

        // GIF: "GIF87a" o "GIF89a"
        val isGif =
            bytes.size >= 6 &&
            bytes[0] == 'G'.code.toByte() &&
            bytes[1] == 'I'.code.toByte() &&
            bytes[2] == 'F'.code.toByte() &&
            bytes[3] == '8'.code.toByte() &&
            (bytes[4] == '7'.code.toByte() || bytes[4] == '9'.code.toByte()) &&
            bytes[5] == 'a'.code.toByte()

        // WEBP: "RIFF" .... "WEBP"
        val isWebp =
            bytes.size >= 12 &&
            bytes[0] == 'R'.code.toByte() &&
            bytes[1] == 'I'.code.toByte() &&
            bytes[2] == 'F'.code.toByte() &&
            bytes[3] == 'F'.code.toByte() &&
            bytes[8] == 'W'.code.toByte() &&
            bytes[9] == 'E'.code.toByte() &&
            bytes[10] == 'B'.code.toByte() &&
            bytes[11] == 'P'.code.toByte()

        return isPng || isJpeg || isGif || isWebp
    }
}
