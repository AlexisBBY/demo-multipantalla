package com.alexis.demo_multipantalla.model

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "stored_images")
class StoredImage(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "original_name", nullable = false)
    var originalName: String = "",

    @Column(name = "content_type", nullable = false)
    var contentType: String = "application/octet-stream",

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "data", nullable = false)
    var data: ByteArray = byteArrayOf()
)
