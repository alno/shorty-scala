package shorty

import scala.concurrent.{ Future, ExecutionContext }
import scala.concurrent.duration._
import java.sql.Connection
import akka.actor._
import akka.routing.RoundRobinPool
import akka.pattern._
import akka.util.Timeout

trait Repository {

  import RepositoryActor._

  def actorRefFactory: ActorRefFactory
  implicit def exc: ExecutionContext

  private implicit val timeout = Timeout(2 seconds)

  private val repositoryActorPoolProps = Props(classOf[RepositoryActor], () => getDbConnection).withRouter(RoundRobinPool(repositoryPoolSize)).withDispatcher("db-dispatcher")
  private val repositoryActorPool = actorRefFactory.actorOf(repositoryActorPoolProps, name = "RepositoryActorPool")

  def shortenUrl(url: String): Future[String] =
    (repositoryActorPool ? SaveUrl(url)).mapTo[String]

  def findUrl(code: String): Future[Option[String]] =
    (repositoryActorPool ? LoadUrl(code)).mapTo[Option[String]]

  def getUrlStats(code: String): Future[Option[Int]] =
    (repositoryActorPool ? LoadUrlStats(code)).mapTo[Option[Int]]

  def incrUrlStats(code: String): Unit =
    repositoryActorPool ! IncrUrlStats(code)

  def getDbConnection: Connection
  def repositoryPoolSize: Int

}
