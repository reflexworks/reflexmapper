#!/bin/sh
#./genentity.sh {service_name} {serviceinit.xml|templatefilename} {outputpath} {secretkey} {prop_acls} (tempfile)
current=$(cd $(dirname $0);pwd)

rm -rf _$1
rm src/main/webapps/WEB-INF/lib/entity.jar

java -cp $current/src/main/webapps/WEB-INF/lib/reflexcore-2.1.3.jar:$current/target/test-classes:$current/src/main/resources:$current/src/main/webapps/WEB-INF/lib/reflexmapper-2.1.3.jar:$current/src/main/webapps/WEB-INF/lib/xstream-1.1.2.jar:$current/src/main/webapps/WEB-INF/lib/xpp3-1.1.3.4-RC8.jar:$current/src/main/webapps/WEB-INF/lib/json-20070829.jar:$current/src/main/webapps/WEB-INF/lib/javassist-3.18.1-GA.jar:$current/src/main/webapps/WEB-INF/lib/msgpack-0.6.11.jar jp.reflexworks.atom.util.GenerateEntity $1 $2 $3 $4 $5

jar cvf src/main/webapps/WEB-INF/lib/entity.jar _$1 
