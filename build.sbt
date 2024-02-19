name := """idos"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.11"

libraryDependencies ++= Seq(
    guice, javaCore, javaJdbc, jdbc, javaJpa, filters, cacheApi, ehcache,
    "com.typesafe.play" %% "play-guice" % "2.8.19",
    "com.typesafe.play" %% "play" % "2.8.19",
    "com.typesafe.play" %% "play-java" % "2.8.19",
    "com.typesafe.play" %% "play-java-jpa" % "2.8.19",
    "com.typesafe.play" %% "play-cache" % "2.8.19",
    "com.typesafe.play" %% "play-slick" % "5.0.0",
    "com.google.inject" % "guice" % "3.0",
    "com.itextpdf" % "itextpdf" % "5.5.13.3",
    "com.lowagie" % "itext" % "2.1.7",
    /* "com.typesafe.akka" %% "akka-actor" % "2.6.20",
    "com.typesafe.akka" %% "akka-cluster" % "2.6.20",
    "com.typesafe.akka" %% "akka-coordination" % "2.6.20",
    "com.typesafe.akka" %% "akka-pki" % "2.6.20",
    "com.typesafe.akka" %% "akka-remote" % "2.6.20", */
    "javax.persistence" % "javax.persistence-api" % "2.2",
    "mysql" % "mysql-connector-java" % "8.0.26",
    "javax.mail" % "javax.mail-api" % "1.6.2",
    // "mysql" % "mysql-connector-java" % "5.1.41",
    "org.hibernate" % "hibernate-core" % "5.4.32.Final",
    "org.hibernate" % "hibernate-entitymanager" % "5.4.32.Final",
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.11.0",
    "net.sf.flexjson" % "flexjson" % "3.3",
    "ch.qos.logback" % "logback-classic" % "1.2.6",
    "log4j" % "log4j" % "1.2.17",
	  "log4j" % "apache-log4j-extras" % "1.2.17",
    "org.apache.logging.log4j" % "log4j-api" % "2.20.0",
    "org.apache.logging.log4j" % "log4j-core" % "2.20.0",
    "org.apache.poi" % "poi" % "5.2.3",
    "org.apache.poi" % "poi-ooxml" % "5.2.3",
    // "com.lowagie" % "itext" % "4.2.2",
    "org.apache.commons" % "commons-email" % "1.5",
    "commons-lang" % "commons-lang" % "2.6",
    "org.eclipse.jetty"  % "jetty-websocket" % "8.2.0.v20160908",
    "org.eclipse.jetty.websocket" % "websocket-api" % "9.4.52.v20230823",
    "org.olap4j" % "olap4j" % "1.2.0",
    "net.sf.jasperreports" % "jasperreports" % "6.4.0",
    "com.google.code.simple-spring-memcached" % "spymemcached" % "2.8.4",
    "org.json" % "json" % "20230618",
    "io.netty" % "netty" % "3.10.6.Final",
    "org.apache.httpcomponents" % "httpmime" % "4.5.3",
    "commons-io" % "commons-io" % "2.13.0",
    "com.microsoft.azure" % "azure-storage" % "8.6.6"
)

PlayKeys.externalizeResourcesExcludes += baseDirectory.value / "conf" / "META-INF" / "persistence.xml"
