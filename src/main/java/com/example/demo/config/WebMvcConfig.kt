package com.example.pasteleriaapp.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
open class WebMvcConfig : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {

        registry.addResourceHandler(
            "/*.png",
            "/*.jpg",
            "/*.jpeg")
            .addResourceLocations("classpath:/static/")

        registry.addResourceHandler("/imagenes/**")
            .addResourceLocations("classpath:/static/")
    }
}