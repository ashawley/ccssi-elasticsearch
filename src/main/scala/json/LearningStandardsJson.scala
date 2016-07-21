package ccssi
package json

import org.json4s._

trait LearningStandardsJson {

  implicit val formats = DefaultFormats

  def fromJson(json: JValue): dsl.LearningStandards =
    json.extract[dsl.LearningStandards]
}
