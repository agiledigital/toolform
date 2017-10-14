package au.com.agiledigital.toolform.model

/**
  * Groups together components that will be built by a single pass of a docker builder.
  */
final case class ComponentGroup(id: String, path: String, subPath: String, name: String, builder: String) extends ProjectElement {}
