<div id="top"></div>

[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]
[![LinkedIn][linkedin-shield]][linkedin-url]

<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/Snazzythat/volcanocamp">
    <img src="readme-content/volcano-logo.png" alt="Logo" width="80" height="80">
  </a>

<h3 align="center">VolcanoCamp</h3>

  <p align="center">
    REST API for Volcano campsite reservation implemented with Spring Boot
    <br />
    <br />
    <a href="https://github.com/Snazzythat/volcanocamp">View Demo</a>
    ·
    <a href="https://github.com/Snazzythat/volcanocamp/issues">Report Bug</a>
    ·
    <a href="https://github.com/Snazzythat/volcanocamp/issues">Request Feature</a>
  </p>
</div>

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>


<!-- ABOUT THE PROJECT -->
## About The Project

![Product Name Screen Shot][product-screenshot]

This is a take-home challenge.

### Problem to solve:

An underwater volcano formed a new small island in the Pacific Ocean last month. All the conditions on the island seems perfect and it was decided to open it up for the general public to experience the pristine uncharted territory. The island is big enough to host a single campsite so everybody is very excited to visit. In order to regulate the number of people on the island, it
was decided to come up with an online web application to manage the reservations. You are responsible for design and development of a REST API service that will manage the campsite reservations.
To streamline the reservations a few constraints need to be in place:

- The campsite will be free for all.
- The campsite can be reserved for max 3 days.
- The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.
- Reservations can be cancelled anytime.
- For sake of simplicity assume the check-in & check-out time is 12:00 AM

### System Requirements:

- The users will need to find out when the campsite is available. So the system should expose an API to provide information of the
availability of the campsite for a given date range with the default being 1 month.
- Provide an end point for reserving the campsite. The user will provide his/her email & full name at the time of reserving the campsite
along with intended arrival date and departure date. Return a unique booking identifier back to the caller if the reservation is successful.
- The unique booking identifier can be used to modify or cancel the reservation later on. Provide appropriate end point(s) to allow
modification/cancellation of an existing reservation
- Due to the popularity of the island, there is a high likelihood of multiple users attempting to reserve the campsite for the same/overlapping
date(s). Demonstrate with appropriate test cases that the system can gracefully handle concurrent requests to reserve the campsite.
- Provide appropriate error messages to the caller to indicate the error cases.
- In general, the system should be able to handle large volume of requests for getting the campsite availability.
- There are no restrictions on how reservations are stored as as long as system constraints are not violated.

<p align="right">(<a href="#top">back to top</a>)</p>

### Built With

* [Spring Boot](https://spring.io/projects/spring-boot)
* [Gradle Build Tool](https://gradle.org/)
* [Project Lombok](https://projectlombok.org/)
* [H2 Database](https://www.h2database.com/html/main.html)
* [PostgeSQL Database](https://www.postgresql.org/)
* [Swagger API documentation](https://swagger.io/specification/)

Note: The project has been developed using Eclipse IDE (20190314-1200):

![IDE Screen Shot][eclipse-ide]

<p align="right">(<a href="#top">back to top</a>)</p>

## Prerequisites

- Git client installed on your machine
- Java 11 installed on your machine
- A Java IDE supporting Lombok (Eclipse or IntelliJ)
- PostgreSQL and pgAdmin4 installed on your machine with default server (if you want to execute the application in production)

<!-- GETTING STARTED -->
### Getting Started
- Clone this repo in your local environment
- Import the project in your IDE as Gradle Project.
- [Configure your IDE to support Lombok](https://www.baeldung.com/lombok-ide)
- Production only: setup an instance of PostgreSQL server on your machine (see application-prod.properties for user and credentials)


## Building/Running the application

The application can be executed using the Gradle wrapper provided in the project. The application support two Spring profiles: dev (default) and prod.

- The dev profile will run with H2 in-memory database (see application-dev.properties for database related configuration)
- The prod profile will run with PostgreSQL database (see application-prod.properties for database related configuration)

### Building the application:
```
cd <cloned repo directory>
./gradlew build bootJar
```
The above will build a JAR file that can be found in build/libs folder. The JAR is executable.

### Running in dev mode:
In order to run the application in development mode, do the following:
```
cd <cloned repo directory>
./gradlew bootRun --args='--spring.profiles.active=dev'
```
Execution output:

![DEV run][dev-run]

To note, you can access the H2 console in browser when running in dev mode. Once the application started, go to http://localhost:8080/h2-console

You will be propted to login. Use credentials and JDBC resource url provided in application-dev.properties:

![H2 login][h2-login]

Once the login is successfull, you can view the data in the tables. For example:

![H2 data][h2-console]


### Running in prod mode:
In order to run the application in production mode, do the following:
```
cd <cloned repo directory>
./gradlew bootRun --args='--spring.profiles.active=prod'
```
Execution output:

![DEV run][prod-run]

To note, you can use pgAdmin4 (native or web) to access PostgreSQL database. Simply open the pgAdmin4 application (using ), access the default server on http://localhost:5432

Example of reservations table data:

![Postgre reservations][reservations-postgresql]


Example of dates table data:

![Postgre dates][dates-postgresql]

<p align="right">(<a href="#top">back to top</a>)</p>


## Test execution

<!-- TODO -->

<!-- USAGE EXAMPLES -->

<p align="right">(<a href="#top">back to top</a>)</p>


## Usage

<!-- TODO -->
The following API end points are available:

<p align="right">(<a href="#top">back to top</a>)</p>


## General logic and implementation

<!-- TODO -->

<p align="right">(<a href="#top">back to top</a>)</p>


<!-- LICENSE -->
## License

Distributed under the MIT License. See `MIT-LICENSE.txt` for more information.

<p align="right">(<a href="#top">back to top</a>)</p>


<!-- CONTACT -->
## Contact

Your Name - [@debug_master](https://twitter.com/debug_master)

Project Link: [https://github.com/Snazzythat/volcanocamp](https://github.com/Snazzythat/volcanocamp)

<p align="right">(<a href="#top">back to top</a>)</p>


<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/Snazzythat/volcanocamp.svg?style=for-the-badge
[contributors-url]: https://github.com/Snazzythat/volcanocamp/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/Snazzythat/volcanocamp.svg?style=for-the-badge
[forks-url]: https://github.com/Snazzythat/volcanocamp/network/members
[stars-shield]: https://img.shields.io/github/stars/Snazzythat/volcanocamp.svg?style=for-the-badge
[stars-url]: https://github.com/Snazzythat/volcanocamp/stargazers
[issues-shield]: https://img.shields.io/github/issues/Snazzythat/volcanocamp.svg?style=for-the-badge
[issues-url]: https://github.com/Snazzythat/volcanocamp/issues
[license-shield]: https://img.shields.io/github/license/Snazzythat/volcanocamp.svg?style=for-the-badge
[license-url]: https://github.com/Snazzythat/volcanocamp/blob/master/MIT-LICENSE.txt
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://linkedin.com/in/roman-andoni-6584b493
[product-screenshot]: ./readme-content/swagger-main.png
[ide-screenshot]: ./readme-content/eclipse-ide.png
[dev-run]: ./readme-content/start-with-dev.png
[prod-run]: ./readme-content/start-with-prod.png
[h2-login]: ./readme-content/h2-console-login.png
[h2-console]: ./readme-content/h2-console-login.png
[reservations-postgresql]: ./readme-content/reservations-postgres.png
[dates-postgresql]: ./readme-content/dates-postgres.png