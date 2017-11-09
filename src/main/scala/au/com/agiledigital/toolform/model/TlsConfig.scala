package au.com.agiledigital.toolform.model

import enumeratum.EnumEntry.CapitalWords
import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable.IndexedSeq

case class TlsConfig(enabled: Boolean = false, tlsTerminationType: TlsTerminationType = TlsTerminationType.Passthrough, tlsInsecureEdgePolicy: TlsInsecureEdgePolicy = TlsInsecureEdgePolicy.Allow)

/**
  * An enumeration representing the different types of TLS termination on a route.
  * @see https://docs.openshift.com/enterprise/3.0/architecture/core_concepts/routes.html#secured-routes
  */
sealed trait TlsTerminationType extends EnumEntry with CapitalWords

object TlsTerminationType extends Enum[TlsTerminationType] {
  val values: IndexedSeq[TlsTerminationType] = findValues

  case object Edge        extends TlsTerminationType
  case object Passthrough extends TlsTerminationType
}

/**
  * An enumeration representing the different policies of handling insecure endpoints.
  * @see https://docs.openshift.com/enterprise/3.1/architecture/core_concepts/routes.html#secured-routes
  */
sealed trait TlsInsecureEdgePolicy extends EnumEntry with CapitalWords

object TlsInsecureEdgePolicy extends Enum[TlsInsecureEdgePolicy] {
  val values: IndexedSeq[TlsInsecureEdgePolicy] = findValues

  case object None     extends TlsInsecureEdgePolicy
  case object Allow    extends TlsInsecureEdgePolicy
  case object Redirect extends TlsInsecureEdgePolicy
}
