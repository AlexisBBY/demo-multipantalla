package com.alexis.demo_multipantalla.domain

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "contact_message")
class ContactMessage(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "full_name", nullable = false, length = 70)
    var fullName: String = "",

    @Column(nullable = false, length = 254)
    var email: String = "",

    @Column(nullable = false, length = 20)
    var phone: String = "",

    @Column(name = "birth_date", nullable = false)
    var birthDate: LocalDate = LocalDate.now(),

    @Column(nullable = false, length = 500)
    var message: String = "",

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
)