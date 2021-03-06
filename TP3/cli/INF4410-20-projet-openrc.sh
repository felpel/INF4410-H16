
#!/bin/bash

echo "[OS] Setup user information..."

# To use an Openstack cloud you need to authenticate against keystone, which
# returns a **Token** and **Service Catalog**.  The catalog contains the
# endpoint for all services the user/tenant has access to - including nova,
# glance, keystone, swift.
#
# *NOTE*: Using the 2.0 *auth api* does not mean that compute api is 2.0.  We
# will use the 1.1 *compute api*
export OS_AUTH_URL=http://pingouin.info.polymtl.ca:5000/v2.0

# With the addition of Keystone we have standardized on the term **tenant**
# as the entity that owns the resources.
export OS_TENANT_ID=eb73b57bfd874da680c013f04d1a3b25
export OS_TENANT_NAME="INF4410-20-projet"

# In addition to the owning entity (tenant), openstack stores the entity
# performing the action as the **user**.
export OS_USERNAME="INF4410-20"

# With Keystone you pass the keystone password.
#echo "Please enter your OpenStack Password: "
#read -sr OS_PASSWORD_INPUT
# On assigne le mot de passe ici pour eviter de devoir le rentrer a chaque fois.
export OS_PASSWORD="ebM2859s8"

# If your configuration has multiple regions, we set that information here.
# OS_REGION_NAME is optional and only valid in certain environments.
export OS_REGION_NAME="regionOne"
# Don't leave a blank variable, unset it if it was empty
if [ -z "$OS_REGION_NAME" ]; then unset OS_REGION_NAME; fi

echo "[OS] Setup completed!"