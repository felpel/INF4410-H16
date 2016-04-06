#!/bin/bash

source ./INF4410-20-projet-openrc.sh
nova floating-ip-create ext-net
nova floating-ip-list
#$1 = INSTANCE_ID & $2 = FLOATING_IP
nova add-floating-ip $1 $2 