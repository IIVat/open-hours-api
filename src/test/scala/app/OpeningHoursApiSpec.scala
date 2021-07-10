package app

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import akka.http.scaladsl.model.{ContentTypes, StatusCodes}

import scala.io.Source
import scala.util.Using
import io.circe.parser._
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.syntax._
import app.Schedule.RestaurantSchedule

class OpeningHoursApiSpec extends AnyWordSpec with Matchers with ScalatestRouteTest {

  import RestaurantStateCodecs._

  val scheduleFormatter = RestaurantScheduleFormatter.format

  val route = new OpeningHoursRouter[RestaurantSchedule, String](scheduleFormatter).route

  val json = parse(readLines("test.json")).map(_.noSpaces).getOrElse("")

  "OpeningHoursApi" should {
    "return valid opening hours of a restaurant" in {
      val request = Post("/opening-hours").withEntity(ContentTypes.`application/json`, json)

      request ~> route ~> check {
        status shouldEqual StatusCodes.OK

        contentType shouldEqual ContentTypes.`application/json`

        entityAs[ScheduleView[String]] shouldEqual ScheduleView("")
      }
    }

    "return Day:Closed if restaurant is closed in that day" in {
      val closedWholeWeek =  RestaurantSchedule().asJson.noSpaces
      val response =
        Seq("Sunday", "Monday", "Tuesday","Wednesday","Thursday","Friday", "Saturday")
         .map(d => s"$d: Closed")

      val request = Post("/opening-hours").withEntity(ContentTypes.`application/json`, closedWholeWeek)

      request ~> route ~> check {
        status shouldEqual StatusCodes.OK

        contentType shouldEqual ContentTypes.`application/json`

        entityAs[ScheduleView[String]] shouldEqual ScheduleView(response.mkString("\n"))
      }
    }
  }

  private def readLines(fileName: String): String = {
    Using(Source.fromFile(getClass.getClassLoader.getResource(fileName).toURI))(_.getLines().mkString(""))
      .getOrElse(throw new RuntimeException("File not found"))
  }
}