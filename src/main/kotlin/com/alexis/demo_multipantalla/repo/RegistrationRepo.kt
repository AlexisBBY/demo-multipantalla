package com.alexis.demo_multipantalla.repo

import com.alexis.demo_multipantalla.domain.Registration
import org.springframework.data.jpa.repository.JpaRepository

interface RegistrationRepo : JpaRepository<Registration, Long> {
    fun existsByUsername(username: String): Boolean
}
