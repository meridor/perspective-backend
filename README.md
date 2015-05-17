# Perspective Backend

## API
### Project
* GET /cloud/$id/projects/list - list projects with enabled regions [+]

// These urls probably aren't needed!
* GET /projects/$id/access - return endpoint urls for project services available
* GET /projects/$id/regions/$id/rcfile - get Openstack RC file
* GET /keypairs - list current user keypairs
* POST /keypairs - add keypair
* DELETE /keypairs/$id - delete keypair

### Instances
* GET /project/$id/region/$id/instance/list - list project instance [+]
* POST /project/$id/region/$id/instance - launch instance
* GET /project/$id/region/$id/instance/$id - show instance information [+]
* GET /project/$id/region/$id/instance/$id/console - redirect to instance console
* GET /project/$id/region/$id/instance/$id/log - return instance log
* DELETE /project/$id/region/$id/instance - terminate instance
* PUT /project/$id/region/$id/instance/rebuild - rebuild instance
* PUT /project/$id/region/$id/instance/reboot - reboot instance
* PUT /project/$id/region/$id/instance/resize - resize instance
* PUT /project/$id/region/$id/instance/snapshot - snapshot instance
* PUT /project/$id/region/$id/instance/pause - pause instance
* PUT /project/$id/region/$id/instance/resume - resume instance
* PUT /project/$id/region/$id/instance/suspend - suspend instance
* PUT /project/$id/region/$id/instance/shutdown - shutdown instance
* POST /project/$id/region/$id/instance/tag - add tags to instance
* DELETE /project/$id/region/$id/instance/tag - remove instance tags
* PUT /project/$id/region/$id/instance/lock - lock instance
* PUT /project/$id/region/$id/instance/unlock - unlock instance
* PUT /project/$id/region/$id/instance/ip - add ip to instance
* DELETE /project/$id/region/$id/instance/ip - remove instance ip
 
### Images
* GET /project/$id/region/$id/image/list - list project images
* GET /project/$id/region/$id/image/$id - show image info
* PUT /project/$id/region/$id/image - modify images
* DELETE /project/$id/region/$id/image - delete images

# Преимущества Perspective по сравнению со стандартным Openstack API

* Очень быстрое асинхронное API (за счет кеширования в память определенной информации наподобие списка флаворов, сетей и т.п.)
* Поддержка bulk для всех типов операций (выполнение одной операции для нескольких сущностей: инстансов, контейнеров и т.п.)
* Поддержка Swagger (возможность нагенерировать клиента для любого языка программирования)
* В отдаленном будущем - поддержка других облачных платформ или нескольких облачных платформ как отдельных проектов в UI
* Более простая аутентификация и авторизация (поддержка OAuth, LDAP и т.п.), по-умолчанию не нужно получать токен для каждой операции (это делается прозрачно для пользователя)
* Более удобный клиентский UI (standalone client-side application)