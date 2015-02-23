package shorty

import akka.actor._
import com.typesafe.config.Config
import java.sql.DriverManager

class RootActor(val conf: Config) extends Actor with Repository with Routes {

  def actorRefFactory = context
  def exc = context.dispatcher

  def receive = runRoute(route)

  def getDbConnection =
    DriverManager.getConnection(s"jdbc:postgresql://${conf.getString("db.host")}:${conf.getString("db.port")}/${conf.getString("db.name")}", conf.getString("db.user"), conf.getString("db.pass"))

  def repositoryPoolSize =
    conf.getInt("db.connections")

}
