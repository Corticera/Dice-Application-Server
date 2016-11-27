# Dice Application Server

## Developers' Guide.
This article is intended to help newcomers, who have little to no idea about what this project is meant to deliver, to get started. This section is divided into the following parts.

1. Project Structure.
2. Organization of code.
3. Package Structure.
4. Design Pattern.

### 1. Project Structure.
The build tool used by this project is Apache Maven.

The project is structured into the following directory tree:
	
	Dice
	`----src
	|    `----main
	|    |    `----java
	|    |    `----resources
	|    `----test
	|    |    `----java
	|    |    `----resources
	|    `----conf
	|    `----webcontent
	|    `----bin
	|    `----lib
	|    `----logs
	`----pom.xml
	
 A. **Dice**: The root directory. Everything goes in here. This directory contains the Project Object Model (pom.xml) file.
 
 B. **Dice/src**: Source code goes in here. This directory contains all the code written for the application server and test in their respective directory.
 
 C. **Dice/src/main**: This directory contains the main source code that the application server executes internally.
 
 D. **Dice/src/main/java**: Application Server's source code goes in here.
 
 E. **Dice/src/main/resources**: This directory contains any files to be included in the final JAR. 
 
 F. **Dice/src/test**: This directory contains source code of the test cases.
 
 G. **Dice/src/test/java**: Application Server's test source code goes in here.
 
 H. **Dice/src/test/resources**: This directory contains misc files to be included in the classpath.
 
 I. **Dice/conf**: This directory contains files that are used to configure the application server. *This is not included in the classpath*.
 
 J. **Dice/webcontent**: The default docbase for web applications.
 
 K. **Dice/bin**: The directory which contains the executable for startup/shutdown. This directory also contains the startup JAR.
 
 L. **Dice/lib**: All the dependencies of the application server goes in here. This directory is included in the classpath of bootstrap.