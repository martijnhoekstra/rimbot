import com.github.retronym.SbtOneJar._

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

libraryDependencies += "pircbot" % "pircbot" % "1.5.0"

//libraryDependencies += "org.http4s" %% "http4s-dsl"          % "0.10.0"  // to use the core dsl

//libraryDependencies += "org.http4s" %% "http4s-blaze-server" % "0.10.0"  // to use the blaze backend

//libraryDependencies += "org.http4s" %% "http4s-servlet"      % "0.10.0"  // to use the raw servlet backend

//libraryDependencies += "org.http4s" %% "http4s-jetty"        % "0.10.0"  // to use the jetty servlet backend

libraryDependencies += "org.http4s" %% "http4s-blaze-client" % "0.10.0"  // to use the blaze client

libraryDependencies += "org.http4s" %% "http4s-argonaut" % "0.10.0"  // to use the blaze client

libraryDependencies += "io.argonaut" %% "argonaut" % "6.0.4"

oneJarSettings

scalariformSettings