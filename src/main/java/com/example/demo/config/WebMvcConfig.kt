package com.example.pasteleriaapp.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
open class WebMvcConfig : WebMvcConfigurer { // ⭐ AGREGA LA PALABRA CLAVE 'open' AQUÍ ⭐

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {

        // 1. Mapea archivos en la raíz de static (ej. /torta.png)
        registry.addResourceHandler(
            "/*.png",
            "/*.jpg",
            "/*.jpeg")
            .addResourceLocations("classpath:/static/")

        // 2. Mapea archivos referenciados con el prefijo /imagenes/ (ej. /imagenes/torta.png)
        registry.addResourceHandler("/imagenes/**")
            .addResourceLocations("classpath:/static/")
    }
}