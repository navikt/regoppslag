#!/usr/bin/env sh

if test -f /var/run/secrets/nais.io/srvregoppslag/username;
then
    echo "Setting SERVICEUSER_USERNAME"
    export SERVICEUSER_USERNAME=$(cat /var/run/secrets/nais.io/srvregoppslag/username)
fi
if test -f /var/run/secrets/nais.io/srvregoppslag/password;
then
    echo "Setting SERVICEUSER_PASSWORD"
    export SERVICEUSER_PASSWORD=$(cat /var/run/secrets/nais.io/srvregoppslag/password)
fi
if test -f /var/run/secrets/nais.io/ldap/username;
then
    echo "Setting LDAP_USERNAME"
    export LDAP_USERNAME=$(cat /var/run/secrets/nais.io/ldap/username)
fi
if test -f /var/run/secrets/nais.io/ldap/password;
then
    echo "Setting LDAP_PASSWORD"
    export LDAP_PASSWORD=$(cat /var/run/secrets/nais.io/ldap/password)
fi
