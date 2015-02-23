package shorty

import akka.actor._
import akka.io.IO
import spray.can.Http
import com.typesafe.config.ConfigFactory

object Application extends App {
  val conf = ConfigFactory.load()

  val system = ActorSystem("shorty")
  val root = system.actorOf(Props(classOf[RootActor], conf))

  IO(Http)(system) ! Http.Bind(root, "0.0.0.0", port = 8080)

  sys.addShutdownHook(system.shutdown())
}
