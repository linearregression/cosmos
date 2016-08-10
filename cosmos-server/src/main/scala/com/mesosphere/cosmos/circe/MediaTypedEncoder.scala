package com.mesosphere.cosmos.circe

import com.mesosphere.cosmos.http.MediaType
import io.circe.Encoder

/** Associates a media type with an [[io.circe.Encoder]] instance. */
final class MediaTypedEncoder[A,CT <: String] private(val encoder: Encoder[A]) {
  type ContentType = CT
}

object MediaTypedEncoder {

  def apply[A](encoder: Encoder[A]): MediaTypedEncoder[A] = {
    new MediaTypedEncoder(encoder)
  }

}
