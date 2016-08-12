package com.mesosphere.cosmos.handler

import com.mesosphere.cosmos.http.RequestSession
import com.twitter.util.Future
import com.twitter.finagle.http.Response
import io.circe.Json
import io.circe.syntax._
import io.finch._

private[cosmos] abstract class EndpointHandler[Request, Res, CT<:String] {

  final def apply(context: EndpointContext[Request, Res]): Future[Output[Response]] = {
    apply(context.requestBody)(context.session).map { response =>
      val encodedResponse = response.asJson(context.responseEncoder.encoder)
      //TODO: Ok(Ok(..)) should be fixed by finch 0.11 release
      Ok(Ok(encodedResponse).toResponse[CT]())
    }
  }

  def apply(request: Request)(implicit session: RequestSession): Future[Res]

}
