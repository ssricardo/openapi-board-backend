<!-- # oaboard-backend-app -->
# Backend for oaBoard

## Running

This app uses Gradle as build tools, therefore to run it, use Gradle's tasks.

* You don't need to download gradle, just use the wrapper in this project.

> ./gradlew build


### Specific Tasks

The build has a customization to let us use either H2 or MySql version

Running with h2

> ./gradlew bootRunH2

Build for H2

> ./gradlew buildH2

Running with MySQL

> ./gradlew bootRunMySQL

Build for MySQL

> ./gradlew buildProduction

**Port**:  

Running it locally, the port used is: *8080* (with empty context)

After building for production, some environment variables need to be mapped for the DB configuration. Check it bellow

### Building the Docker image

Docker image may be prepared using gradle as well.  

Run one of the tasks:

* buildH2
    * For H2 version
* buildProduction
    * For MySQL version

## Endpoints

The API definitions is available on: `/manager/describe`.    
On the UI, there's a special parameter to access it: `{host}/swagger/o/b?self-describe=true`.  

> Abstract below

### Manager endpoints

Used for oaBoard frontend app

> /test [Use it initaly to checking]
> /manager [Main endpoint - multiple paths]  

> For the other ones, check the *Resource classes

### Agent

Used to feed this app.  As a REST endpoint, it may be called regardless of programming language:  

> /agent [PUT]

This endpoint requires 2 extra parts on the path:

- [namespace]/[name]

These are strings indicates the names of a given namespace and the app name.

#### Responses

    * 200 - OK
    * 400 - Bad request: Usually indicates that there are problems with the arguments (wrong types, missing, order...)
    * 409 - Conflict: Some validation has failed. For example, an app is in a given state that shouldn't be changed
 
## Security
        
There are 3 default roles, which are needed:

* READER
    * Needed to use the frontend application (reading operations)
* MANAGER
    * Needed to make changes using the frontend application and manage subscriptions
* AGENT
    * Needed to push new app definitions, usually to be done by a client 
    
> Further roles support TBD

## Architecture

This backend application is based on Kotlin over JVM, and uses mostly Java Web standards/frameworks.   
The app itself is not big, so it's kept in a single module.  

**Components:**  

 * The base language is Kotlin, over the JVM  
    (it is compiled into Java bytecode; therefore it runs on the JVM and most Java Frameworks/libraries can be used)
   
 * Spring Boot App
    * Spring JPA 
    * Spring Security    
 * Jax-RS and OpenAPI/Swagger
 * JWT Token for logged users 

**Database**:  

 It uses standard SQL. 
 It's ready to run/ be built with either H2 or MySQL.  
 
> Its model probably also fits well in a NoSQL DB. A SQL was chosen as it was quicker to start with and it fits better JPA 

## Kubernetes config

On the kube config, an additional Spring configuration is mapped. The following configuration is required:  

        env:
          server-address: http://this server reference
          mail-notification-enabled: true
          main-namespace: Production
        
        jwt:
          private:
            key: someSecretKeyHere 
    
        # Optional: 
        spring:
          mail:
            host: localhost
            port: 1025
            username: oab_server
            password: superSecret
            protcol: smtp
            properties:
        #      - name: mail.smtp.auth
        #        value: true
              - name: mail.smtp.starttls
                value: true
    
Besides that, any standard Spring configuration may also be used.   
    
In addition, the following configs are required as environment variables:   

* db_url
* db_username
* db_password

