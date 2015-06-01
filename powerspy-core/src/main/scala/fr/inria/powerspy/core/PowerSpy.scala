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
package fr.inria.powerspy.core

import scala.concurrent.duration.FiniteDuration

/**
 * Wrapper for the PowerSpy's informations.
 *
 * status: 'R' => Ready
 *         'W' => Waiting trigger
 *         'A' => Acquisition in progress
 *         'C' => Acquisition complete
 *         'T' => ???
 *
 * @author Maxime Colmant <maxime.colmant@gmail.com>
 */
case class PSpyIdentity(status: String,
                        pllLocked: String,
                        triggerStatus: String,
                        swVersion: String,
                        hWVersion: String,
                        hwSerial: String)

case class PSpyRTValues(timestamp: Long,
                        voltage: Double,
                        current: Double,
                        power: Double,
                        pVoltage: Double,
                        pCurrent: Double)

/**
 * Utility class for communicating with the PowerSpy powermeter.
 *
 * @author Maxime Colmant <maxime.colmant@gmail.com>
 */
class PowerSpy(val connexion: Connexion, timeout: FiniteDuration) {
  import fr.inria.powerspy.core.Command.CommandValue
  import org.apache.logging.log4j.LogManager

  import scala.concurrent.duration.{DurationInt, FiniteDuration}
  import scala.concurrent.{Await, Future, TimeoutException}

  private val log = LogManager.getLogger

  private var pSpyIdentity: Option[PSpyIdentity] = None

  private var pSpyUScaleFactory, pSpyIScaleFactory, pSpyPScaleFactory: Option[Float] = None
  private var pSpyUScaleCurrent, pSpyIScaleCurrent, pSpyPScaleCurrent: Option[Float] = None

  private var pSpyFrequency: Option[Float] = None

  /**
   * Send a command by using the output provided by the connexion.
   *
   * @param cmd: Command to send through the connexion
   */
  def send(cmd: CommandValue): Boolean = {
    connexion.output match {
      case Some(out) => {
        try {
          out.write(s"<${cmd.name}>")
          out.flush()
          true
        }
        catch {
          case ex: Throwable => log.warn("{}", ex); false
        }
      }
      case _ => log.warn("connexion with PowerSpy is not established"); false
    }
  }

  /**
   * Receive an answer from the connexion.
   */
  def receive(): Option[String] = {
    import scala.concurrent.ExecutionContext.Implicits.global

    connexion.input match {
      case Some(in) => {
        try {
          val buffer = new StringBuilder("")
          var messageStarted = false
          var c = -1

          val future = Future {
            do {
              c = in.read()

              if (c == '<') {
                messageStarted = true
              }
              else if (messageStarted && c != '>') {
                buffer.append(c.asInstanceOf[Char])
              }
            } while (c != -1 && !(messageStarted && c == '>'))

            buffer.toString() match {
              case "" => None
              case answer: String => log.debug("message received: {}", answer); Some(answer)
            }
          }

          Await.result(future, timeout)
        }
        catch {
          case _: TimeoutException => log.warn("no message received"); None
          case ex: Throwable => log.warn("{}", ex); None
        }
      }
      case _ => log.warn("connexion with PowerSpy is not established"); None
    }
  }

  /**
   * Get the PowerSpy's identity by making a request.
   */
  def identity(): Option[PSpyIdentity] = {
    if(pSpyIdentity == None) {
      send(Command.ID)
      val answer = receive().getOrElse("")
      val Content = """POWERSPY(.)(.{2})(.{2})(.{2})(.{2})(.{4})""".r

      answer match {
        case Content(status, pllLocked, triggerStatus, swVersion, hwVersion, hwSerial) => {
          pSpyIdentity = Some(PSpyIdentity(status, pllLocked, triggerStatus, swVersion, hwVersion, hwSerial))
        }
        case _ => log.warn("the identity's message received is wrong"); pSpyIdentity = None
      }
    }

    pSpyIdentity
  }

  /**
   * Read eeprom float from PowerSpy.
   *
   * @param values: Should be an array of 4 elements
   */
  def eeprom(values: Array[String]): Option[Float] = {
    if(values.size == 4) {
      val str = (for(value <- values) yield {
        send(new CommandValue(s"${Command.EEPROM_READ.name}$value"))
        receive().getOrElse("")
      }).mkString

      val hexadecimal = new Hexadecimal(str, Encoding.LITTLE_ENDIAN)
      Some(java.lang.Float.intBitsToFloat(hexadecimal.bits))
    }
    else None
  }

  /**
   * Factory correction voltage coefficient.
   */
  def uscaleFactory(): Option[Float] = {
    if(pSpyUScaleFactory == None) {
      pSpyUScaleFactory = eeprom(Array("02", "03", "04", "05"))
    }

    pSpyUScaleFactory
  }

  /**
   * Factory correction current coefficient.
   */
  def iscaleFactory(): Option[Float] = {
    if(pSpyIScaleFactory == None) {
      pSpyIScaleFactory = eeprom(Array("06", "07", "08", "09"))
    }

    pSpyIScaleFactory
  }

  /**
   * Actual correction voltage coefficient.
   */
  def uscaleCurrent(): Option[Float] = {
    if(pSpyUScaleCurrent == None) {
      pSpyUScaleCurrent = eeprom(Array("0E", "0F", "10", "11"))
    }

    pSpyUScaleCurrent
  }

  /**
   * Actual correction current coefficient.
   */
  def iscaleCurrent(): Option[Float] = {
    if(pSpyIScaleCurrent == None) {
      pSpyIScaleCurrent = eeprom(Array("12", "13", "14", "15"))
    }

    pSpyIScaleCurrent
  }

  /**
   * Factory correction power coefficient.
   */
  def pscaleFactory(): Option[Float] = {
    if(pSpyUScaleFactory != None && pSpyIScaleFactory != None) {
      val uscaleFactory = pSpyUScaleFactory.get
      val iscaleFactory = pSpyIScaleFactory.get

      pSpyPScaleFactory = Some(uscaleFactory * iscaleFactory)
      pSpyPScaleFactory
    }
    else {
      log.warn("the uscale/iscale factories are not retrieved")
      None
    }
  }

  /**
   * Actual correction power coefficient.
   */
  def pscaleCurrent(): Option[Float] = {
    if(pSpyUScaleCurrent != None && pSpyIScaleCurrent != None) {
      val uscaleCurrent = pSpyUScaleCurrent.get
      val iscaleCurrent = pSpyIScaleCurrent.get

      pSpyPScaleCurrent = Some(uscaleCurrent * iscaleCurrent)
      pSpyPScaleCurrent
    }
    else {
      log.warn("the uscale/iscale currents are not retrieved")
      None
    }
  }

  /**
   * PowerSpy's frequency
   */
  def frequency(): Option[Float] = {
    if(pSpyFrequency == None) {
      if (pSpyIdentity != None) {
        val identity = pSpyIdentity.get

        send(Command.FREQUENCY)

        receive() match {
          case Some(hexa) => {
            val hexaToInt = new Hexadecimal(hexa.substring(1), Encoding.BIG_ENDIAN).bits

            // PSpy V1
            if (identity.hWVersion == "02") {
              pSpyFrequency = Some(1000000f / hexaToInt)
            }
            else {
              pSpyFrequency = Some(1382400f / hexaToInt)
            }
          }
          case _ => pSpyFrequency = None
        }
      }

      else {
        log.warn("the PowerSpy's informations are not retrieved")
        pSpyFrequency = None
      }
    }

    pSpyFrequency
  }

  /**
   * Starts the acquisition.
   */
  def start(): Boolean = {
    if(pSpyIdentity != None) {
      val identity = pSpyIdentity.get

      // TODO: Fix when the protocol will be updated
      if(identity.hWVersion == "02") {
        /**
         * Documentations says that it should return Command.OK, but no answer is received
         */
        send(Command.RESET)
      }
      else {
        send(Command.START)

        receive() match {
          case Some(ack) if ack == Command.OK.name => true
          case _ => log.warn("command START failed"); false
        }
      }
    }
    else {
      log.warn("the PowerSpy's informations are not retrieved")
      false
    }
  }

  /**
   * Stops the acquisition.
   */
  def stop(): Boolean = {
    send(Command.CANCEL)

    // TODO: Fix when the protocol will be updated
    receive()
    true
    /**
     * Documentations says that it should return Command.OK, but it's always Command.FAILED
     */
    /*receive() match {
      case Some(ack) if ack == Command.OK.name => true
      case _ => false
    }*/
  }

  /**
   * Starts a real time monitoring.
   *
   * @param interval: Interval used for computing the average number of periods sent to the PowerSpy
   */
  def startRealTime(interval: FiniteDuration = 1.seconds): Boolean = {
    if(pSpyIdentity != None && pSpyFrequency != None) {
      var intervalUsed = timeout

      if(interval.toMillis > timeout.toMillis) {
        log.warn("increase the default timeout or decrease the interval. The timeout ({}) is used", interval)
      }
      else intervalUsed = interval

      val identity = pSpyIdentity.get
      val frequency = pSpyFrequency.get

      val nbPeriods = ((intervalUsed.toMillis / 1000d) * frequency).toInt

      // PowerSpy v1 ==> hwVersion = 02
      val command = {
        if (identity.hWVersion == "02") {
          new CommandValue(f"${Command.RT.name}$nbPeriods%02X")
        }
        else {
          new CommandValue(f"${Command.RT.name}$nbPeriods%04X")
        }
      }

      send(command)

      receive() match {
        case Some(ack) if ack == Command.OK.name => true
        case Some(data) => log.warn("data flushed from the input stream: {}", data); true
        case _ => log.warn("command {} failed", command.name); false
      }
    }
    else {
      log.warn("the PowerSpy's informations are not retrieved")
      false
    }
  }

  /**
   * Reads real-time values (to use after a startRealTime call).
   */
  def readRealTime(): Option[PSpyRTValues] = {
    if(pSpyUScaleCurrent != None && pSpyIScaleCurrent != None && pSpyPScaleCurrent != None) {
      val uscaleCurrent = pSpyUScaleCurrent.get
      val iscaleCurrent = pSpyIScaleCurrent.get
      val pscaleCurrent = pSpyPScaleCurrent.get

      val answer = receive()

      answer match {
        case Some(str) => {
          val values = str.split(" ")

          if (values.size == 5) {
            /**
             * Note: Initially scale_factory and scale_current are the same but in case of user calibration, scale_current must be used
             * Corrected RMS voltage = squareroot [ (square of the RMS voltage returned by fonction) x (Uscale_current)2 ]
             * Corrected RMS current = squareroot [ (square of the RMS current returned by fonction) x (Iscale_current)2 ]
             * Corrected RMS power = (square of the RMS current returned by fonction) x (Uscale_factory) x (Iscale_current)
             * Corrected peak voltage = peak voltage returned by fonction x Uscale_current
             * Corrected peak current = peak current returned by fonction x Iscale_current
             */
            val voltage = math.sqrt(uscaleCurrent * uscaleCurrent * new Hexadecimal(values(0), Encoding.BIG_ENDIAN).bits)
            val current = math.sqrt(iscaleCurrent * iscaleCurrent * new Hexadecimal(values(1), Encoding.BIG_ENDIAN).bits)
            val power = pscaleCurrent * new Hexadecimal(values(2), Encoding.BIG_ENDIAN).bits
            val pVoltage = uscaleCurrent * new Hexadecimal(values(3), Encoding.BIG_ENDIAN).bits
            val pCurrent = iscaleCurrent * new Hexadecimal(values(4), Encoding.BIG_ENDIAN).bits

            log.debug("voltage: {}, current: {}, power: {}, pvoltage: {}, pcurrent: {}", voltage.toString, current.toString, power.toString, pVoltage.toString, pCurrent.toString)
            Some(PSpyRTValues(System.currentTimeMillis(), voltage, current, power, pVoltage, pCurrent))
          }
          else {
            log.warn("the answer received is invalid.")
            None
          }
        }
        case _ => log.warn("the format of the received message is wrong"); None
      }
    }
    else {
      log.warn("the PowerSpy's informations are not retrieved")
      None
    }
  }

  /**
   * Stops the real-time monitoring.
   */
  def stopRealTime(): Boolean = {
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.util.control.Breaks

    send(Command.RT_STOP)
    var status = false

    val future = Future {
      // flush input
      val loop = new Breaks

      loop.breakable {
        while (true) {
          receive() match {
            case Some(ack) if ack == Command.OK.name => status = true; loop.break()
            case Some(ack) if ack == Command.FAILED.name => status = false; loop.break()
            case _ => {}
          }
        }
      }
    }

    try {
      Await.result(future, timeout)
      status
    }
    catch {
      case ex: TimeoutException => log.warn("command RT_STOP failed"); false
    }
  }
}

/**
 * Companion object.
 *
 * @author Maxime Colmant <maxime.colmant@gmail.com>
 */
object PowerSpy {
  import org.apache.logging.log4j.LogManager
  import scala.concurrent.{Await, Future, TimeoutException}
  import scala.concurrent.duration.DurationDouble

  private val log = LogManager.getLogger

  private var connexionWrapper: Option[PowerSpyConnexion] = None
  private var powerspy: Option[PowerSpy] = None

  def init(address: String, timeout: FiniteDuration = 3.seconds): Option[PowerSpy] = {
    if(connexionWrapper == None && powerspy == None) {
      val connexion = new PowerSpyConnexion(address)
      val pSpy = new PowerSpy(connexion, timeout)
      val identity = pSpy.identity()

      if(identity == None) {
        log.warn("Cannot identify the device")
        deinit()
      }

      else {
        // PowerSpy is busy, force to abort the connexion
        if (identity.get.status != "R" && identity.get.status != "C") {
          log.warn("PowerSpy is in status {}, try to abort the connexion", identity.get.status)
          pSpy.stopRealTime()
          pSpy.stop()
        }

        pSpy.frequency()
        log.debug("PowerSpy's frequency: {}", pSpy.frequency())

        val uScaleFactory = pSpy.uscaleFactory().getOrElse(-1)
        val iScaleFactory = pSpy.iscaleFactory().getOrElse(-1)
        val pScaleFactory = pSpy.pscaleFactory().getOrElse(-1)
        val uScaleCurrent = pSpy.uscaleCurrent().getOrElse(-1)
        val iScaleCurrent = pSpy.iscaleCurrent().getOrElse(-1)
        val pScaleCurrent = pSpy.pscaleCurrent().getOrElse(-1)

        log.debug("uscaleFactory: {}, iscaleFactory: {}, pscaleFactory: {}", uScaleFactory.toString, iScaleFactory.toString, pScaleFactory.toString)
        log.debug("uscaleCurrent: {}, iscaleCurrent: {}, pscaleCurrent: {}", uScaleCurrent.toString, iScaleCurrent.toString, pScaleCurrent.toString)

        connexionWrapper = Some(connexion)
        powerspy = Some(pSpy)
      }
    }

    else log.debug("PowerSpy already connected")

    powerspy
  }

  def deinit(timeout: FiniteDuration = 1.seconds): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global

    connexionWrapper match {
      case Some(connexion) => {
        val closeInput = Future {
          connexion.input match {
            case Some(in) => in.close()
            case _ => log.warn("reader not initialized")
          }
        }

        val closeOutput = Future {
          connexion.output match {
            case Some(out) => out.close()
            case _ => log.warn("writer not initialized")
          }
        }

        val closeInternalConnexion = Future {
          connexion.connexion match {
            case Some(internalCon) => internalCon.close()
            case _ => log.warn("internal connexion not initialized")
          }
        }

        try {
          Await.result(closeInput, timeout)
        }
        catch {
          case _: TimeoutException => log.debug("input already closed")
        }

        try {
          Await.result(closeOutput, timeout)
        }
        catch {
          case _: TimeoutException => log.debug("output already closed")
        }

        try {
          Await.result(closeInternalConnexion, timeout)
        }
        catch {
          case _: TimeoutException => log.debug("internal connexion already closed")
        }
      }
      case _ => log.warn("connexion not initialized")
    }

    connexionWrapper = None
    powerspy = None
  }
}
