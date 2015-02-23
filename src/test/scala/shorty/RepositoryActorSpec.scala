package shorty

import akka.actor._
import akka.testkit._
import org.scalatest._
import java.sql.{ Date, DriverManager }
import acolyte.jdbc.{ Driver ⇒ AcolyteDriver, QueryExecution, UpdateExecution }
import acolyte.jdbc.RowLists.{ rowList1, rowList3 }
import acolyte.jdbc.AcolyteDSL
import acolyte.jdbc.Implicits._
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

class RepositoryActorSpec extends TestKit(ActorSystem("test-system")) with FreeSpecLike with Matchers with ImplicitSender with BeforeAndAfterAll {

  import RepositoryActor._

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

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

  val actor = system.actorOf(Props(classOf[RepositoryActor], () => DriverManager.getConnection("jdbc:acolyte:anything-you-want?handler=acolyte-handler-id")))

  "RepositoryActor should" - {

    "save url to database" in {
      actor ! SaveUrl("http://some.url/example")
      expectMsg("3f")
    }

    "load existing url from database" in {
      actor ! LoadUrl("abcd")
      expectMsg(Some("http://some.url/abcd"))
    }

    "load non-existing url from database" in {
      actor ! LoadUrl("gewfsxz")
      expectMsg(None)
    }

    "load existing url stats from database" in {
      actor ! LoadUrlStats("abcd")
      expectMsg(Some(77))
    }

    "load non-existing url stats from database" in {
      actor ! LoadUrlStats("gewfsxz")
      expectMsg(None)
    }

    "increment url stats in database" in {
      statsIncrQueue.clear()
      actor ! IncrUrlStats("abcd")
      statsIncrQueue.poll(2, TimeUnit.SECONDS) should be("abcd")
    }
  }

}