
cd F:\dev\gateway
jdb -Xincgc -classpath boot\avalonapi.jar;boot\phoenix-client.jar;boot\phoenix-engine.jar;boot\phoenix-loader.jar;boot\xerces-1.3.0.jar -Davalon.home=. org.apache.phoenix.engine.loader.PhoenixLoader -a .\ext