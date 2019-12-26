package ir.ac.ut.jalas.controllers

import ir.ac.ut.jalas.services.ReportService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/report")
class ReportController(val reportService: ReportService) {

    @GetMapping
    fun getGeneralReport() = reportService.getGeneralReport()

}
