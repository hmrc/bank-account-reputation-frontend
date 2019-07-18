
package config

import play.api.libs.json.Json

import scala.io.Source

trait AppVersion {

  private val stream = getClass.getResourceAsStream("/provenance.json")
  val appVersion: String = (Json.parse(Source.fromInputStream(stream).mkString) \ "version").as[String]
  stream.close()

}
