package com.mesosphere.cosmos.rpc.v1.model

import com.mesosphere.cosmos.thirdparty.marathon.model.AppId

case class UninstallRequest(
  packageName: String,
  appId: Option[AppId],
  all: Option[Boolean]
)
