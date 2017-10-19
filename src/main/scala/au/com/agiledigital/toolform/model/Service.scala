package au.com.agiledigital.toolform.model

trait Service {

  def environment: Map[String, String]
  def exposedPorts: List[String]
}
