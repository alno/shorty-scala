package shorty

import org.scalatest._
import org.scalatest.time._
import org.scalatest.concurrent._
import java.sql.{ Date, DriverManager }
import acolyte.jdbc.{ Driver ⇒ AcolyteDriver, QueryExecution, UpdateExecution }
import acolyte.jdbc.RowLists.{ rowList1, rowList3 }
import acolyte.jdbc.AcolyteDSL
import acolyte.jdbc.Implicits._
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import javax.sql.DataSource

class RepositorySpec extends FreeSpec with Matchers with ScalaFutures {

  val statsIncrQueue = new ArrayBlockingQueue[String](10)

  val handler =
    AcolyteDSL.handleStatement.
      withQueryDetection("^SELECT ").
      withUpdateHandler { e: UpdateExecution ⇒
        if (e.sql.startsWith("INSERT INTO urls")) {
          1
        } else if (e.sql.startsWith("UPDATE urls SET visits = visits + 1 WHERE code")) {
          statsIncrQueue.add(e.parameters(0).value.toString)
          1
        } else {
          ???
        }
      } withQueryHandler { e: QueryExecution ⇒
        if (e.sql.startsWith("SELECT nextval")) {
          (rowList1(classOf[Long]).withLabels(1 -> "nextval") :+ 123L).asResult
        } else if (e.sql.startsWith("SELECT url FROM urls WHERE code")) {
          Map("abcd" -> "http://some.url/abcd").get(e.parameters(0).value.toString).map { url =>
            (rowList1(classOf[String]).withLabels(1 -> "url") :+ url).asResult
          } getOrElse {
            rowList1(classOf[String]).withLabels(1 -> "url").asResult
          }
        } else if (e.sql.startsWith("SELECT visits FROM urls WHERE code")) {
          Map("abcd" -> 77).get(e.parameters(0).value.toString).map { url =>
            (rowList1(classOf[Int]).withLabels(1 -> "visits") :+ url).asResult
          } getOrElse {
            rowList1(classOf[Int]).withLabels(1 -> "visits").asResult
          }
        } else {
          ???
        }
      }

  AcolyteDriver.register("acolyte-handler-id", handler)

  val service = new Repository {
    override def getDbConnection = DriverManager.getConnection("jdbc:acolyte:anything-you-want?handler=acolyte-handler-id")
  }

  implicit override val patienceConfig = PatienceConfig(timeout = Span(2, Seconds), interval = Span(5, Millis))

  "RepositoryActor should" - {

    "save url to database" in {
      service.shortenUrl("http://some.url/example").futureValue should be("8t")
    }

    "load existing url from database" in {
      service.findUrl("abcd").futureValue should be(Some("http://some.url/abcd"))
    }

    "load non-existing url from database" in {
      service.findUrl("gewfsxz").futureValue should be(None)
    }

    "load existing url stats from database" in {
      service.getUrlStats("abcd").futureValue should be(Some(77))
    }

    "load non-existing url stats from database" in {
      service.getUrlStats("gewfsxz").futureValue should be(None)
    }

    "increment url stats in database" in {
      statsIncrQueue.clear()
      service.incrUrlStats("abcd")
      statsIncrQueue.poll(2, TimeUnit.SECONDS) should be("abcd")
    }
  }

}
