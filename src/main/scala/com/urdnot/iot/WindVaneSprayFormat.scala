package com.urdnot.iot

import spray.json._
final case class WindVaneReading(wind_degrees: Double,
                                 timestamp: Long,
                                 wind_direction: String,
                                 voltage: Double
                                )

// {"wind_degrees": 0.0, "timestamp": 1598765093244, "wind_direction": "N", "voltage": 0.7756875}
object WindVaneSprayFormat extends DefaultJsonProtocol {
  implicit val windVaneReadingFormat: RootJsonFormat[WindVaneReading] = jsonFormat4(WindVaneReading)
}

