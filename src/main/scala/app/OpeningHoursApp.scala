package app

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.{MalformedRequestContentRejection, RejectionHandler, Route}
import app.model.WorkingWeekSchedule.DaysOfWeek
import io.circe.generic.auto._
import app.router.OpeningHoursRouter
import app.service.{WorkingWeekOrganizer, WorkingWeekService, WorkingWeekViewer}

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object OpeningHoursApp {
  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")

    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    implicit def rejectionHandler = RejectionHandler.newBuilder()
      .handle {
        case MalformedRequestContentRejection(error, cause) =>
          complete(HttpResponse(BadRequest, entity = s"Invalid json: $error"))
      }.result()

    type Error = List[String]

    val organizer = WorkingWeekOrganizer.organizeWorkingWeek
    val viewer = WorkingWeekViewer.view
    val service = new WorkingWeekService(organizer, viewer)

    val route = Route.seal(new OpeningHoursRouter[DaysOfWeek, Error, String](service).route)

    val bindingFuture = Http().newServerAt("localhost", 9000).bind(route)

    println(s"Server online at http://localhost:9000/\nPress RETURN to stop...")

    StdIn.readLine() // let it run until user presses return

    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}