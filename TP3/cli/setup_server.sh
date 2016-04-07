
#!/bin/bash

# Identifier l'usager
source ./INF4410-20-projet-openrc.sh

# Creer l'IP flottant et liste les IP flottants du stack
nova floating-ip-create ext-net 
nova floating-ip-list

echo "Entrez l'identifiant de l'instance, suivi de [ENTER]"
read instanceId
echo "Entrer l'adresse IP flottante nouvellement créée, suivi de [ENTER]"
read floatingIp

#Associe l'IP flottant a l'instance
nova add-floating-ip $instanceId $floatingIp

echo "Entrez le nom complet du fichier .pem, suivi de [ENTER]"
read pemFile

distantHost="ubuntu@$floatingIp"

ssh -i $pemFile $distantHost 'bash -s' < ../srv/server-install.sh