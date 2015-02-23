package shorty

import org.scalatest.FreeSpec
import org.scalatest.Matchers
import spray.testkit.ScalatestRouteTest
import spray.http.StatusCodes
import scala.concurrent.Future
import spray.http.FormData

class RoutesSpec extends FreeSpec with Matchers with ScalatestRouteTest with Routes {

  def exc = system.dispatcher
  def actorRefFactory = system

  def shortenUrl(url: String) = Future.successful("some-code")
  def findUrl(code: String) = Future.successful(Map("some-code" -> "http://some-url.com/saved").get(code))
  def getUrlStats(code: String) = Future.successful(Map("some-code" -> 123).get(code))
  def incrUrlStats(code: String) = {}

  "/shorten should" - {
    "shorten valid links" in {
      Post("/shorten", FormData(Map("url" -> "http://some-url.com/example"))) ~> route ~> check {
        status should be(StatusCodes.OK)
        responseAs[String] should be("some-code")
      }
    }

    "ignore invalid links" in {
      Post("/shorten", FormData(Map("url" -> "about:config"))) ~> sealRoute(route) ~> check {
        status should be(StatusCodes.BadRequest)
      }
    }
  }

  "/expand should" - {
    "expand found links" in {
      Get("/expand/some-code") ~> sealRoute(route) ~> check {
        status should be(StatusCodes.OK)
        responseAs[String] should be("http://some-url.com/saved")
      }
    }

    "return 404 when expanding non-existend links" in {
      Get("/expand/some-other-code") ~> sealRoute(route) ~> check {
        status should be(StatusCodes.NotFound)
      }
    }
  }

  "/:code should" - {
    "redirect to found links" in {
      Get("/some-code") ~> sealRoute(route) ~> check {
        status should be(StatusCodes.PermanentRedirect)
        header("Location").get.value should be("http://some-url.com/saved")
      }
    }

    "return 404 when handling non-existend links" in {
      Get("/some-other-code") ~> sealRoute(route) ~> check {
        status should be(StatusCodes.NotFound)
      }
    }
  }

  "/statistics/:code should" - {
    "redirect to found links" in {
      Get("/statistics/some-code") ~> sealRoute(route) ~> check {
        status should be(StatusCodes.OK)
        responseAs[String] should be("123")
      }
    }

    "return 404 when handling non-existend links" in {
      Get("/statistics/some-other-code") ~> sealRoute(route) ~> check {
        status should be(StatusCodes.NotFound)
      }
    }
  }
}
