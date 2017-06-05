
unzip -o app/build/libs/loans-monitoring-app-1.0.1-SNAPSHOT.jar -d app/build/libs/war
cp support/logback.xml app/build/libs/war/WEB-INF/classes/logback.xml

cd app/build/libs/war

java -Dspring.profiles.active=dev -Dfile.encoding=UTF-8 -Dserver.port=8123 \
org.springframework.boot.loader.WarLauncher

cd ../../../..