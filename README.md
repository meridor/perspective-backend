# Perspective Backend

## API
### Project
* GET /projects - list projects with enabled regions
* GET /projects/$id/access - return endpoint urls for project services available
* GET /projects/$id/regions/$id/rcfile - get Openstack RC file
* GET /projects/$id/flavors - list project flavors -> move to projects list
* GET /projects/$id/networks - list project networks -> move to projects list
* GET /projects/$id/regions/$id/availability_zones - list project availability zones
* GET /keypairs - list current user keypairs
* POST /keypairs - add keypair
* DELETE /keypairs/$id - delete keypair

### Instances
* GET /projects/$id/regions/$id/instances - list project instances
* POST /projects/$id/regions/$id/instances - launch instances
* GET /projects/$id/regions/$id/instances/$id - show instance information
* GET /projects/$id/regions/$id/instances/$id/console - redirect to instance console
* GET /projects/$id/regions/$id/instances/$id/log - return instance log
* DELETE /projects/$id/regions/$id/instances - terminate instances
* PUT /projects/$id/regions/$id/instances/rebuild - rebuild instances
* PUT /projects/$id/regions/$id/instances/reboot - reboot instances
* PUT /projects/$id/regions/$id/instances/resize - resize instances
* PUT /projects/$id/regions/$id/instances/snapshot - snapshot instances
* PUT /projects/$id/regions/$id/instances/pause - pause instances
* PUT /projects/$id/regions/$id/instances/resume - resume instances
* PUT /projects/$id/regions/$id/instances/suspend - suspend instances
* PUT /projects/$id/regions/$id/instances/shutdown - shutdown instances
* POST /projects/$id/regions/$id/instances/tag - add tags to instances
* DELETE /projects/$id/regions/$id/instances/tag - remove instance tags
* PUT /projects/$id/regions/$id/instances/lock - lock instances
* PUT /projects/$id/regions/$id/instances/unlock - unlock instances
* PUT /projects/$id/regions/$id/instances/ip - add ip to instance
* DELETE /projects/$id/regions/$id/instances/ip - remove instance ip
 
### Images
* GET /projects/$id/images - list project images
* GET /projects/$id/images/$id - show image info
* PUT /projects/$id/images - modify images
* DELETE /projects/$id/images - delete images

# Преимущества Perspective по сравнению со стандартным Openstack API

* Очень быстрое асинхронное API (за счет кеширования в память определенной информации наподобие списка флаворов, сетей и т.п.)
* Поддержка bulk для всех типов операций (выполнение одной операции для нескольких сущностей: инстансов, контейнеров и т.п.)
* Поддержка Swagger (возможность нагенерировать клиента для любого языка программирования)
* В отдаленном будущем - поддержка других облачных платформ или нескольких облачных платформ как отдельных проектов в UI
* Более простая аутентификация и авторизация (поддержка OAuth, LDAP и т.п.), по-умолчанию не нужно получать токен для каждой операции (это делается прозрачно для пользователя)
* Более удобный клиентский UI (standalone client-side application)

# TODO
1) Как сделать обновление состояния по таймеру? (@OnTimer)