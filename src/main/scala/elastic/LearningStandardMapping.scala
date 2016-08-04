package ccssi
package elastic

import scala.Predef._

import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

object LearningStandardMapping {

  def json(names: String*) = compact(render(Map("mappings" -> (forTypes _)(names))))

  def forTypes(names: String*) = {
    for {
      name <- names
    } yield {
      (name -> properties)
    }
  }.toMap

  val properties = Map(
    "properties" ->
      Map(
        "refId" ->
          Map(
            "type" -> "string",
            "index" -> "not_analyzed"
          ),
        "refUri" ->
          Map(
            "type" -> "string",
            "index" -> "not_analyzed"
          ),
        "standardHierarchyLevel.number" ->
          Map("type" -> "long"),
        "statementCodes" ->
          Map(
            "type" -> "string",
            "index" -> "not_analyzed"
          ),
        "statements" ->
          Map("type" -> "string"),
        "gradeLevels" ->
          Map(
            "type" -> "string",
            "index" -> "not_analyzed"
          ),
        "learningStandardDocumentRefId" ->
          Map(
            "type" -> "string",
            "index" -> "not_analyzed"
          ),
        "related.relation" ->
          Map(
            "type" -> "string",
            "index" -> "not_analyzed"
          ),
        "related.refId" ->
          Map(
            "type" -> "string",
            "index" -> "not_analyzed"
          ),
        "children" ->
          Map(
            "type" -> "string",
            "index" -> "not_analyzed"
          )
      )
  )
}
