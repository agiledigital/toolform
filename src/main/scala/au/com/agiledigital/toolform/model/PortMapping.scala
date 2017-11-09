package au.com.agiledigital.toolform.model

import au.com.agiledigital.toolform.util.ConfigHelpers.handleErrorWithEither
import au.com.agiledigital.toolform.util.StringUtil
import enumeratum.{Enum, EnumEntry}
import pureconfig._

import scala.collection.immutable.IndexedSeq

/**
  * Defines a port mapping between two project elements.
  *
  * @param port        the port that is exposed by the source.
  * @param targetPort  the port that is exposed by the target.
  * @param protocol    the protocol used by the port. This defaults to Tcp.
  */
final case class PortMapping(port: Int, targetPort: Int, protocol: PortProtocolType = PortProtocolType.Tcp) {

  /**
    * Returns the formatted string representation of this object that can be parsed back in again with the
    * parsePortMappingFromConfigString function. It is compatible with Docker Compose port format.
    *
    * @see https://docs.docker.com/compose/compose-file/#short-syntax-1
    *
    * @return the string representation of this object.
    */
  def toPortString: String = {
    val protocolString = protocol.toString.toLowerCase
    if (port == targetPort) {
      s"$port/$protocolString"
    } else {
      s"$port:$targetPort/$protocolString"
    }
  }
}

object PortMapping {

  private final val PortMappingRegex = """^(\d+)(?::(\d+))?(?:\/(udp|tcp))?$""".r

  /**
    * Takes a string in format {@literal<port>[:containerPort][/udp|/tcp]} and attempts parses it into a PortMapping object.
    * @param value the string to parse.
    * @return a PortMapping object on a successful parse, otherwise a string explaining what went wrong.
    */
  def parsePortMappingFromConfigString(value: String): Either[String, PortMapping] =
    value match {
      case PortMappingRegex(portString, maybeContainerPortString, maybeProtocolString) if StringUtil.isInt(portString) =>
        val port       = portString.toInt
        val targetPort = Option(maybeContainerPortString).flatMap(StringUtil.toIntOpt).getOrElse(port)
        val protocol   = Option(maybeProtocolString).map(PortProtocolType.withNameInsensitive).getOrElse(PortProtocolType.Tcp)
        Right(PortMapping(port, targetPort, protocol))
      case _ => Left(s"Port value [$value] should be in the format <port>[:containerPort][/udp|/tcp]")
    }

  /**
    * Allows strings to be mapped into PortMapping objects during config parsing.
    */
  implicit val portMappingReader: ConfigReader[PortMapping] = ConfigReader.fromString[PortMapping](handleErrorWithEither(parsePortMappingFromConfigString))
}

/**
  * An enumeration representing types a subedge can be.
  */
sealed trait PortProtocolType extends EnumEntry

object PortProtocolType extends Enum[PortProtocolType] {
  val values: IndexedSeq[PortProtocolType] = findValues

  case object Tcp extends PortProtocolType
  case object Udp extends PortProtocolType
}
