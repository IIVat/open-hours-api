package app

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import app.model.WorkingWeekSchedule.DaysOfWeek
import app.model.ScheduleView

import scala.io.Source
import scala.util.Using
import io.circe.parser._
import io.circe.syntax._
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

class OpeningHoursApiSpec extends AnyWordSpec with Matchers with ScalatestRouteTest {

  import app.model.State._

  type Error = List[String]

  val organizer = WorkingWeekOrganizer.organizeWorkingWeek
  val viewer = WorkingWeekViewer.view
  val service = new WorkingWeekService(organizer, viewer)

  val route = new OpeningHoursRouter[DaysOfWeek, Error, String](service).route

  val json = parse(readLines("test.json")).map(_.noSpaces).getOrElse("")

  "OpeningHoursApi" should {
    "return valid opening hours of a restaurant" in {
      val request = Post("/opening-hours").withEntity(ContentTypes.`application/json`, json)

      request ~> route ~> check {
        status shouldEqual StatusCodes.OK

        contentType shouldEqual ContentTypes.`application/json`

        entityAs[ScheduleView[String]] shouldEqual
          ScheduleView("Monday: 1 AM - 6 PM\nTuesday: 1 AM - 6 PM\nWednesday: 1 AM - 6 PM\nThursday: 1 AM - 6 PM" +
            "\nFriday: 1 AM - 6 PM, 7 PM - 2 AM\nSaturday: 9 AM - 11 AM, 4 PM - 11 PM\nSunday: 1 AM - 6 PM")
      }
    }

    "return Day:Closed if restaurant is closed in that day" in {
      val closedWholeWeek =  DaysOfWeek().asJson.noSpaces
      val response =
        Seq("Monday", "Tuesday","Wednesday","Thursday","Friday", "Saturday", "Sunday")
         .map(d => s"$d: Closed")

      val request = Post("/opening-hours").withEntity(ContentTypes.`application/json`, closedWholeWeek)

      request ~> route ~> check {
        status shouldEqual StatusCodes.OK

        contentType shouldEqual ContentTypes.`application/json`

        entityAs[ScheduleView[String]] shouldEqual ScheduleView(response.mkString("\n"))
      }
    }

    "fail if a day of week is invalid" in {
      val typoInDayName =
      """{"TYPO":[],"monday":[],"tuesday":[],"wednesday":[],"thursday":[],"friday":[],"saturday":[]}"""
      val request = Post("/opening-hours").withEntity(ContentTypes.`application/json`, typoInDayName)

      request ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
      }
    }

    "fail if are not all days in a json" in {
      val someDaysMissed = """{"monday":[],"tuesday":[]}"""

      val request = Post("/opening-hours").withEntity(ContentTypes.`application/json`, someDaysMissed)
      request ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
      }
    }

    "fail if state is not valid" in {
      val stateIsNotValid =
      """{"sunday":[{"type": "open", "value": 3600}, {"type": "pending", "value": 7200}],"monday":[],"tuesday":[],
          |"wednesday":[],"thursday":[],"friday":[],"saturday":[]}""".stripMargin

      val request = Post("/opening-hours").withEntity(ContentTypes.`application/json`, stateIsNotValid)
      request ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
      }
    }

    "fail if open-open state found" in {
      val stateIsNotValid =
        """{"sunday":[{"type": "open", "value": 3600}, {"type": "open", "value": 7200}],"monday":[],"tuesday":[],
          |"wednesday":[],"thursday":[],"friday":[],"saturday":[]}""".stripMargin

      val request = Post("/opening-hours").withEntity(ContentTypes.`application/json`, stateIsNotValid)
      request ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[String] shouldEqual "[\"[Open, Open] is invalid.\"]"
      }
    }

    "fail if close-close state found" in {
      val stateIsNotValid =
        """{"sunday":[{"type": "close", "value": 3600}, {"type": "close", "value": 7200}],"monday":[],"tuesday":[],
          |"wednesday":[],"thursday":[],"friday":[],"saturday":[]}""".stripMargin

      val request = Post("/opening-hours").withEntity(ContentTypes.`application/json`, stateIsNotValid)
      request ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[String] shouldEqual "[\"[Close, Close] is invalid.\"]"
      }
    }

    "fail if state is not paired" in {
      val stateIsNotValid =
        """{"sunday":[{"type": "close", "value": 3600}],"monday":[],"tuesday":[],
          |"wednesday":[],"thursday":[],"friday":[],"saturday":[]}""".stripMargin

      val request = Post("/opening-hours").withEntity(ContentTypes.`application/json`, stateIsNotValid)
      request ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[String] shouldEqual "[\"The CloseState(3600) must be in a pair: [Open, Close].\"]"
      }
    }

    "fail if close-open state found" in {
      val stateIsNotValid =
        """{"sunday":[{"type": "close", "value": 3600}, {"type": "open", "value": 7200}],"monday":[],"tuesday":[],
          |"wednesday":[],"thursday":[],"friday":[],"saturday":[]}""".stripMargin

      val request = Post("/opening-hours").withEntity(ContentTypes.`application/json`, stateIsNotValid)
      request ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[String] shouldEqual "[\"[Close, Open] is invalid.\"]"
      }
    }

    "return successful result if restaurant opens on Saturday and closes on Sunday" in {
      val stateIsNotValid =
        """{"sunday":[{"type": "close", "value": 3600}, {"type": "open", "value": 7200}, {"type": "close", "value": 15200}],"monday":[],"tuesday":[],
          |"wednesday":[],"thursday":[],"friday":[],"saturday":[{"type": "open", "value": 82400}]}""".stripMargin

      val request = Post("/opening-hours").withEntity(ContentTypes.`application/json`, stateIsNotValid)
      request ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.OK
        entityAs[ScheduleView[String]] shouldEqual ScheduleView("Monday: Closed\nTuesday: Closed\n" +
          "Wednesday: Closed\nThursday: Closed\nFriday: Closed\nSaturday: 10 PM - 1 AM\nSunday: 2 AM - 4 AM")
      }
    }

    "fail if time value is more than 86399" in {
      val timeValueInvalid =
        """{"sunday":[{"type": "open", "value": 3600}, {"type": "close", "value": 86400}],"monday":[],"tuesday":[],
          |"wednesday":[],"thursday":[],"friday":[],"saturday":[]}""".stripMargin

      val request = Post("/opening-hours").withEntity(ContentTypes.`application/json`, timeValueInvalid)
      request ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
      }
    }
  }

  private def readLines(fileName: String): String = {
    Using(Source.fromFile(getClass.getClassLoader.getResource(fileName).toURI))(_.getLines().mkString(""))
      .getOrElse(throw new RuntimeException("File not found"))
  }
}