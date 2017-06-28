#!/bin/bash

GRAFANA_USER="admin"
GRAFANA_PASSWORD="admin"

USER_ID=""
ORGANIZATION_ID="389"
PLATFORM="ios"

for DASHBOARD_FILE in resources/dashboards/$PLATFORM/*.json
do
    DASHBOARD=`cat $DASHBOARD_FILE`
    printf "\n\nUploading dashboard $DASHBOARD_FILE...\n"
    curl -vvv https://$GRAFANA_USER:$GRAFANA_PASSWORD@dashboards.flowup.io/api/dashboards/db\
         -H "Accept: application/json"\
         -H "Content-Type: application/json"\
         --data "{\"overwrite\": true, \"orgId\": $ORGANIZATION_ID, \"dashboard\": $DASHBOARD}"
done

HOME_DASHBOARD_ID=`curl -s https://$GRAFANA_USER:$GRAFANA_PASSWORD@dashboards.flowup.io/api/search?limit=100\&query=\
     -H "Accept: application/json"\
     -H "Content-Type: application/json" | 
     jq '.[] | {slug: .uri, id: .id} | if .slug == "db/home" then .id else 0 end' |
     grep -v '0'`
if [ ! -z "$HOME_DASHBOARD_ID" ] 
then
    printf "\nUpdating the home dashboard to $HOME_DASHBOARD_ID..."

    USER_IDS=`curl -s https://$GRAFANA_USER:$GRAFANA_PASSWORD@dashboards.flowup.io/api/orgs/$ORGANIZATION_ID/users\
        -H "Accept: application/json"\
        -H "Content-Type: application/json" | 
        jq '.[] | .userId'`
    
    for USER_ID in $USER_IDS
    do
        echo "USER $USER_ID"
    done

    # curl -vvv https://$GRAFANA_USER:$GRAFANA_PASSWORD@dashboards.flowup.io/api/user/preferences\
    #      -X PUT\
    #      -H "Accept: application/json"\
    #      -H "Content-Type: application/json"\
    #      --data "{\"userId\": $USER_ID, \"orgId\": $ORGANIZATION_ID, \"homeDashboardId\": $HOME_DASHBOARD_ID}"
else
    printf "\nNo home dashboard found"
fi
