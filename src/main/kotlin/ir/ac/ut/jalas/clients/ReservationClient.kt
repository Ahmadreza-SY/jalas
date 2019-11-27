package ir.ac.ut.jalas.clients

import ir.ac.ut.jalas.clients.models.AvailableRoomsResponse
import ir.ac.ut.jalas.clients.models.ReservationRequest
import ir.ac.ut.jalas.clients.models.ReservationResponse
import org.springframework.cloud.netflix.feign.FeignClient
import org.springframework.web.bind.annotation.*
import java.text.SimpleDateFormat
import java.util.*

@FeignClient(url = "http://213.233.176.40", name = "reservation")
interface ReservationClient {
    @GetMapping("/available_rooms")
    fun getAvailableRooms(@RequestParam start: String, @RequestParam end: String): AvailableRoomsResponse

    @PostMapping("rooms/{roomId}/reserve")
    fun reserveRoom(@PathVariable roomId: Int, @RequestBody request: ReservationRequest): ReservationResponse
}


// TODO date converter
fun main() {
    println(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(Date()))
}