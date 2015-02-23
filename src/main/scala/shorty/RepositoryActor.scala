package shorty

import akka.actor.Actor
import java.sql.Connection
import com.lucidchart.open.relate.interp._

class RepositoryActor(connFactory: () => Connection) extends Actor {

  import RepositoryActor._

  implicit val conn = connFactory()

  override def postStop {
    conn.close()
  }

  def receive = {
    case SaveUrl(url) =>
      val code = buildCode(sql"SELECT nextval('urls_code_seq')".asSingle { _.long("nextval") })

      sql"INSERT INTO urls(code, url) VALUES($code,$url)"

      sender ! code
    case LoadUrl(code) =>
      ???
    case LoadUrlStats(code) =>
      ???
    case IncrUrlStats(code) =>
      ???
  }

  private def buildCode(codeIdx: Long): String =
    java.lang.Long.toString(codeIdx, 36)

}

object RepositoryActor {

  case class SaveUrl(url: String)
  case class LoadUrl(code: String)
  case class LoadUrlStats(code: String)
  case class IncrUrlStats(code: String)

}
