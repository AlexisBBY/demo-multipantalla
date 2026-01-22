package com.alexis.demo_multipantalla.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Paths

@Configuration
class WebConfig(
    @Value("\${app.upload-dir}") private val uploadDir: String
) : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val path = Paths.get(uploadDir).toAbsolutePath().toUri().toString()
        registry.addResourceHandler("/images/**")
            .addResourceLocations(path)
    }
}
