
@echo off
set LIB=.\lib
set PHOENIX_HOME=..\jakarta-avalon-phoenix
set ENTERPRISE_HOME=..\jakarta-avalon-apps\enterprise
set PSS_HOME=%ENTERPRISE_HOME%\pss
set ORB_HOME=%ENTERPRISE_HOME%\orb
set OTS_HOME=%ENTERPRISE_HOME%\ots
set TIME_HOME=%ENTERPRISE_HOME%\time
set PROP_HOME=properties
set DOMAIN_HOME=domain
set SPS_HOME=sps
set REALM_HOME=realm
set VAULT_HOME=vault

set DEMO_JAR=.\dist\examples.jar
set SESSION_JAR=.\dist\session.jar
set FINDER_JAR=.\dist\adapter.jar
set PSS_JAR=%PSS_HOME%\dist\pss-2.0.1.jar
set OTS_JAR=%OTS_HOME%\lib\openorb_ots-1.2.1.jar
set ORB_BASE_JAR=%ORB_HOME%\lib\openorb-1.3.0.jar
set ORB_JAR=%ORB_HOME%\dist\orb-manager-2.0.1.jar
set PROP_JAR=.\dist\properties.jar
set DOMAIN_JAR=.\dist\domain.jar
set SPS_JAR=.\dist\sps.jar
set REALM_JAR=.\dist\realm.jar
set TIME_JAR=%TIME_HOME%\dist\time-2.0.0.jar
set VAULT_JAR=.\dist\vault.jar
set PHOENIX_JAR=%PHOENIX_HOME%\build\lib\phoenix-client.jar

@echo on
java -jar %LIB%\merlin.jar %PHOENIX_JAR% %ORB_BASE_JAR% %ORB_JAR% %OTS_JAR% %PSS_JAR% %FINDER_JAR% %DOMAIN_JAR% %TIME_JAR% %PROP_JAR% %SPS_JAR% %REALM_JAR% %VAULT_JAR% %SESSION_JAR% %DEMO_JAR% -target net.osm.session.HomeProvider -verbose false -priority DEBUG -disposal false -configuration server.xml