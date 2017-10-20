package au.com.agiledigital.toolform.model

import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.{FlatSpec, Matchers}
import org.scalacheck.Prop.forAll

class PortMappingTest extends FlatSpec with Matchers {

  private implicit final val ArbitraryProtocol: Arbitrary[PortProtocolType] = Arbitrary(Gen.oneOf(PortProtocolType.Tcp, PortProtocolType.Udp))

  "parsePortMappingFromConfigString" should "parse a valid port string" in {
    val validStrings = List(
      "3000",
      "8000:8000",
      "49100:22",
      "9000/udp",
      "9000:80/tcp",
      "6060:6060/udp"
    )

    noException should be thrownBy {
      validStrings.foreach(PortMapping.parsePortMappingFromConfigString)
    }
  }

  "parsePortMappingFromConfigString" should "throw an exception for an invalid port string" in {
    // Ranges and IP addresses are accepted by some tools such Docker Compose but not others
    // such as Kubernetes. To keep things simple for now they are specified as invalid.
    val invalidStrings = List(
      "3000-3005",
      "9090-9091:8080-8081",
      "127.0.0.1:8001:8001",
      "127.0.0.1:5000-5010:5000-5010",
      "10/xdp",
      "80/https",
      "80:8080/https",
      "80:/udp"
    )

    invalidStrings.foreach {
      PortMapping.parsePortMappingFromConfigString(_).isLeft should equal(true)
    }
  }

  "parsePortMappingFromConfigString" should "handle partial matching" in {
    PortMapping.parsePortMappingFromConfigString("80") should matchPattern { case Right(PortMapping(80, 80, PortProtocolType.Tcp))            => }
    PortMapping.parsePortMappingFromConfigString("80:8080") should matchPattern { case Right(PortMapping(80, 8080, PortProtocolType.Tcp))     => }
    PortMapping.parsePortMappingFromConfigString("80:8080/udp") should matchPattern { case Right(PortMapping(80, 8080, PortProtocolType.Udp)) => }
    PortMapping.parsePortMappingFromConfigString("80/udp") should matchPattern { case Right(PortMapping(80, 80, PortProtocolType.Udp))        => }
    PortMapping.parsePortMappingFromConfigString("80/tcp") should matchPattern { case Right(PortMapping(80, 80, PortProtocolType.Tcp))        => }
  }

  "parsePortMappingFromConfigString" should "always be able to parse its own output" in {
    forAll { (port: Int, containerPort: Int, protocol: PortProtocolType) =>
      val originalPortMapping     = PortMapping(port, containerPort, protocol)
      val parsedPortMappingResult = PortMapping.parsePortMappingFromConfigString(originalPortMapping.toPortString)
      parsedPortMappingResult match {
        case Right(parsedPortMapping) => originalPortMapping == parsedPortMapping
        case Left(_)                  => false
      }
    }
  }

  "parsePortMappingFromConfigString" should "always output the same string that is parsed in" in {
    forAll { (port: Int, containerPort: Int, protocol: PortProtocolType) =>
      val protocolLower      = protocol.toString.toLowerCase
      val originalPortString = s"$port:$containerPort/$protocolLower"
      val portMappingResult  = PortMapping.parsePortMappingFromConfigString(originalPortString)
      portMappingResult match {
        case Right(parsedPortMapping) => originalPortString == parsedPortMapping.toPortString
        case Left(_)                  => false
      }
    }
  }
}
