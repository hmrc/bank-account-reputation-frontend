/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import org.pac4j.core.engine.DefaultCallbackLogic
import org.pac4j.oidc.profile.OidcProfile
import org.pac4j.play.PlayWebContext
import org.pac4j.play.http.PlayHttpActionAdapter
import org.pac4j.play.scala.{Security, SecurityComponents}
import play.api.mvc.{Action, AnyContent}
import play.api.{Configuration, Logging}
import play.mvc.Result

import javax.inject.{Inject, Singleton}

@Singleton
class KeycloakController @Inject() (val controllerComponents: SecurityComponents, configuration: Configuration)
  extends Security[OidcProfile]
    with Logging {

  val createUpstreamKeycloakSessionAndIgnoreIt: Action[AnyContent] = Secure("OidcClient") {
    Redirect(
      routes.BarsController.getVerify.url
    ).withNewSession // this is to avoid a need for a logout in this app, we don't actually make any use of logged-in user
  }

  val callback: Action[AnyContent] = Action { request =>
    val adapter        = new PlayHttpActionAdapter
    val callbackLogic  = new DefaultCallbackLogic[Result, PlayWebContext]
    val playWebContext = new PlayWebContext(request.asJava, playSessionStore)

    callbackLogic
      .perform(
        playWebContext,
        config,
        adapter,
        "/",
        true, //this.saveInSession,
        true, // this.multiProfile,
        false, // this.renewSession,
        "OidcClient" // this.defaultClient
      )
      .asScala()
  }

}
