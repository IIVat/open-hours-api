package app

import app.Schedule.RestaurantSchedule
import app.WorkingDay.{Friday, Monday, Saturday, Sunday, Thursday, Tuesday, Wednesday}

case class WorkingWeek(days: Seq[WorkingDay])

object WorkingWeek {
  def from(schedule: Schedule): WorkingWeek = {
    import WorkingHour._
    schedule match {
      case RestaurantSchedule(sunday, monday, tuesday, wednesday, thursday, friday, saturday) =>
        WorkingWeek (
          Seq(
            Monday(monday.hours.map(fromHours)),
            Tuesday(tuesday.hours.map(fromHours)),
            Wednesday(wednesday.hours.map(fromHours)),
            Thursday(thursday.hours.map(fromHours)),
            Friday(friday.hours.map(fromHours)),
            Saturday(saturday.hours.map(fromHours)),
            Sunday(sunday.hours.map(fromHours))
          )
        )
    }
  }
}

sealed abstract class WorkingDay(val name: String, val hours: Seq[WorkingHour]) extends Product with Serializable

object WorkingDay {
  case class Monday(override val hours: Seq[WorkingHour]) extends WorkingDay("Monday", hours)
  case class Tuesday(override val hours: Seq[WorkingHour]) extends WorkingDay("Tuesday", hours)
  case class Wednesday(override val hours: Seq[WorkingHour]) extends WorkingDay("Wednesday", hours)
  case class Thursday(override val hours: Seq[WorkingHour]) extends WorkingDay("Thursday", hours)
  case class Friday(override val hours: Seq[WorkingHour]) extends WorkingDay("Friday", hours)
  case class Saturday(override val hours: Seq[WorkingHour]) extends WorkingDay("Saturday", hours)
  case class Sunday(override val hours: Seq[WorkingHour]) extends WorkingDay("Sunday", hours)

  def copy(day: WorkingDay, hours: Seq[WorkingHour]): WorkingDay = day match {
    case d: Monday => d.copy(hours = hours)
    case d: Tuesday => d.copy(hours = hours)
    case d: Wednesday => d.copy(hours = hours)
    case d: Thursday => d.copy(hours = hours)
    case d: Friday => d.copy(hours = hours)
    case d: Saturday => d.copy(hours = hours)
    case d: Sunday => d.copy(hours = hours)
  }
}
sealed abstract class Day(val name: String)

object Day {
  case object Monday extends Day("Monday")
  case object Tuesday extends Day("Tuesday")
  case object Wednesday extends Day("Wednesday")
  case object Thursday extends Day("Thursday")
  case object Friday extends Day("Friday")
  case object Saturday extends Day("Saturday")
  case object Sunday extends Day("Sunday")
}

sealed abstract class WorkingHour(val name: String) extends Product with Serializable

object WorkingHour {
  case class OpenState(value: Int) extends WorkingHour("close")
  case class CloseState(value: Int) extends WorkingHour("open")

  def fromHours(openingHours: OpeningHours) = {
    openingHours.`type` match {
      case Open => WorkingHour.OpenState(openingHours.value)
      case Close => WorkingHour.CloseState(openingHours.value)
    }
  }
}

case class ScheduleView[R](view: R)

object Test extends App {
  import WorkingDay._
  import WorkingHour._
  val week = WorkingWeek(Seq(
    Monday(Seq(OpenState(3600), CloseState(7200), OpenState(3600), CloseState(7200))),
    Tuesday(Seq(OpenState(3600), CloseState(7200), OpenState(3600), CloseState(7200))),
    Wednesday(Seq.empty)
  ))
  println(RestaurantScheduleFormatter.format.week(week))
}