package shorty

import akka.actor._
import com.typesafe.config.Config
import java.sql.DriverManager

class RootActor(val conf: Config) extends Actor with Repository with Routes {

  def actorRefFactory = context
  def exc = context.dispatcher

  def receive = runRoute(route)

  lazy val dataSource = {
    val ds = new com.zaxxer.hikari.HikariDataSource
    ds.setJdbcUrl(s"jdbc:postgresql://${conf.getString("db.host")}:${conf.getString("db.port")}/${conf.getString("db.name")}")
    ds.setUsername(conf.getString("db.user"))
    ds.setPassword(conf.getString("db.pass"))
    ds
  }

  def getDbConnection = dataSource.getConnection

  def baseUrl =
    conf.getString("http.base-url")

}
