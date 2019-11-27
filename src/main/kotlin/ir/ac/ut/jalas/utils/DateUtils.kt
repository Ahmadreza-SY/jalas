package ir.ac.ut.jalas.utils

import java.text.SimpleDateFormat
import java.util.*

fun Date.toReserveFormat() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(this)
