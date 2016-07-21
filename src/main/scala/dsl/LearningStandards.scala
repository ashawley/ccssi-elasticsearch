package ccssi
package dsl

import java.lang.String
import scala.collection.immutable.List

case class LearningStandards(
  version: String,
  items: List[LearningStandard]
)

object LearningStandards extends json.LearningStandardsJson
