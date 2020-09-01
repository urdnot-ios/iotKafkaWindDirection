package com.urdnot.iot

trait DataStructures{
  final case class WindVaneReading(wind_degrees: Double,
                                   timestamp: Long,
                                   wind_direction: String,
                                   voltage: Double
                                  )
}
