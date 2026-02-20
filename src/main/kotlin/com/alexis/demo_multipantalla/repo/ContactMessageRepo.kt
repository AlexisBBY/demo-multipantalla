package com.alexis.demo_multipantalla.repo

import com.alexis.demo_multipantalla.domain.ContactMessage
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface ContactMessageRepo : JpaRepository<ContactMessage, Long> {

    @Query(
        """
        SELECT c FROM ContactMessage c
        WHERE
          (
            :q IS NULL OR :q = '' OR
            LOWER(c.fullName) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(c.email) LIKE LOWER(CONCAT('%', :q, '%')) OR
            c.phone LIKE CONCAT('%', :q, '%')
          )
          AND (:fromTs IS NULL OR c.createdAt >= :fromTs)
          AND (:toTs IS NULL OR c.createdAt < :toTs)
        ORDER BY c.id DESC
        """
    )
    fun search(
        @Param("q") q: String?,
        @Param("fromTs") fromTs: Instant?,
        @Param("toTs") toTs: Instant?,
        pageable: Pageable
    ): List<ContactMessage>
}