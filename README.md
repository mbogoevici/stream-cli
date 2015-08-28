# What is this?

This a POC for a Spring Boot CLI based module launcher. 

How does it work?

Have Spring Boot CLI installed, and then:

    mvn install 
    
    spring install org.springframework.cloud.stream:stream-cli:1.0.0-SNAPSHOT
    
    spring run-module org.springframework.cloud.stream.module:time-source:1.0.0.BUILD-SNAPSHOT
    
Currently, passing properties is not supported.
