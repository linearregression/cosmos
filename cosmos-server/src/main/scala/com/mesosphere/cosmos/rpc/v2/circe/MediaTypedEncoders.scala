package com.mesosphere.cosmos.rpc.v2.circe

import com.mesosphere.cosmos.circe.{DispatchingMediaTypedEncoder, MediaTypedEncoder}
import com.mesosphere.cosmos.converter
import com.mesosphere.cosmos.converter.Response._
import com.mesosphere.cosmos.http.MediaTypes
import com.mesosphere.cosmos.rpc
import com.mesosphere.cosmos.internal
import com.twitter.bijection.Conversion.asMethod
import com.twitter.util.Try

object MediaTypedEncoders {

  implicit val packageDescribeResponseEncoderV2: DispatchingMediaTypedEncoder[internal.model.PackageDefinition, MediaTypes.V2DescribeResponseType] = {
    DispatchingMediaTypedEncoder(
      MediaTypedEncoder(
        encoder = rpc.v2.circe.Encoders.encodeV2DescribeResponse.contramap { (pkgDefinition: internal.model.PackageDefinition) =>
          converter.Universe.internalPackageDefinitionToV2DescribeResponse(pkgDefinition)
        }
      )
    )
  }
  implicit val packageDescribeResponseEncoderV1: DispatchingMediaTypedEncoder[internal.model.PackageDefinition, MediaTypes.V1DescribeResponseType] = {
    DispatchingMediaTypedEncoder(
      MediaTypedEncoder(
        encoder = rpc.v1.circe.Encoders.encodeDescribeResponse.contramap[internal.model.PackageDefinition] { pkg =>
          pkg.as[Try[rpc.v1.model.DescribeResponse]].get()
        },
        mediaType = MediaTypes.V1DescribeResponse
      )
    )
  }

  implicit val packageInstallResponseEncoderV2: DispatchingMediaTypedEncoder[rpc.v2.model.InstallResponseMediaTypes.V2InstallResponseType] = {
    DispatchingMediaTypedEncoder(
      MediaTypedEncoder(
        encoder = rpc.v2.circe.Encoders.encodeV2InstallResponse
      )
    )
  }

  implicit val packageInstallResponseEncoderV1: DispatchingMediaTypedEncoder[rpc.v2.model.InstallResponseMediaTypes.V1InstallResponseType] = {
    DispatchingMediaTypedEncoder(
      MediaTypedEncoder(
        encoder = rpc.v1.circe.Encoders.encodeInstallResponse.contramap { (x: rpc.v2.model.InstallResponse) =>
          x.as[Try[rpc.v1.model.InstallResponse]].get()
        }
      )
    )
  }
}
