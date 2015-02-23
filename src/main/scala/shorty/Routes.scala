package shorty

import scala.concurrent.{ Future, ExecutionContext }
import spray.routing.HttpService
import spray.http.{ StatusCodes, HttpResponse, HttpHeaders }
import org.apache.commons.validator.routines.UrlValidator

trait Routes extends HttpService {

  implicit def exc: ExecutionContext

  def shortenUrl(url: String): Future[String]
  def findUrl(code: String): Future[Option[String]]
  def getUrlStats(code: String): Future[Option[Int]]
  def incrUrlStats(code: String): Unit

  val route =
    post {
      path("shorten") {
        formFields('url.as[String]) { url =>
          validate(UrlValidator.getInstance.isValid(url), "Invalid url") {
            complete(shortenUrl(url))
          }
        }
      }
    } ~ get {
      path("expand" / Segment) { code =>
        complete(findUrl(code))
      } ~ path("statistics" / Segment) { code =>
        complete(getUrlStats(code) map { _ map { _.toString } })
      } ~ path(Segment) { code =>
        complete(findUrl(code) map { _ map processRedirect(code) })
      }
    }

  private def processRedirect(code: String)(uri: String) = {
    incrUrlStats(code)
    HttpResponse(StatusCodes.MovedPermanently, headers = HttpHeaders.Location(uri) :: Nil)
  }

}
