package ccssi

import scala._
import scala.Predef._
import scala.concurrent.Future
import org.json4s._
import org.scalatest.AsyncFlatSpec

class MainSpec extends AsyncFlatSpec {

  "Old loading" should "have a batch of some size" in {
    val elasticLoad = new OldElasticLoad with MockElasticLoad
    elasticLoad.batch.map( b => assert(b.size >= 0))
  }

  "Chandler's suggestion" should "have a batch of some size" in {
    val elasticLoad = new ChandlerElasticLoad with NullElasticLoad
    elasticLoad.batch.map( uploads => assert(uploads.size >= 0))
  }

  trait NullElasticLoad extends ElasticLoadActions {
    def checkRoot    = Future((): Unit) // Future.unit
    def deleteIndex  = Future((): Unit) // Future.unit
    def addMapping   = Future((): Unit) // Future.unit
    def checkIndexes = Future((): Unit) // Future.unit
    def uploads      = Seq.empty[Future[String]]
  }

  trait MockElasticLoad extends ElasticLoadActions {
    def checkRoot    = Future.successful(null: JValue)
    def deleteIndex  = Future.successful(null: JValue)
    def addMapping   = Future.successful(null: JValue)
    def checkIndexes = Future.successful(null: String)
    def uploads      = Seq.empty[Future[FileUploaded]]
  }
}
