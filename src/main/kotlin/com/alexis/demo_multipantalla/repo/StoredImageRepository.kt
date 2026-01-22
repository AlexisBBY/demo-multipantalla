package com.alexis.demo_multipantalla.repo

import com.alexis.demo_multipantalla.model.StoredImage
import org.springframework.data.jpa.repository.JpaRepository

interface StoredImageRepository : JpaRepository<StoredImage, Long>
