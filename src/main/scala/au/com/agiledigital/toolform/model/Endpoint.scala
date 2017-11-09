package au.com.agiledigital.toolform.model

import au.com.agiledigital.toolform.model.Endpoint.EndpointType
import enumeratum.EnumEntry._
import enumeratum.{Enum, EnumEntry}
import pureconfig._

import scala.collection.immutable.IndexedSeq

/**
  * An Endpoint makes some of the project Components and Resources available externally.
  *
  * E.g.
  * {{{
  *
  * endpoints {
  *   public-api: {
  *     type: http
  *     target: public_api
  *     port-mapping: 80:9000,
  *     tls: {
  *       tls-termination-type: Edge,
  *       tls-insecure-edge-policy: Redirect
  *     }
  *   },
  *   public: {
  *     target: public_www
  *     port-mapping: 80
  *   }
  * }
  *
  * }}}
  *
  * Will create two endpoints named 'public-api' and 'public' it will use the target and port-mapping properties to map to a service.
  *
  * @param endpointType   the type of the endpoint.
  * @param target         the ID of the [[ToolFormService]] that this endpoint will connect to.
  * @param portMapping    the mapping of ports from the endpoint to the target service.
  * @param tlsConfig      An object describing the behaviour of TLS on the endpoint.
  */
case class Endpoint(endpointType: EndpointType, target: String, portMapping: PortMapping, tlsConfig: TlsConfig = TlsConfig()) extends ProjectElement {

  override def id: String = "TODO: endpoint id" //s"some_route"
}

object Endpoint {

  /**
    * 'type' is a reserved keyword in scala so this maps "type" to "endpointType"
    */
  implicit val fieldMapping: ProductHint[Endpoint] =
    ProductHint[Endpoint](ConfigFieldMapping(CamelCase, KebabCase).withOverrides("endpointType" -> "type"))

  /**
    * An enumeration representing the types an endpoint can be.
    */
  sealed trait EndpointType extends EnumEntry with CapitalWords

  object EndpointType extends Enum[EndpointType] {
    val values: IndexedSeq[EndpointType] = findValues

    case object Http  extends EndpointType
    case object Https extends EndpointType
  }
}
