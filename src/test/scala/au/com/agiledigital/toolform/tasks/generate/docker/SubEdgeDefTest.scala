package au.com.agiledigital.toolform.tasks.generate.docker

import au.com.agiledigital.toolform.model._
import org.scalatest.{FlatSpec, Matchers, PrivateMethodTester}

class SubEdgeDefTest extends FlatSpec with Matchers with PrivateMethodTester {

  "subEdgeDefsFromProject" should "return an empty list if there are no edges defined" in {
    val testProject = Project(
      id = "",
      name = "",
      components = Map(),
      resources = Map(),
      topology = Topology(links = List(), edges = Map()),
      volumes = None,
      componentGroups = None
    )

    val output = SubEdgeDef.subEdgeDefsFromProject(testProject)
    output shouldBe empty
  }

  "subEdgeDefsFromProject" should "return an empty list if there are no subedges defined" in {
    val testProject = Project(
      id = "",
      name = "",
      components = Map(),
      resources = Map(),
      topology = Topology(links = List(),
                          edges = Map(
                            "anEdge" -> Edge(subEdges = Map())
                          )),
      volumes = None,
      componentGroups = None
    )

    val output = SubEdgeDef.subEdgeDefsFromProject(testProject)
    output shouldBe empty
  }
}
