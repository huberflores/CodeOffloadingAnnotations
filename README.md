Android-based Code Offload Converter
====================================
The tool explores the classes of an Android project and transforms the annotated methods (``` @Cloud ```) into offloadable components that can be processed remotely by a virtualized Dalvik machine in the cloud. How to deploy AOSP/CyanogenMod Dalvik in a x86 server can be found [here](https://gist.github.com/huberflores/4687766)



Requirements
-------------

Maven : version >= 3.0.4

JDK : version >= 1.7


Build
------------
Download the source and navigate into the root directory. Execute the code below 

```xml
mvn clean install
````
Then move into the converter directory and run 

```xml
mvn org.apache.maven.plugins:maven-assembly-plugin:2.2-beta-2:assembly
````

Try Application 
------------
A file "converter-1.0-jar-with-dependencies" would be created in target folder of the converter directory.
Navigate into the directory via the command prompt and execute the following

```xml
java -jar converter-1.0-jar-with-dependencies.jar
````
