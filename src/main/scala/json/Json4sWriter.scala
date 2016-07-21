package ccssi
package json

import java.lang.String
import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write

trait Json4sWriter {

  implicit val formats = Serialization.formats(NoTypeHints)

  def toJson: String = write(this)
}
