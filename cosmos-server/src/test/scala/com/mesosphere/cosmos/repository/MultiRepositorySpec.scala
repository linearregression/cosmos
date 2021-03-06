package com.mesosphere.cosmos

import com.mesosphere.cosmos.internal.model.PackageDefinition
import com.mesosphere.cosmos.internal.model.CosmosInternalRepository
import com.mesosphere.cosmos.repository.PackageSourcesStorage
import com.mesosphere.cosmos.repository.UniverseClient
import com.mesosphere.universe.v3.model.PackageDefinition.Version
import com.mesosphere.cosmos.rpc.v1.model.PackageRepository
import org.scalatest.FreeSpec
import com.mesosphere.cosmos.http.RequestSession
import com.mesosphere.cosmos.test.TestUtil
import com.mesosphere.cosmos.test.TestUtil.Anonymous //RequestSession implicit
import com.twitter.util.{Future,Await,Throw,Try,Return}
import com.netaporter.uri.Uri
import cats.data.Ior
import cats.data.Ior.{Left,Right,Both}

final class MultiRepositorySpec extends FreeSpec {

  case class TestClient(repos: List[PackageRepository] = Nil, ls: List[PackageDefinition] = Nil) extends UniverseClient {
    def apply(repo: PackageRepository)(implicit session: RequestSession): Future[CosmosInternalRepository] = Future { 
      CosmosInternalRepository(repos.filter( _ == repo).flatMap(_ => ls))
    }
  }

  case class TestStorage(initial:List[PackageRepository] = Nil) extends PackageSourcesStorage {
    var cache: List[PackageRepository] = initial
    def read(): Future[List[PackageRepository]] =  Future { cache }
    def readCache(): Future[List[PackageRepository]] =  Future { cache }
    def add(index: Option[Int], packageRepository: PackageRepository): Future[List[PackageRepository]] = ???
    def delete(nameOrUri: Ior[String, Uri]): Future[List[PackageRepository]] =  ???
  }

  case class TestMultiClient(repos: List[(PackageRepository,List[PackageDefinition])] = Nil) extends UniverseClient {
    def apply(repo: PackageRepository)(implicit session: RequestSession): Future[CosmosInternalRepository] = Future {
      CosmosInternalRepository(repos.filter( _._1 == repo).flatMap(_._2))
    }
  }
  "getRepository" - {
    "empty" in {
      val c = new MultiRepository(TestStorage(), TestClient())
      assertResult(None)(Await.result(c.getRepository(Uri.parse("/test"))))
    }
    "many" in {
      val one = PackageRepository("one", Uri.parse("/one"))
      val two = PackageRepository("two", Uri.parse("/two"))
      val c = new MultiRepository(TestStorage(List(one,two)), TestClient())
      assertResult(None)(Await.result(c.getRepository(Uri.parse("/zero"))))
      assertResult(one)(Await.result(c.getRepository(Uri.parse("/one"))).get.repository)
      assertResult(two)(Await.result(c.getRepository(Uri.parse("/two"))).get.repository)
    }
    "many same uri" in {
      val one = PackageRepository("one", Uri.parse("/same"))
      val two = PackageRepository("two", Uri.parse("/same"))
      val c = new MultiRepository(TestStorage(List(one,two)), TestClient())
      assertResult(None)(Await.result(c.getRepository(Uri.parse("/zero"))))
      assertResult(two.uri)(Await.result(c.getRepository(Uri.parse("/same"))).get.repository.uri)
    }
  }

  "queries" - {
    "not found" in {
      val c = new MultiRepository(TestStorage(), TestClient())
      val ver = TestUtil.MinimalPackageDefinition.version

      assertResult(Throw(new PackageNotFound("test")))(Try(Await.result(c.getPackagesByPackageName("test"))))

      assertResult(Throw(new VersionNotFound("test", ver)))(Try(Await.result(c.getPackageByPackageVersion("test", Some(ver)))))
      assertResult(Throw(new PackageNotFound("test")))(Try(Await.result(c.getPackageByPackageVersion("test", None))))

      assertResult(Return(Nil))(Try(Await.result(c.search(Some("test")))))
      assertResult(Return(Nil))(Try(Await.result(c.search(None))))
    }

    "found minimal" in {
      val u = Uri.parse("/test")
      val repos = List(PackageRepository("minimal", u))
      val storage = TestStorage(repos)
      val cls = List(TestUtil.MinimalPackageDefinition)
      val client = TestClient(repos, cls)
      val c = new MultiRepository(storage, client)
      val ver = TestUtil.MinimalPackageDefinition.version

      assertResult(Return(cls))(Try(Await.result(c.getPackagesByPackageName("minimal"))))
      assertResult(Throw(new PackageNotFound("MAXIMAL")))(Try(Await.result(c.getPackagesByPackageName("MAXIMAL"))))

      assertResult(Return((cls.head, u)))(Try(Await.result(c.getPackageByPackageVersion("minimal", None))))
      assertResult(Return((cls.head, u)))(Try(Await.result(c.getPackageByPackageVersion("minimal", Some(ver)))))

      assertResult(Return(List("minimal")))(Try(Await.result(c.search(None)).map(_.name)))
      assertResult(Return(List("minimal")))(Try(Await.result(c.search(Some("minimal"))).map(_.name)))
    }

    "invalid repo" in {
      val invalid = List(PackageRepository("invalid", Uri.parse("/invalid")))
      val repos = List(PackageRepository("minimal", Uri.parse("/test")))
      val storage = TestStorage(repos)
      val cls = List(TestUtil.MinimalPackageDefinition)
      val client = TestClient(invalid, cls)
      val c = new MultiRepository(storage, client)
      val ver = TestUtil.MinimalPackageDefinition.version

      assertResult(Throw(new PackageNotFound("minimal")))(Try(Await.result(c.getPackagesByPackageName("minimal"))))

      assertResult(Throw(new PackageNotFound("minimal")))(Try(Await.result(c.getPackageByPackageVersion("minimal", None))))
      assertResult(Throw(new VersionNotFound("minimal",ver)))(Try(Await.result(c.getPackageByPackageVersion("minimal", Some(ver)))))
    }

    "wrong query" in {
      val repos = List(PackageRepository("valid", Uri.parse("/valid")))
      val storage = TestStorage(repos)
      val cls = List(TestUtil.MinimalPackageDefinition)
      val client = TestClient(repos, cls)
      val c = new MultiRepository(storage, client)
      val ver = TestUtil.MinimalPackageDefinition.version
      val badver = TestUtil.MaximalPackageDefinition.version

      assertResult(Throw(new PackageNotFound("MAXIMAL")))(Try(Await.result(c.getPackagesByPackageName("MAXIMAL"))))

      assertResult(Throw(new PackageNotFound("MAXIMAL")))(Try(Await.result(c.getPackageByPackageVersion("MAXIMAL", None))))
      assertResult(Throw(new VersionNotFound("MAXIMAL", ver)))(Try(Await.result(c.getPackageByPackageVersion("MAXIMAL", Some(ver)))))
      assertResult(Throw(new VersionNotFound("minimal", badver)))(Try(Await.result(c.getPackageByPackageVersion("minimal", Some(badver)))))
    }

    "from many" in {
      val u = Uri.parse("/valid")
      val repos = List(PackageRepository("valid", u))
      val storage = TestStorage(repos)
      val cls = List(TestUtil.MaximalPackageDefinition, TestUtil.MinimalPackageDefinition)
      val client = TestClient(repos, cls)
      val c = new MultiRepository(storage, client)
      val minver = TestUtil.MinimalPackageDefinition.version
      val minexp = (TestUtil.MinimalPackageDefinition, u)
      val maxver = TestUtil.MaximalPackageDefinition.version

      val expect = List(TestUtil.MinimalPackageDefinition)
      assertResult(Return(expect))(Try(Await.result(c.getPackagesByPackageName("minimal"))))

      assertResult(Return(minexp))(Try(Await.result(c.getPackageByPackageVersion("minimal", None))))
      assertResult(Return(minexp))(Try(Await.result(c.getPackageByPackageVersion("minimal", Some(minver)))))
      assertResult(Throw(new VersionNotFound("minimal", maxver)))(Try(Await.result(c.getPackageByPackageVersion("minimal", Some(maxver)))))

      val maxexp = (TestUtil.MaximalPackageDefinition, u)
      assertResult(Return(maxexp))(Try(Await.result(c.getPackageByPackageVersion("MAXIMAL", None))))
      assertResult(Return(maxexp))(Try(Await.result(c.getPackageByPackageVersion("MAXIMAL", Some(maxver)))))
      assertResult(Throw(new VersionNotFound("MAXIMAL", minver)))(Try(Await.result(c.getPackageByPackageVersion("MAXIMAL", Some(minver)))))

      assertResult(Return(List("MAXIMAL", "minimal")))(Try(Await.result(c.search(None)).map(_.name).sorted))
      assertResult(Return(List("minimal")))(Try(Await.result(c.search(Some("minimal"))).map(_.name)))
      assertResult(Return(List("MAXIMAL")))(Try(Await.result(c.search(Some("MAXIMAL"))).map(_.name)))
    }

    "multi repo multi same packages" in {
      val rmax = PackageRepository("max", Uri.parse("/MAXIMAL"))
      val rmin = PackageRepository("min", Uri.parse("/minimal"))
      val min2 = TestUtil.MinimalPackageDefinition.copy(version = Version("1.2.4"))
      val max2 = TestUtil.MaximalPackageDefinition.copy(version = Version("9.9.9"))
      val clientdata = List((rmax, List(TestUtil.MaximalPackageDefinition,max2)),
                            (rmin, List(TestUtil.MinimalPackageDefinition,min2)))
      val storage = TestStorage(List(rmax,rmin))
      val client = TestMultiClient(clientdata)
      val c = new MultiRepository(storage, client)

      assertResult(Return(2))(Try(Await.result(c.getPackagesByPackageName("minimal")).length))
      assertResult(Return(List(TestUtil.MinimalPackageDefinition,min2)))(Try(Await.result(c.getPackagesByPackageName("minimal"))))
      assertResult(Return(List(TestUtil.MaximalPackageDefinition,max2)))(Try(Await.result(c.getPackagesByPackageName("MAXIMAL"))))
      assertResult(Throw(new PackageNotFound("foobar")))(Try(Await.result(c.getPackagesByPackageName("foobar"))))

      val minver = TestUtil.MinimalPackageDefinition.version
      val minexp = (TestUtil.MinimalPackageDefinition, Uri.parse("/minimal"))

      assertResult(Return(minexp))(Try(Await.result(c.getPackageByPackageVersion("minimal", None))))
      assertResult(Return(minexp))(Try(Await.result(c.getPackageByPackageVersion("minimal", Some(minver)))))

      val maxver = TestUtil.MaximalPackageDefinition.version
      val maxexp = (TestUtil.MaximalPackageDefinition, Uri.parse("/MAXIMAL"))

      assertResult(Return(maxexp))(Try(Await.result(c.getPackageByPackageVersion("MAXIMAL", None))))
      assertResult(Return(maxexp))(Try(Await.result(c.getPackageByPackageVersion("MAXIMAL", Some(maxver)))))

      assertResult(Return(List("MAXIMAL", "minimal")))(Try(Await.result(c.search(None)).map(_.name).sorted))
      assertResult(Return(List("minimal")))(Try(Await.result(c.search(Some("minimal"))).map(_.name)))
      val min2ver = min2.version
      assertResult(Return(List(Set(minver, min2ver))))(Try(Await.result(c.search(Some("minimal"))).map(_.versions.keys)))

      val max2ver = max2.version
      assertResult(Return(List(Set(maxver, max2ver))))(Try(Await.result(c.search(Some("MAXIMAL"))).map(_.versions.keys)))
    }
    "multi repo multi different packages" in {
      val rmax = PackageRepository("max", Uri.parse("/MAXIMAL"))
      val rmin = PackageRepository("min", Uri.parse("/minimal"))
      val min2 = TestUtil.MinimalPackageDefinition.copy(version = Version("1.2.4"))
      val max2 = TestUtil.MaximalPackageDefinition.copy(version = Version("9.9.9"))
      val clientdata = List((rmax, List(TestUtil.MaximalPackageDefinition,min2)),
                            (rmin, List(TestUtil.MinimalPackageDefinition,max2)))
      val storage = TestStorage(List(rmax,rmin))
      val client = TestMultiClient(clientdata)
      val c = new MultiRepository(storage, client)

      assertResult(Return(1))(Try(Await.result(c.getPackagesByPackageName("minimal")).length))
      //will return the first repo
      assertResult(Return(List(min2)))(Try(Await.result(c.getPackagesByPackageName("minimal"))))
      assertResult(Return(List(TestUtil.MaximalPackageDefinition)))(Try(Await.result(c.getPackagesByPackageName("MAXIMAL"))))
      assertResult(Throw(new PackageNotFound("foobar")))(Try(Await.result(c.getPackagesByPackageName("foobar"))))

      val minexp1 = (min2, Uri.parse("/MAXIMAL"))
      assertResult(Return(minexp1))(Try(Await.result(c.getPackageByPackageVersion("minimal", None))))

      val minver = TestUtil.MinimalPackageDefinition.version
      val minexp2 = (TestUtil.MinimalPackageDefinition, Uri.parse("/minimal"))
      assertResult(Return(minexp2))(Try(Await.result(c.getPackageByPackageVersion("minimal", Some(minver)))))

      val maxver = TestUtil.MaximalPackageDefinition.version
      val maxexp = (TestUtil.MaximalPackageDefinition, Uri.parse("/MAXIMAL"))

      assertResult(Return(maxexp))(Try(Await.result(c.getPackageByPackageVersion("MAXIMAL", None))))
      assertResult(Return(maxexp))(Try(Await.result(c.getPackageByPackageVersion("MAXIMAL", Some(maxver)))))

      assertResult(Return(List("MAXIMAL", "minimal")))(Try(Await.result(c.search(None)).map(_.name).sorted))
      assertResult(Return(List("minimal")))(Try(Await.result(c.search(Some("minimal"))).map(_.name)))
      val min2ver = min2.version
      assertResult(Return(List(Set(min2ver))))(Try(Await.result(c.search(Some("minimal"))).map(_.versions.keys)))
      assertResult(Return(List(Set(maxver))))(Try(Await.result(c.search(Some("MAXIMAL"))).map(_.versions.keys)))
    }
  }
}
