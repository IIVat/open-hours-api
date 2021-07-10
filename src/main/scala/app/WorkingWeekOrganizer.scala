package app

import WorkingHour._

import scala.annotation.tailrec

object WorkingWeekOrganizer {
  type Day = String

  def organize(week: WorkingWeek): WorkingWeek = {

    val (closed, opened) = week.days.partition(_.hours.isEmpty)

    val list = opened
      .flatMap { day =>
        day.hours.map(hour => day.name -> hour)
      }


    val days =
      fixWorkingHoursOrder(list.toList)
        .view
        .groupBy(_._1)
        .view
        .values
        .flatMap { hours =>
          val hs = hours.map(_._2).toList
          week.days
            .map(WorkingDay.copy(_, hs))
        }

    WorkingWeek(days.toSeq)
  }

  private def fixWorkingHoursOrder(hours: List[(Day, WorkingHour)]): List[(Day, WorkingHour)] = {
    @tailrec
    def recFix(elems: List[(Day, WorkingHour)],
               stack: List[(Day, WorkingHour)]
              ): List[(Day, WorkingHour)] = {

      def isRequiredReorganizing(day: Day, hour: WorkingHour) = {
        stack.nonEmpty &&
          hour.isInstanceOf[CloseState] &&
          stack.headOption.map(_._2).exists(_.isInstanceOf[OpenState]) &&
          !stack.headOption.map(_._1).contains(day)
      }

      def isCloseStateFirstElem = {
        stack.size == 1 && stack.headOption.exists(_._2.isInstanceOf[CloseState])
      }

      elems match {
        case Nil => stack
        case h :: tail if isCloseStateFirstElem =>
          recFix(tail ::: List(stack.head), h :: stack.tail)
        case (day, hour) :: tail if isRequiredReorganizing(day, hour) =>
          val prev = stack.head
          recFix(tail, (day, hour) :: (prev._1, hour) :: stack.tail)
        case h :: tail => recFix(tail, h :: stack)
      }
    }

    recFix(hours, List.empty)
  }
}
