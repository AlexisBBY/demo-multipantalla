package com.alexis.demo_multipantalla.repo

import com.alexis.demo_multipantalla.domain.ImageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ImageRepo : JpaRepository<ImageEntity, Long>
