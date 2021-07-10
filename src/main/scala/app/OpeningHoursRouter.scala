package app

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.{Decoder, Encoder}
import io.circe.syntax._
import app.Schedule._

class OpeningHoursRouter[T <: Schedule : Decoder, R: Encoder](viewer: WorkingScheduleViewer[R]) {

  import RestaurantStateCodecs._

  lazy val route =
    path("opening-hours") {
      post {
        entity(as[T]) { schedule =>
          val workingWeek = WorkingWeek.from(schedule)
          val organized = WorkingWeekOrganizer.organize(workingWeek)
          val responseEntity = viewer.week(organized).asJson.noSpaces
          complete(HttpEntity(ContentTypes.`application/json`, responseEntity))
        }
      }
    }
}
