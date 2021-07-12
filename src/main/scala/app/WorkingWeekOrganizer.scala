package app

import app.model.WorkingHour.{CloseState, OpenState}
import app.model.{WorkingHour, WorkingWeek, WorkingWeekSchedule}
import app.model.WorkingWeekSchedule.DaysOfWeek

import scala.annotation.tailrec

trait WorkingWeekOrganizer[T <: WorkingWeekSchedule] {
  def organize(week: T): WorkingWeek
}

object WorkingWeekOrganizer {
  def organizeWorkingWeek: WorkingWeekOrganizer[DaysOfWeek] = new WorkingWeekOrganizer[DaysOfWeek] {
    private type Day = String

    override def organize(schedule: DaysOfWeek): WorkingWeek = {
      val week = WorkingWeek.from(schedule)
      val (closed, opened) = week.days.partition(_.hours.isEmpty)

      val list = opened
        .flatMap { day =>
          day.hours.map(hour => day.day.name -> hour).sortBy(_._2.value)
        }

      val days =
        fixWorkingHoursOrder(list.toList)
          .view
          .groupBy(_._1)
          .view
          .map {
            case (day, hours) =>
              day -> hours.map(_._2)
          }
          .flatMap { case (day, hours) =>
            week.days.find(_.day.name == day)
              .map(d => d.copy(hours = hours.toList))
          }


      WorkingWeek((days.toSeq ++ closed).sortBy(_.day.id))
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

        def isFirstElemInCloseState(h: WorkingHour) = {
          stack.size == 1 &&
            stack.headOption.exists(_._2.isInstanceOf[CloseState]) &&
            !h.isInstanceOf[CloseState]
        }

        elems match {
          case Nil => stack
//          case h :: tail if isFirstElemInCloseState(h._2) =>
//            recFix(tail ::: List(stack.head), h :: stack.tail)
          case (day, hour) :: tail if isRequiredReorganizing(day, hour) =>
            val prev = stack.head
            recFix(tail, (prev._1, hour) :: (prev._1, stack.head._2) :: stack.tail)
          case h :: tail => recFix(tail, h :: stack)
        }
      }

      recFix(hours, List.empty).reverse
    }
  }
}

