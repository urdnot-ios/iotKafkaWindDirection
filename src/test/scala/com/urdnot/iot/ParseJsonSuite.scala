package com.urdnot.iot

import com.urdnot.iot.DataProcessing.parseRecord
import org.scalatest.flatspec.AsyncFlatSpec

class ParseJsonSuite extends AsyncFlatSpec with DataStructures {

  val validJson: Array[Byte] = """{"wind_degrees": 0.0, "timestamp": 1598900477034, "wind_direction": "N", "voltage": 0.9676875}""".getBytes("utf-8")
  val inValidJson: Array[Byte] = """{"wind_degrees: 0.0, "timestamp": 1598900477034, "wind_direction": "N", "voltage": 0.9676875}""".getBytes("utf-8")
  val errorReply = Left("""couldn't parse: {"wind_degrees: 0.0, "timestamp": 1598900477034, "wind_direction": "N", "voltage": 0.9676875} -- io.circe.ParsingFailure: expected : got 'timest...' (line 1, column 23)""")

  behavior of "parsedJson"
  it should "Parse out the JSON data structure from the JSON string" in {
    val futureJson = parseRecord(validJson)
    futureJson map { x =>
      assert(x == Right(WindVaneReading(wind_degrees = 0.0, timestamp = 1598900477034L, wind_direction = "N", voltage = 0.9676875)))
    }
  }
  it should "Return an error when there is a bad JSON string" in {
    val futureJson = parseRecord(inValidJson)
    futureJson map { x =>
      assert(x == errorReply)
    }
  }
}
