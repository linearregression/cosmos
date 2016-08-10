package com.mesosphere.cosmos.rpc.v1.circe

import com.mesosphere.cosmos.circe.DispatchingMediaTypedEncoder
import com.mesosphere.cosmos.http.MediaTypes
import com.mesosphere.cosmos.rpc.v1.circe.Encoders._
import com.mesosphere.cosmos.rpc.v1.model._

object MediaTypedEncoders {

  implicit val capabilitiesEncoder: DispatchingMediaTypedEncoder[CapabilitiesResponse, MediaTypes.CapabilitiesResponseType] =
    DispatchingMediaTypedEncoder()

  implicit val packageListV1Encoder: DispatchingMediaTypedEncoder[ListResponse, MediaTypes.V1ListResponseType] =
    DispatchingMediaTypedEncoder()

  implicit val packageListVersionsEncoder: DispatchingMediaTypedEncoder[ListVersionsResponse, MediaTypes.ListVersionsResponseType] =
    DispatchingMediaTypedEncoder()

  implicit val packageDescribeV1Encoder: DispatchingMediaTypedEncoder[DescribeResponse,MediaTypes.V1DescribeResponseType] =
    DispatchingMediaTypedEncoder()

  implicit val packageInstallV1Encoder: DispatchingMediaTypedEncoder[InstallResponse,MediaTypes.V1InstallResponseType] =
    DispatchingMediaTypedEncoder()

  implicit val packageRenderEncoder: DispatchingMediaTypedEncoder[RenderResponse,MediaTypes.RenderResponseType] =
    DispatchingMediaTypedEncoder()

  implicit val packageRepositoryAddEncoder: DispatchingMediaTypedEncoder[PackageRepositoryAddResponse,MediaTypes.PackageRepositoryAddResponseType] =
    DispatchingMediaTypedEncoder()

  implicit val packageRepositoryDeleteEncoder: DispatchingMediaTypedEncoder[PackageRepositoryDeleteResponse,MediaTypes.PackageRepositoryDeleteResponseType] =
    DispatchingMediaTypedEncoder()

  implicit val packageRepositoryListEncoder: DispatchingMediaTypedEncoder[PackageRepositoryListResponse,MediaTypes.PackageRepositoryListResponseType] =
    DispatchingMediaTypedEncoder()

  implicit val packageSearchEncoder: DispatchingMediaTypedEncoder[SearchResponse,MediaTypes.SearchResponseType] =
    DispatchingMediaTypedEncoder()

  implicit val packageUninstallEncoder: DispatchingMediaTypedEncoder[UninstallResponse,MediaTypes.UninstallResponseType] =
    DispatchingMediaTypedEncoder()

}
