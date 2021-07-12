package app

import app.model.WorkingWeekSchedule

class WorkingWeekService[T <: WorkingWeekSchedule, E, R](organizer: WorkingWeekOrganizer[T],
                                                         viewer: WorkingWeekViewer[E, R]) {
  def view(schedule: T): Either[E, R] = {
    val organizedWeek = organizer.organize(schedule)
    viewer.week(organizedWeek)
  }
}
