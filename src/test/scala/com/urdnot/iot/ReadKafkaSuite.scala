package com.urdnot.iot

import akka.actor.ActorSystem
import akka.kafka.{ProducerMessage, ProducerSettings}
import com.typesafe.config.Config
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.testcontainers.containers.KafkaContainer

class ReadKafkaSuite extends AnyFlatSpec with should.Matchers {

  implicit val system: ActorSystem = ActorSystem("iot_wind_direction")
  val producerConfig: Config = system.settings.config.getConfig("akka.kafka.producer")


  val kafka = new KafkaContainer("4.1.2")
  private val TOPIC = "containers"
  kafka.start()

  val producer: ProducerSettings[String, Array[Byte]] =
    ProducerSettings(producerConfig, new StringSerializer, new ByteArraySerializer)
      .withBootstrapServers(kafka.getBootstrapServers)

  val kafkaKey: String = "key"
  val kafkaValue: Array[Byte] = "value".getBytes("utf-8")

  //  val record = new ProducerRecord[String, Array[Byte]](TOPIC, "key", "value".getBytes("utf-8"))
  val single: ProducerMessage.Envelope[String, Array[Byte], String] =
    ProducerMessage.single(
      new ProducerRecord(TOPIC, kafkaKey, kafkaValue),
      ""
    )


  kafka.stop()
}
