#!/bin/bash

GRAFANA_USER="admin"
GRAFANA_PASSWORD="admin"

PLATFORM="ios"

for ORGANIZATION_ID in {394..394}
do
    printf "\n[Migrating dashboards for organization with id $ORGANIZATION_ID]"
    for DASHBOARD_FILE in conf/resources/dashboards/$PLATFORM/*.json
    do
        DASHBOARD=`cat $DASHBOARD_FILE`
        printf "\nUploading dashboard $DASHBOARD_FILE...\n"
        curl -s https://$GRAFANA_USER:$GRAFANA_PASSWORD@dashboards.flowup.io/api/dashboards/db\
            -H "Accept: application/json"\
            -H "Content-Type: application/json"\
            --data "{\"overwrite\": true, \"orgId\": $ORGANIZATION_ID, \"dashboard\": $DASHBOARD}"
    done

    HOME_DASHBOARD_ID=`curl -s https://$GRAFANA_USER:$GRAFANA_PASSWORD@dashboards.flowup.io/api/search?limit=100\&query=\&orgId=$ORGANIZATION_ID\
        -H "Accept: application/json"\
        -H "Content-Type: application/json" | 
        jq '.[] | {slug: .uri, id: .id} | if .slug == "db/home" then .id else 0 end' |
        grep -v '^0$'`
    if [ ! -z "$HOME_DASHBOARD_ID" ] 
    then
        printf "\nUpdating home dashboard to the one with id $HOME_DASHBOARD_ID..."

        USER_IDS=`curl -s https://$GRAFANA_USER:$GRAFANA_PASSWORD@dashboards.flowup.io/api/orgs/$ORGANIZATION_ID/users\
            -H "Accept: application/json"\
            -H "Content-Type: application/json" | 
            jq '.[] | .userId'`
        
        for USER_ID in $USER_IDS
        do
            printf "\n\t> to user $USER_ID"
            curl -s https://$GRAFANA_USER:$GRAFANA_PASSWORD@dashboards.flowup.io/api/user/preferences\
                -X PUT\
                -H "Accept: application/json"\
                -H "Content-Type: application/json"\
                --data "{\"userId\": $USER_ID, \"orgId\": $ORGANIZATION_ID, \"homeDashboardId\": $HOME_DASHBOARD_ID}"
        done
    else
        printf "\nNo home dashboard found"
    fi

done