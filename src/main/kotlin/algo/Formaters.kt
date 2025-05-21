package pl.zulov.algo

import java.text.DecimalFormat
import java.time.format.DateTimeFormatter

val red = "\u001B[31m"
val green = "\u001B[32m"
val reset = "\u001B[0m"


val df = DecimalFormat("0.0")
val df2 = DecimalFormat("0.00")
val dfP = DecimalFormat("00.0")

val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")