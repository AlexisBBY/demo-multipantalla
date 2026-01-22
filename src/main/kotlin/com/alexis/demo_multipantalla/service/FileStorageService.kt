package com.alexis.demo_multipantalla.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

@Service
class FileStorageService(
    @Value("\${app.upload-dir}") private val uploadDir: String
) {
    fun ensureDir(): Path {
        val dir = Path.of(uploadDir).toAbsolutePath().normalize()
        Files.createDirectories(dir)
        return dir
    }

    fun save(bytes: ByteArray, storedName: String): Path {
        val dir = ensureDir()
        val target = dir.resolve(storedName)
        Files.write(target, bytes)
        return target
    }

    fun load(storedName: String): Path {
        val dir = ensureDir()
        return dir.resolve(storedName).normalize()
    }

    fun randomName(original: String): String {
        val ext = original.substringAfterLast('.', "")
        val base = UUID.randomUUID().toString()
        return if (ext.isBlank()) base else "$base.$ext"
    }
}
