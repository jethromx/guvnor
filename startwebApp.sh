#!/bin/sh

apacheURL=https://downloads.apache.org/tomcat/tomcat-8/v8.5.57/bin/apache-tomcat-8.5.57.tar.gz
installDir=apache-tomcat
apacheTomcatVersion=apache-tomcat-8.5.57.tar.gz

startServer(){
    echo -e "Iniciando server"
    sh $installDir/bin/catalina.sh run
}




if [ -d "$installDir" ];
then
    startServer
else
    echo -e "Descargando Apache tomcat"
    wget -c $apacheURL 

    echo -e "Unzip Apache"
    tar -xvf $apacheTomcatVersion --one-top-level=$installDir --strip-components 1

    echo -e "Copiando war guvnor"
    cp guvnor-5.5.0.Final.war $installDir/webapps/

    echo -e "Copiando repository"
#    cp -R repository $installDir

    echo -e "Borrando ficheros temporales"
    rm -rf $apacheTomcatVersion

    startServer
fi



