package com.urdnot.iot

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.{ActorAttributes, ActorMaterializer, Supervision}
import akka.stream.scaladsl.Sink
import com.typesafe.config.Config
import com.typesafe.scalalogging.{LazyLogging, Logger}
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import org.influxdb.InfluxDBFactory
import spray.json.enrichAny

import scala.concurrent.ExecutionContextExecutor


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

  import javax.management.Query

  val INFLUXDB_URL = "http://" + envConfig.getString("influx.host") + ":" + envConfig.getInt("influx.port")
  val USERNAME = ""
  val PASSWORD = ""

  val influxDB = InfluxDBFactory.connect(INFLUXDB_URL, USERNAME, PASSWORD)
  influxDB.setDatabase(envConfig.getString("influx.database"))
  // {'wind_degrees': 0.0, 'timestamp': 1553402644410L, 'wind_direction': 'N', 'voltage': 2.116125}


  val resumeOnParsingException = ActorAttributes.supervisionStrategy {
    case _: spray.json.JsonParser.ParsingException => Supervision.Resume
    case _ => Supervision.stop
  }
implicit val materializer: ActorMaterializer = ActorMaterializer()

  Consumer
    .plainSource(consumerSettings, Subscriptions.topics(envConfig.getString("kafka.topic")))
    .map { consumerRecord =>
      import WindVaneSprayFormat._
      println(consumerRecord.value.map(_.toChar).mkString)
      val value: Array[Byte] = consumerRecord.value()
      val sampleData: WindVaneReading = value.toJson.convertTo[WindVaneReading]
      println(sampleData)
      sampleData
    }
    .withAttributes(resumeOnParsingException)
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
}
