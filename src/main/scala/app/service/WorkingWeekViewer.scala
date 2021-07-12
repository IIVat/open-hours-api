package app.service

import app.model.WorkingHour._
import app.model.{WorkingDay, WorkingHour, WorkingWeek}
import app.utils.Utils
import cats.implicits._

import scala.util.Try

trait WorkingWeekViewer[E, R] {
  type View[R] = Either[E, R]

  def week(w: WorkingWeek): View[R]

  def days(ds: Seq[WorkingDay]): View[R]

  def hours(hs: Seq[WorkingHour]): View[R]

  def hour(v: Int): View[R]
}

object WorkingWeekViewer {
  def view: WorkingWeekViewer[List[String], String] = new WorkingWeekViewer[List[String], String] {

    override def week(w: WorkingWeek): View[String] = days(w.days)

    override def days(ds: Seq[WorkingDay]): View[String] =  {
      val (left, right) = ds.map {
        case day if day.hours.isEmpty => Right(s"${day.day.name}: Closed")
        case day =>
          hours(day.hours)
            .map(hours => s"${day.day.name}: $hours")
      }.partitionMap(identity)

      toEither(left.toList, right.mkString("\n"))
    }

    override def hours(hs: Seq[WorkingHour]): View[String] = {
      val (left, right) = hs.grouped(2).map {
        case Seq(OpenState(v1), CloseState(v2)) =>
          for {
            open <- hour(v1)
            close <- hour(v2)
          } yield s"$open - $close"
        case Seq(CloseState(_), OpenState(_)) =>
          List("[Close, Open] is invalid.").asLeft[String]
        case Seq(CloseState(_), CloseState(_)) =>
          List("[Close, Close] is invalid.").asLeft[String]
        case Seq(OpenState(_), OpenState(_)) =>
          List("[Open, Open] is invalid.").asLeft[String]
        case Seq(state) =>
          List(s"The $state must be in a pair: [Open, Close].").asLeft[String]
      }.toList.partitionMap(identity)

      toEither(left, right.mkString(", "))
    }

    override def hour(v: Int): View[String] = {
      Try(Utils.formatTime(v)).toEither.leftMap(err => List(err.getMessage))
    }
  }

  private def toEither(left: List[List[String]], right: String) = {
    left match {
      case Nil => Right(right)
      case xs => Left(xs.flatten)
    }
  }
}