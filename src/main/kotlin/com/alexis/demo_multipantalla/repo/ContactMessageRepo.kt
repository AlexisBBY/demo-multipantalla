package com.alexis.demo_multipantalla.repo

import com.alexis.demo_multipantalla.domain.ContactMessage
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface ContactMessageRepo : JpaRepository<ContactMessage, Long> {

    // Consulta personalizada con parámetros q (texto), from y to (fechas)
    @Query("SELECT cm FROM ContactMessage cm WHERE (:q IS NULL OR LOWER(cm.fullName) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(cm.email) LIKE LOWER(CONCAT('%', :q, '%')) OR cm.phone LIKE CONCAT('%', :q, '%')) " +
            "AND (:from IS NULL OR cm.createdAt >= :from) " +
            "AND (:to IS NULL OR cm.createdAt < :to) " +
            "ORDER BY cm.id DESC")
    fun search(
        @Param("q") q: String?,
        @Param("from") from: Instant?,
        @Param("to") to: Instant?,
        pageable: Pageable
    ): List<ContactMessage>
}