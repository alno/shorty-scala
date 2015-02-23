package shorty

import spray.routing.HttpService
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import org.apache.commons.validator.routines.UrlValidator
import spray.http.HttpResponse

trait Routes extends HttpService {

  implicit def exc: ExecutionContext

  def shortenUrl(url: String): Future[String]
  def findUrl(code: String): Future[Option[String]]

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
      }
    }

}
