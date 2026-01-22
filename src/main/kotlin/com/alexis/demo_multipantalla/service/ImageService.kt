package com.alexis.demo_multipantalla.service

import com.alexis.demo_multipantalla.model.StoredImage
import com.alexis.demo_multipantalla.repo.StoredImageRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ImageService(
    private val repo: StoredImageRepository
) {

    fun findAll(): List<ImageItem> =
        repo.findAll()
            .sortedByDescending { it.id ?: 0 }
            .map { ImageItem(id = it.id!!, originalName = it.originalName) }

    fun saveImage(file: MultipartFile) {
        val img = StoredImage(
            originalName = file.originalFilename ?: "sin-nombre",
            contentType = file.contentType ?: "application/octet-stream",
            data = file.bytes
        )
        repo.save(img)
    }

    fun getImage(id: Long): StoredImage? =
        repo.findById(id).orElse(null)
}
