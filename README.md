# Perspective Backend

## API
### Project
GET /projects - list projects with enabled regions
GET /projects/$id/access - return endpoint urls for project services available
GET /projects/$id/regions/$id/rcfile - get Openstack RC file
GET /projects/$id/flavors - list project flavors
GET /projects/$id/networks - list project networks
GET /projects/$id/regions/$id/availability_zones - list project availability zones
GET /keypairs - list current user keypairs
POST /keypairs - add keypair
DELETE /keypairs/$id - delete keypair

### Instances
GET /projects/$id/regions/$id/instances - list project instances
POST /projects/$id/regions/$id/instances - launch instances
GET /projects/$id/regions/$id/instances/$id - show instance information
GET /projects/$id/regions/$id/instances/$id/console - redirect to instance console
GET /projects/$id/regions/$id/instances/$id/log - return instance log
DELETE /projects/$id/regions/$id/instances - terminate instances
PUT /projects/$id/regions/$id/instances/rebuild - rebuild instances
PUT /projects/$id/regions/$id/instances/reboot - reboot instances
PUT /projects/$id/regions/$id/instances/resize - resize instances
PUT /projects/$id/regions/$id/instances/snapshot - snapshot instances
PUT /projects/$id/regions/$id/instances/pause - pause instances
PUT /projects/$id/regions/$id/instances/resume - resume instances
PUT /projects/$id/regions/$id/instances/suspend - suspend instances
PUT /projects/$id/regions/$id/instances/shutdown - shutdown instances
POST /projects/$id/regions/$id/instances/tag - add tags to instances
DELETE /projects/$id/regions/$id/instances/tag - remove instance tags
PUT /projects/$id/regions/$id/instances/lock - lock instances
PUT /projects/$id/regions/$id/instances/unlock - unlock instances
PUT /projects/$id/regions/$id/instances/ip - add ip to instance
DELETE /projects/$id/regions/$id/instances/ip - remove instance ip

### Images
GET /projects/$id/images - list project images
GET /projects/$id/images/$id - show image info
PUT /projects/$id/images - modify images
DELETE /projects/$id/images - delete images
    