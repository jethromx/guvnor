# Guvnor
Versión: **guvnor 5.5.0**
Versión apache: **8**
URL de acceso: [http://localhost:8080/guvnor/](http://localhost:8080/guvnor/o)
# Run Contenedor Docker
Iniciar guvnor dentro de un contenerdor docker

## Construir Imagen

    docker build -t jethro/guvnor:5.5.0 .

## Iniciar contenedor

    docker run --rm --name guvnor -d jethro/guvnor:5.5.0

### Entrar al contendor

    docker exec -it guvnor /bin/bash

### Logs

    docker logs guvnor --follow

## Modificar parámetros de java

    docker run --rm --name guvnor -e JAVA_OPTS="-Xms512m -Xmx700m -Xss1m" -d jethro/guvnor:5.5.0

# Run Web Application

Inciar guvnor usando Apache localmente, ejecutando el script se descargara apache tomcat(en caso de que no exista) e iniciara la aplicacion web

## Start Script

    sh startwebApp.sh


