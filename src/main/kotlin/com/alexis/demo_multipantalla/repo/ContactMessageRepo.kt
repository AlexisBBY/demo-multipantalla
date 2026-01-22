package com.alexis.demo_multipantalla.repo

import com.alexis.demo_multipantalla.domain.ContactMessage
import org.springframework.data.jpa.repository.JpaRepository

interface ContactMessageRepo : JpaRepository<ContactMessage, Long>
