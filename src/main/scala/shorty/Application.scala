package shorty

import akka.actor._
import akka.io.IO
import spray.can.Http
import com.typesafe.config.ConfigFactory

object Application extends App {
  val conf = ConfigFactory.load()

  val system = ActorSystem("shorty")
  val root = system.actorOf(Props(classOf[RootActor], conf))

  IO(Http)(system) ! Http.Bind(root, conf.getString("http.host"), port = conf.getInt("http.port"))

  sys.addShutdownHook(system.shutdown())
}
