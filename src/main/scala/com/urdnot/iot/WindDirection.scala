package com.urdnot.iot

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.Config
import com.typesafe.scalalogging.{LazyLogging, Logger}
import com.urdnot.iot.DataProcessing.parseRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}


object WindDirection extends LazyLogging
  with DataStructures {

  implicit val system: ActorSystem = ActorSystem("iot_wind_direction")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val log: Logger = Logger("windDirection")

  val consumerConfig: Config = system.settings.config.getConfig("akka.kafka.consumer")
  val envConfig: Config = system.settings.config.getConfig("env")
  val bootstrapServers: String = consumerConfig.getString("kafka-clients.bootstrap.servers")
  val consumerSettings: ConsumerSettings[String, Array[Byte]] =
    ConsumerSettings(consumerConfig, new StringDeserializer, new ByteArrayDeserializer)
      .withBootstrapServers(bootstrapServers)

  val INFLUX_URL: String = "http://" + envConfig.getString("influx.host") + ":" + envConfig.getInt("influx.port") + envConfig.getString("influx.route")
  val INFLUX_USERNAME: String = envConfig.getString("influx.username")
  val INFLUX_PASSWORD: String = envConfig.getString("influx.password")
  val INFLUX_DB: String = envConfig.getString("influx.database")
  val INFLUX_MEASUREMENT: String = "windDirection"

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  Consumer
    .plainSource(consumerSettings, Subscriptions.topics(envConfig.getString("kafka.topic")))
    .map { consumerRecord =>
      parseRecord(consumerRecord.value())
        .onComplete {
          case Success(x) => x match {
            case Right(valid) =>
              val data =
                s"""$INFLUX_MEASUREMENT,host=pi-weather,sensor=windDirection wind_degrees=${valid.wind_degrees},wind_direction="${valid.wind_direction}",voltage=${valid.voltage} ${valid.timestamp}000000""".stripMargin
              val runInflux: Future[HttpResponse] = Http().singleRequest(HttpRequest(
                method = HttpMethods.POST,
                uri = Uri(INFLUX_URL).withQuery(
                  Query(
                    "bucket" -> INFLUX_DB,
                    "precision" -> "ns"
                  )
                ),
                headers = Seq(Authorization(
                  BasicHttpCredentials(INFLUX_USERNAME, INFLUX_PASSWORD))),
                entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, data)
              ))
              println(data)
              runInflux.onComplete{{
                case Success(res) => res match {
                  case res if res.status.isFailure() => println(res.status.reason() + ": " + res.status.defaultMessage() + " Influx Message: " + res.headers(2))
                  case res if res.status.isSuccess() => println(res)
                  case _ => println("Influx failure: " + res)
                }
                case Failure(e)   => sys.error("something went wrong: " + e.getMessage)
              }}
              valid
            case Left(invalid) => println(invalid)
          }
          case Failure(exception) => println(exception)
        }
    }
    .toMat(Sink.ignore)(DrainingControl.apply)
    .run()
  //    curl -i -XPOST 'http://intel-server-02:8086/api/v2/write?bucket=home_sensors&precision=ns' --header 'Authorization: Token admin:aceace123' --data-raw 'windDirection,host=pi-weather,sensor=windDirection wind_degrees=0.0,wind_direction="N",voltage=-0.0815625 1598937011218000000'

}
