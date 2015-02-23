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
    .exec(_.set("url", s"http://example.com/some-url-${math.random}"))
    .pause(2)
    .exec(http("Shorten link").post("/shorten").formParam("url", "${url}").check(regex(s"${conf.getString("http.base-url")}/(\\w+)").saveAs("code")))
    .exec(http("Use link").get("/${code}").check(status.is(301)).check(header("Location").is("${url}")))
    .pause(1)
    .exec(http("Check link stats").get("/statistics/${code}").check(bodyString.is("1")))
    .exec(http("Use link again").get("/${code}").check(status.is(301)).check(header("Location").is("${url}")))
    .pause(1)
    .exec(http("Check link stats again").get("/statistics/${code}").check(bodyString.is("2")))

  setUp(scn.inject(atOnceUsers(1)).
    protocols(httpConf)).
    assertions(global.successfulRequests.percent.is(100))

}
