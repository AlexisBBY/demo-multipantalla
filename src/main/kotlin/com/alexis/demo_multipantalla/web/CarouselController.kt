package com.alexis.demo_multipantalla.web

import com.alexis.demo_multipantalla.service.ImageService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@Controller
class CarouselController(
    private val imageService: ImageService
) {

    @GetMapping("/carousel")
    fun view(model: Model): String {
        model.addAttribute("images", imageService.findAll())
        return "carousel"
    }

    @PostMapping("/images/upload")
    fun upload(@RequestParam("file") file: MultipartFile): String {
        imageService.saveImage(file)
        return "redirect:/carousel"
    }
}
