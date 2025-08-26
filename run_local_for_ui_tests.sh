#!/bin/bash

sm2 --stop-all

sm2 --start BANK_ACCOUNT_REPUTATION_FRONTEND_SERVICES --appendArgs '{
  "BANK_ACCOUNT_REPUTATION": [
    "-Dplay.http.router=testOnlyDoNotUseInAppConf.Routes",
    "-Dmicroservice.services.modulr.protocol=http",
    "-Dmicroservice.services.modulr.host=localhost",
    "-Dmicroservice.services.modulr.port=6001",
    "-Dmicroservice.services.modulr.enabled=true",
    "-Dmicroservice.services.modulr.business.cache.enabled=false",
    "-Dmicroservice.services.modulr.personal.cache.enabled=false",
    "-Dauditing.consumer.baseUri.port=6001",
    "-Dauditing.consumer.baseUri.host=localhost",
    "-Dauditing.enabled=true",
    "-Dproxy.proxyRequiredForThisEnvironment=false",
    "-Dmicroservice.services.eiscd.aws.endpoint=http://localhost:4566",
    "-Dmicroservice.services.eiscd.aws.bucket=txm-dev-bacs-eiscd",
    "-Dmicroservice.services.eiscd.cache-schedule.initial-delay=86400",
    "-Dmicroservice.services.modcheck.cache-schedule.initial-delay=86400",
    "-Dmicroservice.services.thirdPartyCache.endpoint=http://localhost:9899/cache",
    "-Dmicroservice.services.access-control.endpoint.verify.enabled=true",
    "-Dmicroservice.services.access-control.endpoint.verify.allow-list.0=bars-acceptance-tests",
    "-Dmicroservice.services.access-control.endpoint.verify.allow-list.1=some-upstream-service",
    "-Dmicroservice.services.access-control.endpoint.verify.allow-list.2=bank-account-reputation-frontend",
    "-Dmicroservice.services.access-control.endpoint.verify.allow-list.3=bank-account-verification-frontend",
    "-Dmicroservice.services.access-control.endpoint.validate.enabled=true",
    "-Dmicroservice.services.access-control.endpoint.validate.allow-list.0=bars-acceptance-tests",
    "-Dmicroservice.services.access-control.endpoint.validate.allow-list.1=some-upstream-service",
    "-Dmicroservice.services.access-control.endpoint.validate.allow-list.2=bank-account-reputation-frontend",
    "-Dmicroservice.services.access-control.endpoint.validate.allow-list.3=bank-account-verification-frontend",
    "-Dmicroservice.services.modcheck.useLocal=true"
  ],
  "BANK_ACCOUNT_REPUTATION_THIRD_PARTY_CACHE": [
    "-Dcontrollers.confidenceLevel.uk.gov.hmrc.bankaccountreputationthirdpartycache.controllers.CacheController.needsLogging=true"
  ],
  "BANK_ACCOUNT_VERIFICATION_FRONTEND": [
    "-Dmicroservice.hosts.allowList.1=localhost",
    "-Dauditing.consumer.baseUri.port=6001",
    "-Dauditing.consumer.baseUri.host=localhost",
    "-Dauditing.enabled=true",
    "-Dmicroservice.services.access-control.enabled=true",
    "-Dmicroservice.services.access-control.allow-list.0=bavfe-acceptance-tests"
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
 -Dauditing.consumer.baseUri.port=6001 \
 -Dauditing.consumer.baseUri.host=localhost \
 "