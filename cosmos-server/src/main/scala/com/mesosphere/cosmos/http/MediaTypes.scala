package com.mesosphere.cosmos.http
import shapeless.Witness

object MediaTypes {
  private[this] def vnd(kind: String, version: Int = 1): MediaType =
    MediaType(
      "application",
      MediaTypeSubType(s"vnd.dcos.package.$kind", Some("json")),
      Map("charset" -> "utf-8", "version" -> ("v" + version))
    )

  val any = MediaType("*", MediaTypeSubType("*"))
  val applicationJson = MediaType("application", MediaTypeSubType("json"), Map("charset" -> "utf-8"))
  val applicationZip = MediaType("application", MediaTypeSubType("zip"))

  val UninstallRequest = vnd("uninstall-request")
  val UninstallResponse = vnd("uninstall-response")

  val ListRequest = vnd("list-request")
  val ListResponse = vnd("list-response")

  val ErrorResponse = vnd("error")
  val InstallRequest = vnd("install-request")
  val RenderRequest = vnd("render-request")
  val RenderResponse = vnd("render-response")
  val SearchRequest = vnd("search-request")
  val SearchResponse = vnd("search-response")
  val DescribeRequest = vnd("describe-request")
  val ListVersionsRequest = vnd("list-versions-request")
  val ListVersionsResponse = vnd("list-versions-response")
  val CapabilitiesResponse = MediaType(
    "application",
    MediaTypeSubType("vnd.dcos.capabilities", Some("json")),
    Map("charset" -> "utf-8", "version" -> "v1")
  )

  val PackageRepositoryListRequest = vnd("repository.list-request")
  val PackageRepositoryListResponse = vnd("repository.list-response")
  val PackageRepositoryAddRequest = vnd("repository.add-request")
  val PackageRepositoryAddResponse = vnd("repository.add-response")
  val PackageRepositoryDeleteRequest = vnd("repository.delete-request")
  val PackageRepositoryDeleteResponse = vnd("repository.delete-response")

  val UniverseV3Repository = MediaType(
    "application",
    MediaTypeSubType("vnd.dcos.universe.repo", Some("json")),
    Map("charset" -> "utf-8", "version" -> "v3")
  )

  val V1DescribeResponse = vnd("describe-response", 1)
  val V2DescribeResponse = vnd("describe-response", 2)
  val V1InstallResponse = vnd("install-response", 1)
  val V2InstallResponse = vnd("install-response", 2)
  val V1ListResponse = vnd("list-response", 1)
  val V2ListResponse = vnd("list-response", 2)

	type V2DescribeResponseType = Witness.`"application/vnd.dcos.package.describe-response+json; charset=utf-8; version=v2;"`.T  
	type V1DescribeResponseType = Witness.`"application/vnd.dcos.package.describe-response+json; charset=utf-8; version=v1;"`.T  

	type V2InstallResponseType = Witness.`"application/vnd.dcos.package.install-response+json; charset=utf-8; version=v2;"`.T  
	type V1InstallResponseType = Witness.`"application/vnd.dcos.package.install-response+json; charset=utf-8; version=v1;"`.T  

	type CapabilitiesResponseType = Witness.`"application/vnd.dcos.capabilities+json; charset=utf-8; version=v1;"`.T  
	type V1ListResponseType = Witness.`"application/vnd.dcos.package.list-response+json; charset=utf-8; version=v1;"`.T  

	type ListVersionsResponseType = Witness.`"application/vnd.dcos.package.list-versions-response+json; charset=utf-8; version=v1;"`.T  

	type RenderResponseType = Witness.`"application/vnd.dcos.package.render-response+json; charset=utf-8; version=v1;"`.T  

	type PackageRepositoryAddResponseType = Witness.`"application/vnd.dcos.package.repository.add-response+json; charset=utf-8; version=v1;"`.T  
	type PackageRepositoryDeleteResponseType = Witness.`"application/vnd.dcos.package.repository.delete-response+json; charset=utf-8; version=v1;"`.T  

	type PackageRepositoryListResponseType = Witness.`"application/vnd.dcos.package.repository.list-response+json; charset=utf-8; version=v1;"`.T  

	type SearchResponseType = Witness.`"application/vnd.dcos.package.search-response+json; charset=utf-8; version=v1;"`.T  
	type UninstallResponseType = Witness.`"application/vnd.dcos.package.uninstall-response+json; charset=utf-8; version=v1;"`.T  
	type DescribeRequestType = Witness.`"application/vnd.dcos.package.describe-request+json; charset=utf-8; version=v1;"`.T  
	type ErrorResponseType = Witness.`"application/vnd.dcos.package.error+json; charset=utf-8; version=v1;"`.T  
  def fromContentType[CT<:String]:MediaType = ???
}

