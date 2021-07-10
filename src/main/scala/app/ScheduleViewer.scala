package app

import cats.data.State
import cats.implicits._
import app.Schedule._

import java.time.{Instant, LocalTime}
import java.time.format.DateTimeFormatter
import scala.collection.mutable
//
//trait ScheduleViewer[T <: Schedule] {
//  def view(s: T): ScheduleView
//}



object Utils {
  def formatTime(value: Int): String = {
    val timeFormatter = DateTimeFormatter.ofPattern("h a")
    val time = LocalTime.ofSecondOfDay(value)

    timeFormatter.format(time).toUpperCase
  }
}

//case class OpeningHoursView(value: String)
//
//sealed trait OpeningHoursT
//
//object OpeningHoursT {
//  case class RequiredOpeningHours() extends OpeningHoursT
//
//  case class ProposedOpeningHours() extends OpeningHoursT
//}



//object ScheduleViewer {
//  final def apply[T <: OpeningHoursT: ScheduleViewer]: ScheduleViewer[T] =
//    implicitly[ScheduleViewer[T]]
//}

