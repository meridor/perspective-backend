# Development Specific Stuff

## API
### Project
* GET /projects - list projects [+]

### Instances (/instances)
* GET /?query - list project instance [+]
* POST / - launch instance [+]
* GET /$id - show instance information [+]
* GET /$id/console - redirect to instance console
* GET /$id/log - return instance log
* POST /delete?query - delete (terminate) instances [+]
* PUT /rebuild?query - rebuild instance
* PUT /instance/reboot?query - reboot instance [+]
* PUT /instance/hard-reboot?query - hard reboot instance [+]
* PUT /instance/resize?query - resize instance
* PUT /instance/snapshot?query - snapshot instance
* PUT /instance/pause?query - pause instance
* PUT /instance/resume?query - resume instance
* PUT /instance/suspend?query - suspend instance
* PUT /instance/shutdown?query - shutdown instance
* POST /instance/tag?query - add tags to instance
* DELETE /instance/tag?query - remove instance tags
* PUT /instance/lock?query - lock instance
* PUT /instance/unlock?query - unlock instance
* PUT /instance/ip?query - add ip to instance
* DELETE /instance/ip?query - remove instance ip
 
### Images
* GET /image/list?query - list project images
* GET /image/$id - show image info
* PUT /image?query - modify images
* DELETE /image?query - delete images


## Shell

~/.perspective/rc (/etc/perspective/shell/rc) file support [+]

### Generic show commands
* show projects [+]
* show networks [+]
* show instances [+]
* show instance (показать детали)
* show images [+]
* show image (показать детали)
* show flavors [+]
* show networks [+]

### Add commands
* add instances [+]
* add images (включает в себя snapshot и upload) [+]

### Delete commands
* delete instances [+]
* delete images [+]

### Instances commands
* reboot instances (--hard) [+]
* rebuild instances
* resize instances
* migrate instances
* pause instances
* resume instances
* lock instances
* unlock instances
* shutdown instances
* start instances (включить из состояния shutdown или suspended)
* suspend instances

### Set commands
Filters - e.g. ```set project test-project```
Settings - e.g. ```set log_file /var/log/shell.log```
* set key1 = value1, value2, value3; key2 = value3, value4 [+]
* unset key1 ; key2 [+]
* show settings [+]
* show filters [+]

### Groups commands
E.g. error, active, running and stopped instances.
* define group where <predicate> 
* show group <name>
* show groups

### Triggers
Показывает предупреждение или выполняет действие, если выполняется какое-то условие (например, больше 3 инстансов в состоянии Error).
* define trigger where
* show trigger <name>
* show triggers

### Views
Показывает таблицу, соответствующую определенному SQL запросу.
* define view where
* show view <name>
* show views

### SQL statements
* select ... [+]
* insert ...
* delete ...
