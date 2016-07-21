package ccssi
package dsl

import java.lang.String
import scala.collection.immutable.List

case class LearningStandard(
  refId: String,
  refUri: String,
  standardHierarchyLevel: StandardHierarchyLevel,
  statementCodes: List[String],
  statements: List[String],
  gradeLevels: List[String],
  learningStandardDocumentRefId: String,
  related: List[RelatedLearningStandard],
  children: List[String]
) extends json.Json4sWriter
