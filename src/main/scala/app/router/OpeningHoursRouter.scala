package app.router

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import app.model.WorkingWeekSchedule
import app.service.WorkingWeekService
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}

class OpeningHoursRouter[T <: WorkingWeekSchedule : Decoder, E: Encoder, R: Encoder](viewer: WorkingWeekService[T, E, R]) {
  lazy val route =
    path("opening-hours") {
      post {
        entity(as[T]) { schedule =>
          viewer.view(schedule).fold(
            error => complete(StatusCodes.BadRequest, error.asJson.noSpaces),
            view => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, view.toString))
          )
        }
      }
    }
}
