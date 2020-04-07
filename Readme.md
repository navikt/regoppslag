Regoppslag
================

## Swagger dokumentasjon
Beskrivelsen av grensesnittet finner du på endepunktet `/swagger-ui.html` 

## Bygge app.jar og kjøre tester

`mvn clean package`

## Kjøre systemtester

Denne applikasjonen har ingen automatiske systemtester

## Hvordan kjøre lokalt med IntelliJ

Start `Application.java` som en Spring Boot/Java Application. På denne måten kan man kjøre lokalt og få full debug-støtte. 

Active profiles: `t`.

Det ligger profil for t i `regoppslag/src/main/resources` hvor url for alle endepunkter ligger som applikasjonen henter opp ved oppstart:

* application-t.properties

Noen secrets må settes i VM Options

```

# Systembruker
-Dserviceuser.username=srvregoppslag
-Dserviceuser.password=<pw>

# System cert
-Djavax.net.ssl.trustStore=<nav_truststore_nonproduction_ny2.jts path>
-Djavax.net.ssl.trustStorePassword=<pw>

# Ldap
-Dldap_password=<psw>

-Dnamespace=tx
```

Miljø som det ønskes å kjøre mot kan settes med `-Dnamespace=q2` feks for q2
## Cache ved lokal kjøring

Denne applikasjonen bruker Redis cache som er avhengig av en ekstern cache server som den kan koble seg til. 
Når applikasjonen kjøres lokalt vil det istedenfor settes opp cache som kjører lokalt på applikasjonen. Konfigurasjon av denne cachen ligger i `LokalCacheConfig` klassen og vil bare kjøres når Activeprofiles settes `local`.

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan rettes mot:

* Applikasjonsansvarlig Paul Magne Lunde <Paul.Magne.Lunde@nav.no> 

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #team_dokument.
