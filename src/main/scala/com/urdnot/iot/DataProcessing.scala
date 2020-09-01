package com.urdnot.iot

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.jawn.decode

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object DataProcessing extends DataStructures {
  def parseRecord(record: Array[Byte]): Future[Either[String, WindVaneReading]] = Future {
    implicit val decoder: Decoder[WindVaneReading] = deriveDecoder[WindVaneReading]
    // : Either[circe.Error, DataProcessing.WindVaneReading] = decode[WindVaneReading](record.map(_.toChar).mkString)
   decode[WindVaneReading](record.map(_.toChar).mkString) match {
     case Right(x) => Right(x)
     case Left(x) => Left("couldn't parse: " + record.map(_.toChar).mkString + " -- " + x)
   }
  }
}
