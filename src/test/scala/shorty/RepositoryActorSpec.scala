package shorty

import akka.actor._
import akka.testkit._
import org.scalatest._

import java.sql.{ Date, DriverManager }
import acolyte.jdbc.{ Driver ⇒ AcolyteDriver, QueryExecution, UpdateExecution }
import acolyte.jdbc.RowLists.{ rowList1, rowList3 }
import acolyte.jdbc.AcolyteDSL
import acolyte.jdbc.Implicits._

class RepositoryActorSpec extends TestKit(ActorSystem("test-system")) with FreeSpecLike with Matchers with ImplicitSender with BeforeAndAfterAll {

  import RepositoryActor._

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  val handler =
    AcolyteDSL.handleStatement.
      withQueryDetection("^SELECT ").
      withUpdateHandler { e: UpdateExecution ⇒
        if (e.sql.startsWith("INSERT INTO urls")) {
          1
        } else {
          ???
        }
      } withQueryHandler { e: QueryExecution ⇒
        if (e.sql.startsWith("SELECT nextval")) {
          (rowList1(classOf[Long]).withLabels(1 -> "nextval") :+ 123L).asResult
        } else {
          println(e.sql)
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

  }

}