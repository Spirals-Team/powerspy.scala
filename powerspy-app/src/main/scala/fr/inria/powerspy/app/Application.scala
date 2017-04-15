/*
 * This software is licensed under the GNU Affero General Public License, quoted below.
 *
 * This file is a part of powerspy.scala.
 *
 * Copyright (C) 2011-2014 Inria, University of Lille 1.
 *
 * powerspy.scala is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * powerspy.scala is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with powerspy.scala.
 *
 * If not, please consult http://www.gnu.org/licenses/agpl-3.0.html.
 */
package fr.inria.powerspy.app

import org.rogach.scallop.ScallopConf

/**
 * Main configuration for the launcher.
 */
class Configuration(arguments: Seq[String]) extends ScallopConf(arguments) {
  val MacAdress = """([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}""".r

  version("powerspy.scala (c) Spirals team")
  banner(
    """
      |usage: powerspy.scala [Options] mac
      |
      |powerspy.scala is a program which allows to print the power consumption using the Alciom PowerSpy powermeter.
    """.stripMargin)

  val interval = opt[Long](descr = "Interval between two displays in milliseconds.", default = Some(1000), short = 'i', validate = 0<)
  // TODO: Contact ALCIOM, the interval cannot be greater than 2 seconds. The command JXXXX works, but we don't receive messages.
  val expiration = opt[Double](descr = "Connexion timeout in seconds (can be a floating number).", default = Some(3), validate = 0<=)
  val time = opt[Double](descr = "Acquisition duration in seconds (can be a floating number). 0 means indefinitely.", default = Some(0), validate = 0<=)
  val mac = trailArg[String](descr = "Mac address of the PowerSpy device.", validate = _ match {
    case MacAdress(_*) => true
    case _ => false
  })

  verify()
}

/**
 * Object launcher used to print the power consumption received from PowerSpy inside the Console.
 */
object Application extends App {
  import fr.inria.powerspy.core.PowerSpy
  import org.apache.logging.log4j.LogManager
  import scala.concurrent.duration.DurationDouble

  @volatile var running = true
  @volatile var powerspy: Option[PowerSpy] = None

  val shutdownHookThread = scala.sys.ShutdownHookThread {
    println("It's the time for sleeping! ...")

    powerspy match {
      case Some(pSpy) => {
        pSpy.stopRealTime()
        pSpy.stop()
        PowerSpy.deinit()
      }
      case _ => {}
    }

    running = false
    powerspy = None
  }

  private val log = LogManager.getLogger

  val configuration = new Configuration(args)
  powerspy = PowerSpy.init(configuration.mac(), configuration.expiration().seconds)

  powerspy match {
    case Some(pSpy) => {
      pSpy.start()

      println("Trying to establish the connexion ...")
      while(!pSpy.startRealTime(configuration.interval().milliseconds)) {
        println("Failed. Retrying.")
        Thread.sleep(configuration.interval())
      }

      printf("T\t\tV\t\tA\t\tW\t\tPeak V\t\tPeak A\n")

      val expectedEnd = System.currentTimeMillis() + configuration.time().seconds.toMillis
      while(running && (configuration.time() == 0 || System.currentTimeMillis() < expectedEnd)) {
        pSpy.readRealTime() match {
          case Some(rtValue) => printf("%d\t%.2f\t\t%.2f\t\t%.2f\t\t%.2f\t\t%.2f\n", rtValue.timestamp, rtValue.voltage, rtValue.current, rtValue.power, rtValue.pVoltage, rtValue.pCurrent)
          case _ => {}
        }
      }
    }
    case _ => log.error("problem for establishing the connexion with PowerSpy")
  }

  shutdownHookThread.start()
  shutdownHookThread.join()
  shutdownHookThread.remove()
  sys.exit(0)
}
