package ccssi

// import java.lang._
import scala._
import scala.Predef._
import scala.collection.parallel.mutable.ParArray
import scala.util.Success
import scala.util.Failure
import scala.util.matching.Regex
import scala.concurrent.Await
import scala.concurrent.duration._

import dispatch._
import dispatch.Defaults._

import com.ning.http.client.AsyncHttpClientConfig
import com.ning.http.client.extra.ThrottleRequestFilter

import org.json4s._
import org.json4s.native.JsonMethods._

// Unit of computation we want from the future.
case class FileUploaded(file: String, count: BigInt)

object Main {

  val http = Http.configure { config: AsyncHttpClientConfig.Builder =>
    config.setAllowPoolingConnections(true)
    // Doesn't work in dispatch-11.3:
    // config.setMaxConnections(4)
    // Alternatively:
    config.addRequestFilter(new ThrottleRequestFilter(4))
  }

  val elasticEndpoint = "search-ccssi-json-5asyk3sywkmpx33mp4izclbtla.us-east-1.es.amazonaws.com"

  val elasticHost = host(elasticEndpoint)

  // The CCSSI version number is in the file, but we haven't looked
  // yet, so it's hard-coded here:
  val elasticIndex: String = "ccssi-1.1"

  // .json
  val extMatcher: Regex = "\\.json\\Z".r

  // src/main/json
  val prefixMatcher: Regex = "\\Asrc/main/json/".r

  def main(args: Array[String]) = {

    // For sanity's sake:
    val checkRoot: Future[JValue] = http(elasticHost OK as.json4s.Json)

    // May fail if it doesn't exist, but that's ok
    val deleteIndex: Future[JValue] =
      http((elasticHost / elasticIndex).DELETE OK as.json4s.Json)

    // List what exists on the cluster
    val checkIndexes: Future[String] =
      http(elasticHost / "_cat" / "indices" <<? Map(("v" -> "")) OK as.String)

    // Add an index with two identical type mappings: math and ela-literacy
    val addMapping: Future[JValue] = 
      http((elasticHost / elasticIndex << elastic.LearningStandardMapping.json("math", "ela-literacy")).PUT OK as.json4s.Json)

    if (args.isEmpty) {
      println("Error: no arguments given")
    } else {

      // Callbacks:

      checkRoot.onComplete {

        case Success(j: JValue) => {
          val JString(name: String) = j \\ "name"
          println(s"Elastic endpoint is $name")
        }

        case Failure(t) => {
          println("Failed to get checkRoot")
          println(t.getMessage)
        }
      }

      deleteIndex.onComplete {

        case Success(j: JValue) => {
          val JBool(acknowledged: Boolean) = j \\ "acknowledged"
          println(s"DeleteIndex was acknowledged: $acknowledged")
        }

        case Failure(t) => {
          println("Failed to deleteIndex index")
          println(t.getMessage)
        }
      }

      checkIndexes.onComplete {

        case Success(s: String) => println(s.trim)

        case Failure(t) => {
          println("Failed to retrieve indices")
          println(t.getMessage)
        }
      }

      addMapping.onComplete {

        case Success(j: JValue) => {
          val JBool(acknowledged: Boolean) = j \\ "acknowledged"
          println(s"Mappping was acknowledged: $acknowledged")
        }

        case Failure(t) => {
          println("Failed to create index mapping")
          println(t.getMessage)
        }
      }
    }

    // Read the files, upload JSON item one-at-a-time:
    val uploads: ParArray[Future[FileUploaded]] =
      for {
        file <- args.par
      } yield {
        val processed: Future[FileUploaded] =
          for {
            p <- upload(file)
          } yield {
            println(s"Completed ${p.file} with ${p.count} items")
            p
          }
        processed
      }

    // Use for-comprehensions to combine independent futures, and use
    // flatMap to separate dependent futures.
    val batch: Future[Traversable[FileUploaded]] = {

      for {
        r <- checkRoot
        d <- deleteIndex
      } yield {
      }

    } flatMap { _ =>

      for {
        m <- addMapping
        i <- checkIndexes
      } yield {
      }

    } flatMap { _ =>

      for {
        u <- Future.sequence(uploads.toArray.toTraversable)
      } yield {
        println(s"File count: ${u.size}")
        u
      }
    }

    try {

      // Wait forever for everything to finish
      Await.ready(batch, Duration.Inf)
      println("FINISHED")

    } catch {
      case t: Throwable => println(t.getMessage)

    }
  }

  // Upload a file
  def upload(file: String): Future[FileUploaded] = {

    // Check extension
    extMatcher.findFirstIn(file) match {

      case Some(extension: String) => {

        val jsonFile = new java.io.File(file)

        val json: JValue = parse(jsonFile)

        val basename = file.stripSuffix(extension)

        val pathSegments =
          prefixMatcher.replaceFirstIn(basename, "").split('/').toList

        val (elasticType :: _) = pathSegments
          
        val resourcePath = elasticIndex + "/" + elasticType

        val learnStds: dsl.LearningStandards = dsl.LearningStandards.fromJson(json)

        val responses: Traversable[Future[JValue]] = for {
          std <- learnStds.items.toTraversable
        } yield {
          val response: Future[JValue] = uploadStandard(resourcePath, std)

          response.onComplete {

            case Success(j: JValue) => {
              val JString(id: String) = j \\ "_id"
              // println(s"SUCCESS (_id: $id)")
              print(".")
            }
            case Failure(t) => {
              println("FAIL")
              println(t.getMessage)
            }
          }
          response
        }

        // Ask Elastic for the count:
        def count: Future[BigInt] = {
          val url = pathToUrl(resourcePath)
          for {
            j <- http(url / "_count" OK as.json4s.Json)
          } yield {
            j \\ "count" match {
              case JInt(n: BigInt) => n
              case _ => -1: BigInt
            }
          }
        }

        // After the uploads complete, get the count and expect it
        // to be wrong per Elastic's near real-time (NRT)...
        Future.sequence(responses) flatMap { rs =>
          for {
            c <- count
          } yield {
            println
            if (responses.size != c) {
              println(s"ERROR: File items (${responses.size}) did not equal index count ($c)")
            } else {
              println(s"Items: ${responses.size}")
              println(s"PUTs: ${rs.size}")
              println(s"Loaded: $c")
            }
            FileUploaded(file, c)
          }
        }
      }
      case _ => {
        println(s"$file: missing .json extension")
         // FIXME: Should use an Either type.
        Future(FileUploaded(file, 0: BigInt))
      }
    }
  }

  def uploadStandard(path: String, std: dsl.LearningStandard): Future[JValue] = {
    val url = pathToUrl(path)
    val jsonSnippetSize = 32
    val jsonSnippet = std.toJson.take(jsonSnippetSize)
    // println(s"PUT ${url.url} '$jsonSnippet ...}'") // DEBUG

    http(url << std.toJson OK as.json4s.Json)
  }

  def pathToUrl(path: String): Req =
    path.split("/").foldLeft(elasticHost)(_ / _)
}
