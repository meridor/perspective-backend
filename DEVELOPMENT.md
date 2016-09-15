# Development Specific Stuff

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
