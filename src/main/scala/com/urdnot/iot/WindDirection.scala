package com.urdnot.iot

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.Config
import com.typesafe.scalalogging.{LazyLogging, Logger}
import io.circe.ParsingFailure
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import spray.json.enrichAny
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}


object WindDirection extends LazyLogging {

  implicit val system: ActorSystem = ActorSystem("iot_wind_direction")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val log: Logger = Logger("windDirection")

  val consumerConfig: Config = system.settings.config.getConfig("akka.kafka.consumer")
  val envConfig: Config = system.settings.config.getConfig("env")
  val bootstrapServers: String = consumerConfig.getString("kafka-clients.bootstrap.servers")
  val consumerSettings: ConsumerSettings[String, Array[Byte]] =
    ConsumerSettings(consumerConfig, new StringDeserializer, new ByteArrayDeserializer)
      .withBootstrapServers(bootstrapServers)

  //Influxdb
  //  val influxdb: InfluxDB = InfluxDB.connect(envConfig.getString("influx.host"), envConfig.getInt("influx.port"))
  //  val database: Database = influxdb.selectDatabase(envConfig.getString("influx.database"))

  //  val INFLUXDB_URL = "http://" + envConfig.getString("influx.host") + ":" + envConfig.getInt("influx.port")
  //  val USERNAME = "admin"
  //  val PASSWORD = ""
  //
  //  val influxDB = InfluxDBFactory.connect(INFLUXDB_URL, USERNAME, PASSWORD)
  //  influxDB.setDatabase(envConfig.getString("influx.database"))
  //  // {'wind_degrees': 0.0, 'timestamp': 1553402644410L, 'wind_direction': 'N', 'voltage': 2.116125}


  implicit val materializer: ActorMaterializer = ActorMaterializer()

  Consumer
    .plainSource(consumerSettings, Subscriptions.topics(envConfig.getString("kafka.topic")))
    .map { consumerRecord =>
      //println(consumerRecord.value.map(_.toChar).mkString)
      val parsedRecord: Future[Either[String, WindVaneReading]] = parseRecord(consumerRecord.value())
      parsedRecord.onComplete{
        case Success(x) => x match {
          case Right(valid) => println("valid: " + valid)
          case Left(invalid) => println("invalid: " + invalid)
        }
        case Failure(exception) => println(exception)
      }
    }
    .toMat(Sink.ignore)(DrainingControl.apply)
    .run()


  //
  //
  //  Consumer.committableSource(consumerSettings, Subscriptions.topics(envConfig.getString("kafka.topic")))
  //    .runForeach { x =>
  //      val record = x.record.value()
  //      record.parseJson.convertTo[WindVaneReading]
  //      val rawJson = record.map(_.toChar).mkString.replace("\'", "\"").replace("L", "")
  //      val parsedJson = Json.parse(rawJson)
  //      val jsonRecord = dataObjects.windDirectionData(
  //        (parsedJson \ "timestamp").asOpt[Long],
  //        (parsedJson \ "wind_degrees").asOpt[Double],
  //        (parsedJson \ "wind_direction").asOpt[String],
  //        (parsedJson \ "voltage").asOpt[Double]
  //      )
  //      val sensor = "windDirection"
  //      val host = "pi-weather"

  //      val windSpeedPoint = Point(sensor, jsonRecord.timeStamp.get)
  //        .addTag("sensor", sensor)
  //        .addTag("host", host)
  //        .addField("wind_degrees", jsonRecord.windDegrees.get)
  //        .addField("wind_direction", jsonRecord.windDirection.getOrElse(""))
  //        .addField("voltage", jsonRecord.voltage.get)
  //      Future(database.write(windSpeedPoint, precision = Precision.MILLISECONDS))


  //    }
  def parseRecord(record: Array[Byte]): Future[Either[String, WindVaneReading]] = Future{
    import WindVaneSprayFormat._
    import io.circe.Json
    val testJsonString = """{"wind_degrees": 0.0, "timestamp": 1598900477034, "wind_direction": "N", "voltage": 0.9676875}"""
    //    println(record.map(_.toChar).mkString.replace("\'", "\""))

    //    val parsedJson: Either[ParsingFailure, Json] = io.circe.parser.parse(record.map(_.toChar).mkString.replace("\'", "\""))//.replace("L", ""))

    val parsedJson: Either[ParsingFailure, Json] = io.circe.parser.parse(testJsonString)
    parsedJson match {
      case Right(x: Json) => println(x)
      case Left(x) => println(x)
    }
    try {
      Right(record.toJson.convertTo[WindVaneReading])
    } catch {
      case e: Exception => Left("couldn't parse: " + record.map(_.toChar).mkString + " because " + e.getMessage)
    }
  }
}
