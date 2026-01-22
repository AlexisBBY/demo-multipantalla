package com.alexis.demo_multipantalla

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.alexis"])
class DemoMultipantallaApplication

fun main(args: Array<String>) {
    runApplication<DemoMultipantallaApplication>(*args)
}
