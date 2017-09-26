#!/bin/bash
set -eu

CONSUMER_TOKEN=${CONSUMER_TOKEN:-"some-default-token"}
CONSUMER_SECRET=${CONSUMER_SECRET:-"some-default-secret"}

export LC_COLLATE=C

urlencode() {
    # urlencode <string>
    old_lc_collate=$LC_COLLATE
    LC_COLLATE=C
    local length="${#1}"
    for (( i = 0; i < length; i++ )); do
        local c="${1:i:1}"
        case $c in
            [a-zA-Z0-9.~_-]) printf "$c" ;;
            *) printf '%%%02X' "'$c" ;;
        esac
    done
    LC_COLLATE=$old_lc_collate
}

urldecode() {
    # urldecode <string>
    local url_encoded="${1//+/ }"
    printf '%b' "${url_encoded//%/\\x}"
}

auth=$(printf "%s:%s" $(urlencode "$CONSUMER_TOKEN") $(urlencode "$CONSUMER_SECRET") | base64)

response=$(curl -s -X POST \
    https://api.twitter.com/oauth2/token \
    -H "Authorization: Basic $auth" \
    -H "cache-control: no-cache" \
    -H "content-type: application/x-www-form-urlencoded;charset=UTF-8" \
    --data "grant_type=client_credentials")

token_type=$(echo $response | jq -r '.token_type')

if [ "$token_type" != "bearer" ]; then
    echo "Invalid token_type: $token_type, expecting bearer"
    exit 1
fi

bearer_token=$(echo $response | jq -r '.access_token')

printf "Bearer %s" $bearer_token
