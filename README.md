<!-- # oaboard-backend-app -->
# Backend for  OpenAPI Board / Center

## Running

This app uses Gradle as build tools, therefore run it use Gradle's tasks.

*You don't need to download gradle, just use the wrapper in this project.

> ./gradlew build

### Specific Tasks

The build has a customization to let us use either H2 or MySql version

Running with h2

> ./gradlew bootRunTest

Build for H2

> ./gradlew buildTest

Running with MySQL

> ./gradlew bootRunMySQL

Build for MySQL

> ./gradlew buildProduction

**Port**:  

Running it locally, the port used is: *8080* (empty context)

After build for production, the following configs are available as environment variables:   

* db_url
* db_username
* db_password

## Endpoints

### Manager endpoints

Used for oaBoard frontend app

> /manager [JSON - multiple paths]

### Agent

Used to feed this app.  As a REST endpoint, it may be called regardless of programming language:  

> /agent [PUT]

This endpoint requires 2 extra parts on the path:

- [namespace]/[name]

These are strings indicates the names of a given namespace and the app name.

#### Responses

    * 200 - OK
    * 400 - Bad request: Usually indicates that the arguments are incorrect
    * 409 - Conflict: Some validation has failed. For example, an app is in a given state that shouldn't be changed
        

## Architecture

This backend application is based on Kotlin over JVM, using mostly Java Web platforms. 
The app itself is not big, so it's kept in a single module.  

**Components:**  

 * The base language is Kotlin. But this one is compiled into Java; therefore it run on the JVM and 
   most Java Frameworks/libraries can be used
   
 * Spring Boot App
    * Spring JPA
 * Jax-RS and OpenAPI/Swagger

**Database**:  

 It uses standard SQL. 
 It's ready to run/ be built with either H2 or MySQL.  
 
> Its model probably also fits well in a NoSQL DB. A SQL was chosen for being quicker to start with and it fits better JPA 


## TODOs

> Some ideas for future

- Add and configure security
  - Users / roles
  
- Store requests samples