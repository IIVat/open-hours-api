import cats.implicits.toFunctorOps
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._

package object app {

  sealed trait Schedule

  object Schedule {
    final case class RestaurantSchedule(
                                         sunday: DaySchedule = DaySchedule(),
                                         monday: DaySchedule = DaySchedule(),
                                         tuesday: DaySchedule = DaySchedule(),
                                         wednesday: DaySchedule = DaySchedule(),
                                         thursday: DaySchedule = DaySchedule(),
                                         friday: DaySchedule = DaySchedule(),
                                         saturday: DaySchedule = DaySchedule()
                                       ) extends Schedule
  }

  final case class DaySchedule(hours: Seq[OpeningHours] = Seq.empty)


  final case class OpeningHours(`type`: RestaurantState, value: Int)

  sealed trait RestaurantState

  case object Open extends RestaurantState

  case object Close extends RestaurantState

  object RestaurantStateCodecs {
    implicit val decode: Decoder[RestaurantState] = Decoder[String].emap {
      case "open" => Right(Open)
      case "close" => Right(Close)
      case other => Left(s"Invalid mode: $other")
    }

    implicit val encode: Encoder[RestaurantState] = Encoder[String].contramap {
      case Open => "open"
      case Close => "close"
    }
  }
}
