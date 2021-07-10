package app

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.{Decoder, Encoder}
import app.Schedule._

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object OpeningHoursApp {
  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")

    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    val scheduleFormatter = RestaurantScheduleFormatter.format

    val route = new OpeningHoursRouter[RestaurantSchedule, String](scheduleFormatter).route

    val bindingFuture = Http().newServerAt("localhost", 9000).bind(route)

    println(s"Server online at http://localhost:9000/\nPress RETURN to stop...")

    StdIn.readLine() // let it run until user presses return

    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}