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

import com.google.inject.{AbstractModule, Provides}
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod
import config.AppConfig
import org.pac4j.core.client.Clients
import org.pac4j.core.config.{Config => Pac4JConfig}
import org.pac4j.oidc.client.OidcClient
import org.pac4j.oidc.config.OidcConfiguration
import org.pac4j.play.scala.{DefaultSecurityComponents, SecurityComponents}
import org.pac4j.play.store.{DataEncrypter, PlayCookieSessionStore, PlaySessionStore}
import play.api.{Configuration, Environment}
import play.api.libs.concurrent.AkkaGuiceSupport

class Module(environment: Environment, playConfig: Configuration) extends AbstractModule with AkkaGuiceSupport {

  private val frontendBaseUrl = {
    val protocol = playConfig.get[String]("cip-auth.frontend-protocol")
    val host = playConfig.get[String]("cip-auth.frontend-host")
    s"$protocol://$host"
  }

  override def configure(): Unit = {
    super.configure()

    bind(classOf[AppConfig])

    bind(classOf[PlaySessionStore]).toInstance {
      // session is already encrypted, no need to do it again
      val noopDataEncrypter = new DataEncrypter {
        override def decrypt(encryptedBytes: Array[Byte]): Array[Byte] = encryptedBytes

        override def encrypt(rawBytes: Array[Byte]): Array[Byte] = rawBytes
      }
      new PlayCookieSessionStore(noopDataEncrypter)
    }

    bind(classOf[Pac4JConfig]).toInstance {
      val clients = new Clients(frontendBaseUrl + "/customer-insight-platform/oidc-callback", provideOidcClient)
      new Pac4JConfig(clients)
    }

    bind(classOf[SecurityComponents]).to(classOf[DefaultSecurityComponents])
  }

  @Provides
  def provideOidcClient: OidcClient[OidcConfiguration] = {
    val oidcConfiguration = new OidcConfiguration()
    oidcConfiguration.setClientId(playConfig.get[String]("cip-auth.client-id"))
    oidcConfiguration.setSecret(playConfig.get[String]("cip-auth.client-secret"))
    oidcConfiguration.setWithState(false)
    oidcConfiguration.setDisablePkce(true)
    oidcConfiguration.setDiscoveryURI(playConfig.get[String]("cip-auth.discovery-uri"))
    oidcConfiguration.setClientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
    new OidcClient(oidcConfiguration)
  }
}
