#!/bin/bash

sm2 --stop-all

sm2 --start BANK_ACCOUNT_REPUTATION_THIRD_PARTIES_STUB

sm2 --start BANK_ACCOUNT_REPUTATION_FRONTEND_SERVICES --appendArgs '{
  "BANK_ACCOUNT_REPUTATION": [
    "-Dplay.http.router=testOnlyDoNotUseInAppConf.Routes",
    "-Dfeatures.useDownstreamCOPStub=true",
    "-Dauditing.enabled=false",
    "-Dproxy.proxyRequiredForThisEnvironment=false",
    "-Dmicroservice.services.access-control.endpoint.verify.enabled=true",
    "-Dmicroservice.services.access-control.endpoint.verify.allow-list.0=bars-acceptance-tests",
    "-Dmicroservice.services.access-control.endpoint.verify.allow-list.1=some-upstream-service",
    "-Dmicroservice.services.access-control.endpoint.verify.allow-list.2=bank-account-reputation-frontend",
    "-Dmicroservice.services.access-control.endpoint.validate.enabled=true",
    "-Dmicroservice.services.access-control.endpoint.validate.allow-list.0=bars-acceptance-tests",
    "-Dmicroservice.services.access-control.endpoint.validate.allow-list.1=some-upstream-service",
    "-Dmicroservice.services.access-control.endpoint.validate.allow-list.2=bank-account-reputation-frontend",
    "-Dmicroservice.services.eiscd.useLocal=true",
    "-Dmicroservice.services.modcheck.useLocal=true"
  ],
  "BANK_ACCOUNT_REPUTATION_THIRD_PARTY_CACHE": [
    "-Dcontrollers.confidenceLevel.uk.gov.hmrc.bankaccountreputationthirdpartycache.controllers.CacheController.needsLogging=true"
  ],
  "BANK_ACCOUNT_REPUTATION_FRONTEND": [
    "-Dauditing.enabled=true",
    "-Dauditing.consumer.baseUri.port=6001",
    "-Dauditing.consumer.baseUri.host=localhost"
  ]
}'

## stop the service via service manager as we will start this up locally below
sm2 --stop BANK_ACCOUNT_REPUTATION_FRONTEND

sbt "run \
 -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes
 -Dauditing.enabled=false \
 "