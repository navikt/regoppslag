#!/usr/bin/env sh

if test -f /var/run/secrets/nais.io/srvregoppslag/username;
then
    echo "Setting REGOPPSLAG_SERVICEUSER_USERNAME"
    export REGOPPSLAG_SERVICEUSER_USERNAME=$(cat /var/run/secrets/nais.io/srvregoppslag/username)
fi
if test -f /var/run/secrets/nais.io/srvregoppslag/password;
then
    echo "Setting REGOPPSLAG_SERVICEUSER_PASSWORD"
    export REGOPPSLAG_SERVICEUSER_PASSWORD=$(cat /var/run/secrets/nais.io/srvregoppslag/password)
fi
