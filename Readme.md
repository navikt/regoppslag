Regoppslag
================

## Swagger dokumentasjon
Beskrivelsen av grensesnittet finner du på endepunktet `/swagger-ui.html` 

## Bygge app.jar og kjøre tester

`mvn clean package`

## Kjøre systemtester

Denne applikasjonen har ingen automatiske systemtester

## Hvordan kjøre lokalt med mvn spring-boot plugin

Det ligger profil for t8 i `regoppslag/src/main/resources`:

* application-t8.properties

Noen secrets må settes - se https://fasit.adeo.no/instances/4423689

```

# Systembruker
export SRVREGOPPSLAG_USERNAME=srvregoppslag
export SRVREGOPPSLAG_PASSWORD=<pw>

# System cert
export SRVREGOPPSLAG_CERT_KEYSTORE=<keystorepath>
export SRVREGOPPSLAG_CERT_KEYSTOREALIAS=<keystorealias>   # Default app-key
export SRVREGOPPSLAG_CERT_PASSWORD=<certpw>
```

Kjøre appen med mvn spring boot plugin. Truststore finnes på Fasit som `nav_truststore` alias. 

```
mvn spring-boot:run -Drun.profiles=t8 -Drun.jvmArguments="-Dsrvregoppslag_cert_keystore=/path/til/cert.jks -Dsrvregoppslag_cert_password=<certpw> -Djavax.net.ssl.trustStore=/path/til/truststore.jks -Djavax.net.ssl.trustStorePassword=<truststorepw>"
```
## Cache ved lokal kjøring

Denne applikasjonen bruker Redis cache som er avhengig av en ekstern cache server som den kan koble seg til. 
Når applikasjonen kjøres lokalt vil det istedenfor settes opp cache som kjører lokalt på applikasjonen. Konfigurasjon av denne cachen ligger i `LokalCacheConfig` klassen og vil bare kjøres når Activeprofiles settes `local`.

## Hvordan kjøre lokalt med IntelliJ

Start `Application.java` som en Spring Boot/Java Application. På denne måten kan man kjøre lokalt og få full debug-støtte. 

Skriv inn passordet for `srvregoppslag` servicebrukene i `serviceuser.password` i application-t8.properties filen. 

VM Options: `-Dsrvregoppslag_cert_keystore=/path/til/cert.jks -Dsrvregoppslag_cert_password=<certpw> -Djavax.net.ssl.trustStore=/path/til/truststore.jks -Djavax.net.ssl.trustStorePassword=<truststorepw>`

Active profiles: `t8`.

---

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan rettes mot:

* Applikasjonsansvarlig Paul Magne Lunde <Paul.Magne.Lunde@nav.no> 

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #dokumenthåndtering.
