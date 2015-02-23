package shorty

import akka.actor.Actor
import java.sql.Connection
import com.lucidchart.open.relate.interp._
import scala.annotation.tailrec

class RepositoryActor(connFactory: () => Connection) extends Actor {

  import RepositoryActor._

  implicit val conn = connFactory()

  override def postStop {
    conn.close()
  }

  def receive = {
    case SaveUrl(url) =>
      val code = buildCode(sql"SELECT nextval('urls_code_seq')".asSingle { _.long("nextval") })

      sql"INSERT INTO urls(code, url) VALUES($code,$url)".executeUpdate

      sender ! code
    case LoadUrl(code) =>
      sender ! sql"SELECT url FROM urls WHERE code = $code".asSingleOption { _.string("url") }
    case LoadUrlStats(code) =>
      sender ! sql"SELECT visits FROM urls WHERE code = $code".asSingleOption { _.int("visits") }
    case IncrUrlStats(code) =>
      sql"UPDATE urls SET visits = visits + 1 WHERE code = $code".executeUpdate
  }

  private def buildCode(codeIdx: Long): String =
    buildCode(codeIdx, new StringBuilder)

  @tailrec
  private def buildCode(codeIdx: Long, builder: StringBuilder): String =
    if (codeIdx == 0) {
      builder.toString
    } else {
      builder.append(alphabet.charAt((codeIdx % alphabet.size).toInt))
      buildCode(codeIdx / alphabet.size, builder)
    }

}

object RepositoryActor {

  case class SaveUrl(url: String)
  case class LoadUrl(code: String)
  case class LoadUrlStats(code: String)
  case class IncrUrlStats(code: String)

  val alphabet = "5BtdPYL7mTJj36DpyxhFbHGcs8rCgfRXvZVzSQ29n4wKMNqkW"

}
