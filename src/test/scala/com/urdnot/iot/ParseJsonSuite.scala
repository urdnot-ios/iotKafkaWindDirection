package com.urdnot.iot

import org.scalatest.flatspec.AsyncFlatSpec

import scala.util.{Failure, Success}

class ParseJsonSuite extends AsyncFlatSpec {

  "Parsing a JSON string " should "return a data structure " in {
    val validJson: Array[Byte] = """{"wind_degrees": 0.0, "timestamp": 1598900477034, "wind_direction": "N", "voltage": 0.9676875}""".getBytes("utf-8")
    val parsedJson = WindDirection.parseRecord(validJson)

    assert(parsedJson.onComplete {
      case Success(x) => x match {
        case Right(valid) => println(valid == WindVaneReading(wind_degrees = 0.0, timestamp = 1598900477034L, wind_direction = "N", voltage = 0.9676875))
        case Left(invalid) => println(invalid == "")
      }
      case Failure(exception) => println(exception)
    } == ())
  }
}
