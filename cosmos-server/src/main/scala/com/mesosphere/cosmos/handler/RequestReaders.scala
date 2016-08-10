package com.mesosphere.cosmos.handler

import cats.syntax.option._
import com.mesosphere.cosmos.circe.{DispatchingMediaTypedEncoder, MediaTypedDecoder, MediaTypedEncoder}
import com.mesosphere.cosmos.http.FinchExtensions._
import com.mesosphere.cosmos.http.{Authorization, MediaType, RequestSession}
import io.finch._

object RequestReaders {

  def noBody[Res, CT<:String](implicit
    produces: DispatchingMediaTypedEncoder[Res, CT]
  ): Endpoint[EndpointContext[Unit, Res, CT]] = {
    baseReader(produces).map { case (session, responseEncoder) =>
      EndpointContext((), session, responseEncoder)
    }
  }

  def standard[Req, Res, CT<:String](implicit
    accepts: MediaTypedDecoder[Req],
    produces: DispatchingMediaTypedEncoder[Res, CT]
  ): Endpoint[EndpointContext[Req, Res, CT]] = {
    for {
      (reqSession, responseEncoder) <- baseReader(produces)
      _ <- header("Content-Type").as[MediaType].should(beTheExpectedType(accepts.mediaType))
      req <- body.as[Req](accepts.decoder, accepts.classTag)
    } yield {
      EndpointContext(req, reqSession, responseEncoder)
    }
  }

  private[this] def baseReader[Res, CT<:String](
    produces: DispatchingMediaTypedEncoder[Res, CT]
  ): Endpoint[(RequestSession, MediaTypedEncoder[Res, CT])] = {
    for {
      responseEncoder <- header("Accept")
        .as[MediaType]
        .convert { accept =>
          produces(accept)
            .toRightXor(s"should match one of: ${produces.mediaTypes.map(_.show).mkString(", ")}")
        }
      auth <- headerOption("Authorization")
    } yield {
      (RequestSession(auth.map(Authorization(_))), responseEncoder)
    }
  }

}
