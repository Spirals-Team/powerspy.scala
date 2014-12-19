/*
 * This software is licensed under the GNU Affero General Public License, quoted below.
 *
 * This file is a part of PowerAPI.
 *
 * Copyright (C) 2011-2014 Inria, University of Lille 1.
 *
 * PowerAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * PowerAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with PowerAPI.
 *
 * If not, please consult http://www.gnu.org/licenses/agpl-3.0.html.
 */
package fr.inria.powerspy.core

/**
 * Represents the commands that can be sent to the PowerSpy bluetooth powermeter.
 *
 * @author Maxime Colmant <maxime.colmant@gmail.com>
 */
object Command extends Enumeration {

  case class CommandValue(name: String) extends Val

  val ID = CommandValue("?")
  val RESET = CommandValue("R")
  val EEPROM_READ = CommandValue("V")
  val START = CommandValue("S")
  val CANCEL = CommandValue("C")
  val FREQUENCY = CommandValue("F")
  val RT = CommandValue("J")
  val RT_STOP = CommandValue("Q")
  val OK = CommandValue("K")
  val FAILED = CommandValue("Z")
}
