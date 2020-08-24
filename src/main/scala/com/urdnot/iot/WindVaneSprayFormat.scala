package com.urdnot.iot

import spray.json._
final case class WindVaneReading(timestamp: Long,
                                 wind_degrees: Double,
                                 wind_direction: String,
                                 voltage: Double
                                )

// {'wind_degrees': 0.0, 'timestamp': 1553402644410L, 'wind_direction': 'N', 'voltage': 2.116125}
object WindVaneSprayFormat extends DefaultJsonProtocol {
  implicit val windVaneReadingFormat: RootJsonFormat[WindVaneReading] = jsonFormat4(WindVaneReading)
}

