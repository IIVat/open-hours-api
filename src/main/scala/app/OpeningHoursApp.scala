package app

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import app.model.WorkingWeekSchedule.DaysOfWeek
import io.circe.generic.auto._
import app._

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object OpeningHoursApp {
  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")

    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    type Error = List[String]

    val organizer = WorkingWeekOrganizer.organizeWorkingWeek
    val viewer = WorkingWeekViewer.view
    val service = new WorkingWeekService(organizer, viewer)

    val route = new OpeningHoursRouter[DaysOfWeek, Error, String](service).route

    val bindingFuture = Http().newServerAt("localhost", 9000).bind(route)

    println(s"Server online at http://localhost:9000/\nPress RETURN to stop...")

    StdIn.readLine() // let it run until user presses return

    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}