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

/**
 * Contract for establishing the connexion.
 *
 * @author Maxime Colmant <maxime.colmant@gmail.com>
 */
trait Connexion {
  import java.io.{Reader, Writer}

  def input: Option[Reader]
  def output: Option[Writer]
}

/**
 * Specialized connexion for the PowerSpy powermeter.
 *
 * @param address: PowerSpy's mac address.
 *
 * @author Maxime Colmant <maxime.colmant@gmail.com>
 */
class PowerSpyConnexion(address: String) extends Connexion {
  import java.io.{BufferedReader, InputStreamReader, PrintWriter, Reader, Writer}
  import javax.microedition.io.{Connector, StreamConnection}
  import org.apache.logging.log4j.LogManager

  private val log = LogManager.getLogger

  val connexion: Option[StreamConnection] = {
    try {
      Some(Connector.open(s"btspp://${address.replace(":", "").replace("-", "")}:1;authenticate=false;encrypt=false;master=false").asInstanceOf[StreamConnection])
    }
    catch {
      case ex: Throwable => log.warn("{}", ex.getMessage); None
    }
  }

  val input: Option[Reader] = {
    connexion match {
      case Some(con) => {
        try {
          Some(new BufferedReader(new InputStreamReader(con.openDataInputStream())))
        }
        catch {
          case ex: Throwable => log.warn("{}", ex.getMessage); None
        }
      }
      case _ => None
    }
  }

  val output: Option[Writer] = {
    connexion match {
      case Some(con) => {
        try {
          Some(new PrintWriter(con.openOutputStream()))
        }
        catch {
          case ex: Throwable => log.warn("{}", ex.getMessage); None
        }
      }
      case _ => None
    }
  }
}
