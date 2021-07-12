package app.model

import app.model.Day._
import app.model.WorkingWeekSchedule.DaysOfWeek
import app.model.State.{Close, Open}

final case class WorkingWeek(days: Seq[WorkingDay])

object WorkingWeek {
  def from(schedule: WorkingWeekSchedule): WorkingWeek = {
    import WorkingHour._
    schedule match {
      case DaysOfWeek(sunday, monday, tuesday, wednesday, thursday, friday, saturday) =>
        WorkingWeek (
          Seq(
            WorkingDay(Monday, monday.map(fromHours)),
            WorkingDay(Tuesday, tuesday.map(fromHours)),
            WorkingDay(Wednesday,wednesday.map(fromHours)),
            WorkingDay(Thursday, thursday.map(fromHours)),
            WorkingDay(Friday, friday.map(fromHours)),
            WorkingDay(Saturday, saturday.map(fromHours)),
            WorkingDay(Sunday, sunday.map(fromHours))
          )
        )
    }
  }
}

final case class WorkingDay(day: Day, hours: Seq[WorkingHour])

sealed abstract class Day(val name: String, val id: Int)

object Day {
  final case object Monday extends Day("Monday", 1)
  final case object Tuesday extends Day("Tuesday", 2)
  final case object Wednesday extends Day("Wednesday", 3)
  final case object Thursday extends Day("Thursday", 4)
  final case object Friday extends Day("Friday", 5)
  final case object Saturday extends Day("Saturday", 6)
  final case object Sunday extends Day("Sunday", 7)
}

sealed abstract class WorkingHour(val name: String, val value: Int) extends Product with Serializable

object WorkingHour {
  final case class OpenState(override val value: Int) extends WorkingHour("close", value)
  final case class CloseState(override val value: Int) extends WorkingHour("open", value)

  def fromHours(openingHours: OpeningHours) = {
    openingHours.`type` match {
      case Open => WorkingHour.OpenState(openingHours.value)
      case Close => WorkingHour.CloseState(openingHours.value)
    }
  }
}

final case class ScheduleView[R](view: R)