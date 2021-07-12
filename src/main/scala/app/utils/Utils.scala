package app.utils

import java.time.LocalTime
import java.time.format.DateTimeFormatter

object Utils {
  def formatTime(value: Int): String = {
    val timeFormatter = DateTimeFormatter.ofPattern("h.mm a")
    val time = LocalTime.ofSecondOfDay(value)

    timeFormatter.format(time).toUpperCase
  }
}
