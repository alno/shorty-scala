package shorty

import scala.annotation.tailrec
import scala.concurrent.{ Future, ExecutionContext }
import scala.concurrent.duration._
import java.util.concurrent.Executors
import java.sql.Connection
import akka.actor._
import akka.routing.RoundRobinPool
import akka.pattern._
import akka.util.Timeout
import com.lucidchart.open.relate.interp._

trait Repository {

  private implicit val dbExc = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())

  def shortenUrl(url: String): Future[String] = withConnection { implicit c =>
    val code = Repository.buildCode(sql"SELECT nextval('urls_code_seq')".asSingle { _.long("nextval") })

    sql"INSERT INTO urls(code, url) VALUES($code,$url)".executeUpdate

    code
  }

  def findUrl(code: String): Future[Option[String]] = withConnection { implicit c =>
    sql"SELECT url FROM urls WHERE code = $code".asSingleOption { _.string("url") }
  }

  def getUrlStats(code: String): Future[Option[Int]] = withConnection { implicit c =>
    sql"SELECT visits FROM urls WHERE code = $code".asSingleOption { _.int("visits") }
  }

  def incrUrlStats(code: String): Unit = withConnection { implicit c =>
    sql"UPDATE urls SET visits = visits + 1 WHERE code = $code".executeUpdate
  }

  def getDbConnection: Connection

  private def withConnection[T](block: Connection => T): Future[T] = Future {
    val conn = getDbConnection

    try {
      block(conn)
    } finally {
      conn.close
    }
  }

}

object Repository {

  val alphabet = "5BtdPYL7mTJj36DpyxhFbHGcs8rCgfRXvZVzSQ29n4wKMNqkW"

  def buildCode(codeIdx: Long): String =
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
