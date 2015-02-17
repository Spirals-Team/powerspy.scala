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

import org.scalamock.scalatest.MockFactory

class PowerSpySuite extends UnitTest with MockFactory {
  import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStreamReader, PrintWriter}
  import scala.concurrent.duration.DurationInt

  val connexion = mock[Connexion]

  "PowerSpy class" should "have a method to send a command" in {
    val pspy = new PowerSpy(connexion, 3.seconds)

    val outputStream = new ByteArrayOutputStream()
    val output = new PrintWriter(outputStream)

    connexion.output _ expects() returning Some(output)
    connexion.output _ expects() returning None

    pspy.send(Command.START) should equal(true)
    new String(outputStream.toByteArray) should equal(s"<${Command.START.name}>")
    outputStream.reset()

    pspy.send(Command.START) should equal(false)
    new String(outputStream.toByteArray) should equal("")
  }

  it should "have a method to receive a command" in {
    val pspy = new PowerSpy(connexion, 3.seconds)

    val bytes = "<54><6C>".getBytes ++ new Array[Byte](99999999)
    val inputStream = new ByteArrayInputStream(bytes)
    val input = new InputStreamReader(inputStream)

    connexion.input _ expects() repeat 3 returning Some(input)

    pspy.receive() should equal(Some("54"))
    pspy.receive() should equal(Some("6C"))
    pspy.receive() should equal(None)
  }

  it should "have a method to get its identity" in {
    val pspy = new PowerSpy(connexion, 3.seconds)

    val bytes = "<POWERSPYC01FF35020001>".getBytes
    val inputStream = new ByteArrayInputStream(bytes)
    val input = new InputStreamReader(inputStream)
    val outputStream = new ByteArrayOutputStream()
    val output = new PrintWriter(outputStream)

    connexion.input _ expects() repeat 1 returning Some(input)
    connexion.output _ expects() repeat 1 returning Some(output)

    pspy.identity() should equal(
      Some(PSpyIdentity("C", "01", "FF", "35", "02", "0001"))
    )
  }

  it should "have a method for reading an eeprom value" in {
    val pspy = new PowerSpy(connexion, 3.seconds)

    val bytes = "<6C><02><A7><3D>".getBytes
    val inputStream = new ByteArrayInputStream(bytes)
    val input = new InputStreamReader(inputStream)
    val outputStream = new ByteArrayOutputStream()
    val output = new PrintWriter(outputStream)

    val sent = s"<${Command.EEPROM_READ.name}02><${Command.EEPROM_READ.name}03><${Command.EEPROM_READ.name}04><${Command.EEPROM_READ.name}05>"
    val answer = java.lang.Float.intBitsToFloat(java.lang.Integer.reverseBytes(0x6C02A73D))

    connexion.input _ expects() anyNumberOfTimes() returning Some(input)
    connexion.output _ expects() anyNumberOfTimes() returning Some(output)

    pspy.eeprom(Array("02", "03", "04", "05")) should equal(Some(answer))
    new String(outputStream.toByteArray) should equal(sent)
    outputStream.reset()
    pspy.eeprom(Array("01")) should equal(None)
    new String(outputStream.toByteArray) should equal("")
  }

  it should "have a method for getting the factories and corrections for the voltage, current, power" in {
    val pspy = new PowerSpy(connexion, 3.seconds)

    val bytes = "<6C><02><A7><3D><CE><DC><0E><3A><6C><02><A7><3D><CE><DC><0E><3A>".getBytes
    val inputStream = new ByteArrayInputStream(bytes)
    val input = new InputStreamReader(inputStream)
    val outputStream = new ByteArrayOutputStream()
    val output = new PrintWriter(outputStream)
    val answerUscaleFact, answerUscaleCurr = java.lang.Float.intBitsToFloat(java.lang.Integer.reverseBytes(0x6C02A73D))
    val answerIscaleFact, answerIscaleCurr = java.lang.Float.intBitsToFloat(java.lang.Integer.reverseBytes(0xCEDC0E3A))
    val answerPscaleFact = answerUscaleFact * answerIscaleFact
    val answerPscaleCurr = answerUscaleCurr * answerIscaleCurr

    connexion.input _ expects() repeat 16 returning Some(input)
    connexion.output _ expects() repeat 16 returning Some(output)

    pspy.pscaleFactory() should equal(None)

    pspy.uscaleFactory() should equal(Some(answerUscaleFact))
    pspy.pscaleFactory() should equal(None)

    pspy.iscaleFactory() should equal(Some(answerIscaleFact))
    pspy.pscaleFactory() should equal(Some(answerPscaleFact))

    pspy.pscaleCurrent() should equal(None)

    pspy.uscaleCurrent() should equal(Some(answerUscaleCurr))
    pspy.pscaleCurrent() should equal(None)

    pspy.iscaleCurrent() should equal(Some(answerIscaleCurr))
    pspy.pscaleCurrent() should equal(Some(answerPscaleCurr))
  }

  it should "have a method for getting the PowerSpy's frequency" in {
    val pspyV1 = new PowerSpy(connexion, 3.seconds)
    val pspyV2 = new PowerSpy(connexion, 3.seconds)

    val bytesV1 = "<POWERSPYC01FF35020001><F1F40>".getBytes
    val bytesV2 = "<POWERSPYC01FF35010001><F1F40>".getBytes
    val inputStreamV1 = new ByteArrayInputStream(bytesV1)
    val inputV1 = new InputStreamReader(inputStreamV1)
    val inputStreamV2 = new ByteArrayInputStream(bytesV2)
    val inputV2 = new InputStreamReader(inputStreamV2)

    val outputStream = new ByteArrayOutputStream()
    val output = new PrintWriter(outputStream)

    connexion.input _ expects() repeat 2 returning Some(inputV1)
    connexion.input _ expects() repeat 2 returning Some(inputV2)
    connexion.output _ expects() repeat 4 returning Some(output)

    pspyV1.frequency() should equal(None)
    pspyV2.frequency() should equal(None)

    pspyV1.identity()
    pspyV1.frequency() should equal(Some(1000000f / 0x1F40))

    pspyV2.identity()
    pspyV2.frequency() should equal(Some(1382400f / 0x1F40))
  }

  it should "have a method for starting/stopping the acquisition" in {
    val pspy = new PowerSpy(connexion, 3.seconds)

    val bytes = "<POWERSPYC01FF35020001><K><K>".getBytes
    val inputStream = new ByteArrayInputStream(bytes)
    val input = new InputStreamReader(inputStream)
    val outputStream = new ByteArrayOutputStream()
    val output = new PrintWriter(outputStream)

    connexion.input _ expects() repeat 2 returning Some(input)
    connexion.output _ expects() repeat 3 returning Some(output)

    pspy.start() should equal(false)

    pspy.identity()
    pspy.start() should equal(true)
    pspy.stop() should equal(true)
  }

  it should "have a method for starting a real time monitoring" in {
    import scala.concurrent.duration.DurationDouble

    val pspyV1 = new PowerSpy(connexion, 3.seconds)
    val pspyV2 = new PowerSpy(connexion, 3.seconds)

    val bytesV1 = "<POWERSPYC01FF35020001><F1F40><K>".getBytes
    val bytesV2 = "<POWERSPYC01FF35010001><F1F40><K><K>".getBytes

    val inputStreamV1 = new ByteArrayInputStream(bytesV1)
    val inputV1 = new InputStreamReader(inputStreamV1)
    val inputStreamV2 = new ByteArrayInputStream(bytesV2)
    val inputV2 = new InputStreamReader(inputStreamV2)

    val outputStream = new ByteArrayOutputStream()
    val output = new PrintWriter(outputStream)
    val nbPeriodsPspy1 = (1.0 * (1000000f / 0x1F40)).toInt
    val nbPeriodsPspy2Conf1 = (1.5 * (1382400f / 0x1F40)).toInt
    val nbPeriodsPspy2Conf2 = (3.0 * (1382400f / 0x1F40)).toInt

    connexion.input _ expects() repeat 3 returning Some(inputV1)
    connexion.input _ expects() repeat 4 returning Some(inputV2)
    connexion.output _ expects() repeat 7 returning Some(output)

    pspyV1.startRealTime() should equal(false)

    pspyV1.identity()
    pspyV1.startRealTime() should equal(false)

    pspyV1.frequency()

    outputStream.reset()
    pspyV1.startRealTime() should equal(true)
    new String(outputStream.toByteArray) should equal(f"<${Command.RT.name}$nbPeriodsPspy1%02X>")

    pspyV2.startRealTime() should equal(false)

    pspyV2.identity()
    pspyV2.startRealTime() should equal(false)

    pspyV2.frequency()

    outputStream.reset()
    pspyV2.startRealTime(1.5.seconds) should equal(true)
    new String(outputStream.toByteArray) should equal(f"<${Command.RT.name}$nbPeriodsPspy2Conf1%04X>")
    outputStream.reset()
    pspyV2.startRealTime(10.seconds) should equal(true)
    new String(outputStream.toByteArray) should equal(f"<${Command.RT.name}$nbPeriodsPspy2Conf2%04X>")
  }

  it should "have a method for getting real-time values after starting a real-time monitoring" in {
    val pspy = new PowerSpy(connexion, 3.seconds)

    val bytes = "<6C><02><A7><3D><CE><DC><0E><3A><007A1FCA 001F82FB 003AC29C 0FA9 0AB5>\n\r<007656F4 001CF0BF 00379ECF 0F2E 09DA>\n\r".getBytes
    val inputStream = new ByteArrayInputStream(bytes)
    val input = new InputStreamReader(inputStream)
    val outputStream = new ByteArrayOutputStream()
    val output = new PrintWriter(outputStream)
    var uscaleCurrent, iscaleCurrent, pscaleCurrent = 0f
    var voltage, current, power, pVoltage, pCurrent = 0d

    connexion.input _ expects() repeat 10 returning Some(input)
    connexion.output _ expects() repeat 8 returning Some(output)

    pspy.readRealTime() should equal(None)

    pspy.uscaleCurrent()
    pspy.readRealTime() should equal(None)

    pspy.iscaleCurrent()
    pspy.readRealTime() should equal(None)

    pspy.pscaleCurrent()

    uscaleCurrent = java.lang.Float.intBitsToFloat(java.lang.Integer.reverseBytes(0x6C02A73D))
    iscaleCurrent = java.lang.Float.intBitsToFloat(java.lang.Integer.reverseBytes(0xCEDC0E3A))
    pscaleCurrent = uscaleCurrent * iscaleCurrent
    voltage = math.sqrt(uscaleCurrent * uscaleCurrent * 0x007A1FCA)
    current = math.sqrt(iscaleCurrent * iscaleCurrent * 0x001F82FB)
    power = pscaleCurrent * 0x003AC29C
    pVoltage = uscaleCurrent * 0x0FA9
    pCurrent = iscaleCurrent * 0x0AB5

    pspy.readRealTime() match {
      case Some(PSpyRTValues(_, vol, cur, pow, pVol, pCur)) => {
        vol should equal(voltage)
        cur should equal(current)
        pow should equal(power)
        pVol should equal(pVoltage)
        pCur should equal(pCurrent)
      }
      case _ => assert(false)
    }

    uscaleCurrent = java.lang.Float.intBitsToFloat(java.lang.Integer.reverseBytes(0x6C02A73D))
    iscaleCurrent = java.lang.Float.intBitsToFloat(java.lang.Integer.reverseBytes(0xCEDC0E3A))
    pscaleCurrent = uscaleCurrent * iscaleCurrent
    voltage = math.sqrt(uscaleCurrent * uscaleCurrent * 0x007656F4)
    current = math.sqrt(iscaleCurrent * iscaleCurrent * 0x001CF0BF)
    power = pscaleCurrent * 0x00379ECF
    pVoltage = uscaleCurrent * 0x0F2E
    pCurrent = iscaleCurrent * 0x09DA
    pspy.readRealTime() match {
      case Some(PSpyRTValues(_, vol, cur, pow, pVol, pCur)) => {
        vol should equal(voltage)
        cur should equal(current)
        pow should equal(power)
        pVol should equal(pVoltage)
        pCur should equal(pCurrent)
      }
      case _ => assert(false)
    }
  }

  it should "have a method for stopping a real-time monitoring" in {
    val pspy = new PowerSpy(connexion, 3.seconds)

    val bytes = "3223><54><6C3D><K><Z>".getBytes
    val inputStream = new ByteArrayInputStream(bytes)
    val input = new InputStreamReader(inputStream)
    val outputStream = new ByteArrayOutputStream()
    val output = new PrintWriter(outputStream)

    connexion.input _ expects() repeat 4 returning Some(input)
    connexion.output _ expects() repeat 2 returning Some(output)

    pspy.stopRealTime() should equal(true)
    pspy.stopRealTime() should equal(false)
  }
}
