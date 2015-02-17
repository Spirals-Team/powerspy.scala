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
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import sbt._

object PowerSpyBuild extends Build {
  lazy val powerspy = Project(id = "powerspy", base = file(".")).aggregate(powerspyCore, powerspyApp)

  lazy val powerspyCore = Project(id = "powerspy-core", base = file("powerspy-core"))
  lazy val powerspyApp = Project(id = "powerspy-app", base = file("powerspy-app")).dependsOn(powerspyCore).enablePlugins(JavaAppPackaging)
}
