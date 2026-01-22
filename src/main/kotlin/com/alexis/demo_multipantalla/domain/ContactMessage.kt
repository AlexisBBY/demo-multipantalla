package com.alexis.demo_multipantalla.domain

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "contact_messages")
class ContactMessage(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 50)
    var fullName: String,

    @Column(nullable = false, length = 100)
    var email: String,

    @Column(nullable = false, length = 10)
    var phone: String,

    @Column(nullable = false)
    var birthDate: LocalDate,

    @Column(nullable = false, length = 500)
    var message: String,

    @Column(nullable = false)
    var createdAt: Instant = Instant.now()
)
