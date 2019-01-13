package test

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.openjdk.jmh.annotations._
import neotypes.Driver
import neotypes.implicits._
import neotypes.Async._
import org.neo4j.driver.v1.{AuthTokens, GraphDatabase, Transaction, TransactionWork}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationDouble
import scala.concurrent.{Await, Future}
import scala.io.Source
import org.anormcypher._
import org.neo4j.driver.v1
import play.api.libs.ws._

@State(Scope.Benchmark)
@Threads(1)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(0)
class TestBenchmark {

  private val neo4jDriver: v1.Driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "pass"))
  val driver = neo4jDriver.asScala[Future]

  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()

  val wsclient = ning.NingWSClient()
  implicit val connection: Neo4jREST = Neo4jREST("localhost", 7474, "neo4j", "pass")(wsclient, materializer)

  @Benchmark
  def anormcypherSingle(): String =
    Cypher(
      """MATCH (movie:Movie)
             RETURN movie.title as title LIMIT 1""").apply().map(row => row[String]("title")).head

  @Benchmark
  def neotypesSingle(): String = Await.result(driver.readSession(s =>
    c"""MATCH (movie:Movie)
       RETURN movie.title LIMIT 1
       """.query[String].single(s))
    , 10 second)

  @Benchmark
  def anormcypher(): Seq[String] =
    Cypher(
      """MATCH (movie:Movie)
             RETURN movie.title as title""").apply().map(row => row[String]("title"))

  @Benchmark
  def neotypes(): Seq[String] = Await.result(driver.readSession(s =>
    c"""MATCH (movie:Movie)
       RETURN movie.title
       """.query[String].list(s))
    , 10 second)

  @Benchmark
  def neo4j(): String = {
    val session = neo4jDriver.session()
    try {
      session.readTransaction(new TransactionWork[String] {
        override def execute(tx: Transaction): String = tx.run(
          """MATCH (movie:Movie)
          RETURN movie.title as title""").single().get(0).asString()
      })
    } finally {
      session.close()
    }
  }

}