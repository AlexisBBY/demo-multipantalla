package com.alexis.demo_multipantalla.domain

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "registrations")
class Registration(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true, length = 50)
    var username: String,

    @Column(nullable = false)
    var createdAt: Instant = Instant.now()
)
