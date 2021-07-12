package app.model

import io.circe.{Decoder, Encoder}

sealed trait WorkingWeekSchedule

object WorkingWeekSchedule {
  final case class DaysOfWeek(
                               sunday: Seq[OpeningHours] = Seq.empty,
                               monday: Seq[OpeningHours] = Seq.empty,
                               tuesday: Seq[OpeningHours] = Seq.empty,
                               wednesday: Seq[OpeningHours] = Seq.empty,
                               thursday: Seq[OpeningHours] = Seq.empty,
                               friday: Seq[OpeningHours] = Seq.empty,
                               saturday: Seq[OpeningHours] = Seq.empty
                             ) extends WorkingWeekSchedule
}


final case class OpeningHours(`type`: State, value: Int) {
  //the most simple validation approach
  //here could be used something more sophisticated, for example refined types lib
  require(value >= 0 && value < 86400, "Wrong time value. Time must be >= 0 and < 86400")
}

sealed trait State

object State {
  final case object Open extends State

  final case object Close extends State

  implicit val decodeState: Decoder[State] = Decoder[String].emap {
    case "open" => Right(Open)
    case "close" => Right(Close)
    case other => Left(s"Invalid mode: $other")
  }

  implicit val encodeState: Encoder[State] = Encoder[String].contramap {
    case Open => "open"
    case Close => "close"
  }
}