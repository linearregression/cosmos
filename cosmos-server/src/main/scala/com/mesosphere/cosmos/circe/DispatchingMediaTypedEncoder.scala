package com.mesosphere.cosmos.circe

import com.mesosphere.cosmos.http.MediaType
import com.mesosphere.cosmos.http.MediaTypes
import com.mesosphere.cosmos.http.MediaTypeOps.mediaTypeToMediaTypeOps
import io.circe.Encoder

/** Allows an [[io.circe.Encoder]] to be selected by a [[com.mesosphere.cosmos.http.MediaType]]. */
final class DispatchingMediaTypedEncoder[A,CT<:String] private(
  private[this] val encoder: MediaTypedEncoder[A]
) {

  def apply(mediaType: MediaType): Option[MediaTypedEncoder[A]] = {
    if(MediaTypes.fromContentType[CT].isCompatibleWith(mediaType)) {
      Some(encoder)
    } else {
      None
    }
  }
}

object DispatchingMediaTypedEncoder {

  def apply[A,CT<:String](encoder: MediaTypedEncoder[A]): DispatchingMediaTypedEncoder[A,CT] = {
    new DispatchingMediaTypedEncoder(encoder)
  }

  def apply[A,CT<:String]()(implicit
    encoder: Encoder[A]
  ): DispatchingMediaTypedEncoder[A,CT] = {
    DispatchingMediaTypedEncoder(MediaTypedEncoder(encoder))
  }

}
