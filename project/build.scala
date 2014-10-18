import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._

object build extends Build {
  val Organization = "dubhacks"
  val Name = "Bill-Splitter-Server"
  val Version = "0.0.0"
  val ScalaVersion = "2.10.4"
  val ScalatraVersion = "2.2.2"
  //val ScalatraVersion = "2.3.0"
  
  
  lazy val project = Project (
    "bill-splitter-server",
    file("."),
    settings = Defaults.defaultSettings ++ ScalatraPlugin.scalatraWithJRebel ++ scalateSettings ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers ++= Seq(),
      libraryDependencies ++= Seq(
        // scalatra deps
        "org.scalatra"            %% "scalatra"             % ScalatraVersion,
        "org.scalatra"            %% "scalatra-scalate"     % ScalatraVersion,
        "org.scalatra"            %% "scalatra-json"        % ScalatraVersion,
        "org.json4s"              %% "json4s-jackson"       % "3.2.7",
        "org.scalatra"            %% "scalatra-specs2"      % ScalatraVersion % "test",
        "org.scalatra"            %% "scalatra-scalatest"   % ScalatraVersion % "test",
        "org.scalatra"            %% "scalatra-auth"        % "2.3.0",

        // jetty deps
        "org.eclipse.jetty"        % "jetty-webapp"         % "8.1.8.v20121106"     % "container;compile",
        "org.eclipse.jetty.orbit"  % "javax.servlet"        % "3.0.0.v201112011016" % "container;provided;test" artifacts (Artifact("javax.servlet", "jar", "jar")),
        //"org.eclipse.jetty"          % "jetty-webapp"         % "9.1.0.v20131115"     % "container;compile",
        //"org.eclipse.jetty"          % "jetty-plus"           % "9.1.0.v20131115"     % "container;compile",
        //"javax.servlet"              % "servlet-api"          % "2.5"                 % "container;provided;test",

        // database deps
        "com.typesafe.slick"      %% "slick"                % "2.0.2",
        "postgresql"               % "postgresql"           % "9.1-901.jdbc4",
        "com.h2database"           % "h2"                   % "1.4.180",
        "com.mchange"              % "c3p0"                 % "0.9.5-pre8",
        "com.mchange"              % "mchange-commons-java" % "0.2.6.2",
        "com.github.tototoshi"    %% "scala-csv"            % "1.0.0",

        // disbatch
        "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",

        // joda time deps
        "joda-time"                % "joda-time"            % "2.3",
        "org.joda"                 % "joda-convert"         % "1.2",
        "org.json4s"              %% "json4s-ext"           % "3.2.7",

        // test deps
        "org.scalatest"            % "scalatest_2.10"       % "2.2.0"         % "test" withSources() withJavadoc()
      ),
      scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
        Seq(
          TemplateConfig(
            base / "webapp" / "WEB-INF" / "templates",
            Seq.empty,  /* default imports should be added here */
            Seq(
              Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)
            ),  /* add extra bindings here */
            Some("templates")
          )
        )
      }
    )
  )
}  
