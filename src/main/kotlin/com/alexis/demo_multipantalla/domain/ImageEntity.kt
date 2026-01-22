package com.alexis.demo_multipantalla.domain

import jakarta.persistence.*

@Entity
@Table(name = "images")
data class ImageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "original_name", nullable = false)
    val originalName: String,

    @Column(name = "file_name", nullable = false)
    val fileName: String
)
