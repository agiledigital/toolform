package au.com.agiledigital.toolform.model

trait Service {

  def environment: Option[Map[String, String]]
  def exposedPorts: Option[List[String]]
}
