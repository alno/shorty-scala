package shorty

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import akka.actor._
import akka.io.IO
import spray.can.Http

class BasicWorkSimulation extends Simulation {

  val conf = ConfigFactory.load()
  val system = ActorSystem("shorty")

  IO(Http)(system) ! Http.Bind(system.actorOf(Props(classOf[RootActor], conf)), conf.getString("http.host"), port = conf.getInt("http.port"))

  after {
    system.shutdown()
  }

  val httpConf = http.
    baseURL(s"http://${conf.getString("http.host")}:${conf.getInt("http.port")}").
    disableFollowRedirect

  val scn = scenario("Creating and using link") // A scenario is a chain of requests and pauses
    .exec(_.set("url", "http://example.com/some-url"))
    .exec(http("Shorten link").post("/shorten").formParam("url", "${url}").check(bodyString.saveAs("code")))
    .exec(http("Use link").get("/${code}").check(status.is(301)).check(header("Location").is("${url}")))

  setUp(scn.inject(atOnceUsers(1)).
    protocols(httpConf)).
    assertions(global.successfulRequests.percent.is(100))

}
