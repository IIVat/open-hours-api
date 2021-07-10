package app

import WorkingHour._

trait WorkingScheduleViewer[R] {
  def week(w: WorkingWeek): ScheduleView[R] = days(w.days)

  def days(ds: Seq[WorkingDay]): ScheduleView[R]

  def hours(hs: Seq[WorkingHour]): ScheduleView[R]

  def hour(v: Int): ScheduleView[R]
}

object RestaurantScheduleFormatter {
  def format: WorkingScheduleViewer[String] = new WorkingScheduleViewer[String] {
    override def days(ds: Seq[WorkingDay]): ScheduleView[String] = ScheduleView(ds.map {
      case day if day.hours.isEmpty => s"${day.name}: Closed"
      case day => s"${day.name}: ${hours(day.hours)}"
    }.mkString("\n"))

    override def hours(hs: Seq[WorkingHour]): ScheduleView[String] = ScheduleView(hs.grouped(2).collect {
      case Seq(OpenState(v1), CloseState(v2)) => s"${hour(v1)} - ${hour(v2)}"
    }.mkString(", "))

    override def hour(v: Int): ScheduleView[String] = ScheduleView(Utils.formatTime(v))
  }
}