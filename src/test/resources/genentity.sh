#!/bin/sh
#./genentity.sh {service_name} {prop_acls} {outputpath} {secretkey}
current=$(cd $(dirname $0);pwd)

rm -rf $1
rm src/main/webapps/WEB-INF/lib/entity.jar

java -cp $current/src/main/webapps/WEB-INF/lib/reflexcore-2.1.2.jar:$current/target/test-classes:$current/src/main/resources:$current/src/main/webapps/WEB-INF/lib/reflexmapper-2.1.2.jar:$current/src/main/webapps/WEB-INF/lib/xstream-1.1.2.jar:$current/src/main/webapps/WEB-INF/lib/xpp3-1.1.3.4-RC8.jar:$current/src/main/webapps/WEB-INF/lib/json-20070829.jar:$current/src/main/webapps/WEB-INF/lib/javassist-3.18.0-GA.jar:$current/src/main/webapps/WEB-INF/lib/msgpack-0.6.8.jar jp.reflexworks.atom.util.GenerateEntity $2 $3 $4

jar cvf src/main/webapps/WEB-INF/lib/entity.jar $1 
