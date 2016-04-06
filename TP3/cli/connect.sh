#!/bin/bash

if [ "$#" -ne 3 ] 
    then
        echo "3 arguments necessaires : besoin ou non de configurer nova, identifiant de l'instance (ex. WS), l'IP flottant de l'instance (ex. 132.207.12.240)";
fi;

if [ "$1" = true ] 
    then
        if [ ! -f "INF4410-20-projet-openrc.sh" ]; then
            echo "Fichier 'INF4410-20-projet-openrc.sh' manquant pour configurer nova"
            exit
        else
            source configure_nova.sh $2 $3
        fi   
fi;

pemFile="ws.pem"

if [ ! -f $pemFile ] 
    then
        echo "Fichier '$pemFile' manquant pour initialiser la connection SSH..."
        exit
fi;

fullHost="ubuntu@$3"
echo "Connection SSH a l'hote: $fullHost"
ssh -i $pemFile $fullHost