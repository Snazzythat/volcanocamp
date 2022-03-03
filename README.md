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
    <img src="readme-content/volcano-logo.png" alt="Logo" width="80" height="80" style={mix-blend-mode: multiply}>
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

Side-note: No authentication or authorization was implemented in this project for simplicity purposes.

<p align="right">(<a href="#top">back to top</a>)</p>

### Built With

* [Spring Boot](https://spring.io/projects/spring-boot)
* [Gradle Build Tool](https://gradle.org/)
* [Project Lombok](https://projectlombok.org/)
* [H2 Database](https://www.h2database.com/html/main.html)
* [PostgeSQL Database](https://www.postgresql.org/)
* [Swagger API documentation](https://swagger.io/specification/)

Note: The project has been developed using Eclipse IDE (20190314-1200):

![IDE Screen Shot][ide-screenshot]

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
In order to execute Controller integration tests as well as unit tests, simply run:
```
cd <cloned repo directory>
./gradlew clean test
```

At the end of the execution, you should be able to see the tests results"

![tests][tests]


<p align="right">(<a href="#top">back to top</a>)</p>


## Usage

<!-- TODO -->
The following API end points and methods are provided:

* GET /api/v1/available-dates (Get all available dates for reservations)
* POST /api/v1/reservations (Create a new reservation)
* GET /api/v1/reservations/{id} (Fetch a given reservation by id)
* DELETE  /api/v1/reservations/{id} (Delete a given reservation by id)
* PATCH  /api/v1/reservations/{id} (Update a given reservation by id)

Note:
The format of provided dates in request body or query parameters is yyyy-MM-dd as per [Date ISO](https://en.wikipedia.org/wiki/ISO_8601)

<br />
The reservation date period is considered to have all dates in the period reserved and last date as available (since due to the requirement,
the check-out and check-in are at 00:00). Therefore, as an example, a reservation between 2022-03-03 and 2022-03-05 will have dates reserved:
2022-03-03,2022-03-03,2022-03-04 and 2022-03-05 is available (since current reservation check-out is at 2022-03-03, 00:00 and the next available
registration can start at the same instance).

<br />
<br />
For in-depth API usage (including query parameters and request body fields format), examples and request responses you can access the Swagger UI running along with the application at http://localhost:8080/swagger-ui/index.html:

![Swagger-UI][product-screenshot]


### Examples:

* Fetching all available dates between 2022-03-03 and 2022-03-20 using fromDate and toDate query parameters:

Example of a resulting GET request URl is
```
GET http://localhost:8080/api/v1/available-dates?fromDate=2022-03-03&toDate=2022-03-20
```

Returning a 200 response with all available dates:
```
{
  "fromDate": "2022-03-04",
  "toDate": "2022-03-20",
  "availableDates": [
    "2022-03-04",
    "2022-03-05",
    "2022-03-06",
    "2022-03-07",
    "2022-03-08",
    "2022-03-09",
    "2022-03-10",
    "2022-03-11",
    "2022-03-12",
    "2022-03-13",
    "2022-03-14",
    "2022-03-15",
    "2022-03-16",
    "2022-03-17",
    "2022-03-18",
    "2022-03-19",
    "2022-03-20"
  ]
}
```
<br />

To note, if no dates are provided, the default period used for date fetching is 1 month as per requirement. Therefore,
if todays date is 2022-03-03 and no reservations have been made yet, all dates starting are returned starting with 2022-03-04
(since as per requirement, the first available date is today + 1 day) and 2022-04-03 inclusively:

```
GET http://localhost:8080/api/v1/available-dates
```

Returning a 200 response with all available dates:
```
{
  "fromDate": "2022-03-04",
  "toDate": "2022-04-03",
  "availableDates": [
    "2022-03-04",
    "2022-03-05",
    "2022-03-06",
    "2022-03-07",
    "2022-03-08",
    "2022-03-09",
    "2022-03-10",
    "2022-03-11",
    "2022-03-12",
    "2022-03-13",
    "2022-03-14",
    "2022-03-15",
    "2022-03-16",
    "2022-03-17",
    "2022-03-18",
    "2022-03-19",
    "2022-03-20",
    "2022-03-21",
    "2022-03-22",
    "2022-03-23",
    "2022-03-24",
    "2022-03-25",
    "2022-03-26",
    "2022-03-27",
    "2022-03-28",
    "2022-03-29",
    "2022-03-30",
    "2022-03-31",
    "2022-04-01",
    "2022-04-02",
    "2022-04-03"
  ]
}
```

<br />

To note, dates in the past or more than 1 month can also be provided. In this case, the fromDate parameter will be adjusted to the closest available
date (today + 1 day) and toDate parameter will ne adjusted to be the last possible reservation date (today + 1 month).

<br />

* Creating a new reservation:

Example of a resulting resulting POST request URl is
```
POST http://localhost:8080/api/v1/reservations
```

with body
```
{
  "userFullName": "John Doe",
  "userEmail": "john.doe@upgrade.com",
  "checkinDate": "2022-03-20",
  "checkoutDate": "2022-03-22"
}
```

Returning a 201 response with the created reservation containing a generated UUID:
```
{
  "id": "29642174-2f3c-4714-b922-c93af7e57397",
  "active": true,
  "userFullName": "John Doe",
  "userEmail": "john.doe@upgrade.com",
  "checkinDate": "2022-03-20",
  "checkoutDate": "2022-03-22"
}
```
Note 1: the returned active field is an indicator to the user that he registration is active (not cancelled). It is a read-only field, just like the id field.
Note 2: request validation (date validity, format) is performed as well as verification if the reservation to be created does not overlap with existing reservation(s):

Returning a 409 response indicating that the reservation conflicts with another reservation:
```
{
  "status": "CONFLICT",
  "errorMessage": "Occupied period error",
  "details": [
    "There is at least one unavailable date in the provided time period"
  ],
  "timeStamp": "2022-03-03 05:01:18"
}
```

Returning a 400 response indicating that the the one of the date field format is bad:
POST:
```
{
  "userFullName": "John Doe",
  "userEmail": "john.doe@upgrade.com",
  "checkinDate": "2022-03-20",
  "checkoutDate": "2022-03-22aaa"
}
```
Error:
```
{
  "status": "BAD_REQUEST",
  "errorMessage": "Validation error",
  "details": [
    "Both check-in and check-out dates must have valid format: yyyy-MM-dd"
  ],
  "timeStamp": "2022-03-03 05:04:19"
}
```

Returning a 400 response indicating that the the check-in date cannot be before check-out date
POST:
```
{
  "userFullName": "John Doe",
  "userEmail": "john.doe@upgrade.com",
  "checkinDate": "2022-03-08",
  "checkoutDate": "2022-03-05"
}
```
Error:
```
{
  "status": "BAD_REQUEST",
  "errorMessage": "Validation error",
  "details": [
    "The check-in date must be before the check-out date"
  ],
  "timeStamp": "2022-03-03 05:41:25"
}
```

* Getting a new reservation (using an existing id)

Example of a resulting GET request URl is (with a valid UUID)
```
GET http://localhost:8080/api/v1/reservations/29642174-2f3c-4714-b922-c93af7e57397
```

Returning a 200 response with the requested reservation
```
{
  "id": "29642174-2f3c-4714-b922-c93af7e57397",
  "active": true,
  "userFullName": "John Doe",
  "userEmail": "john.doe@upgrade.com",
  "checkinDate": "2022-03-20",
  "checkoutDate": "2022-03-22"
}
```

Note, error cases exist, such as invalid id or the reservation does not exist. For example, 404 on non-existing reservation:

```
GET http://localhost:8080/api/v1/reservations/29642174-2f3c-4714-b922-c93af7e57395
```
Error:
```
{
  "status": "NOT_FOUND",
  "errorMessage": "Resource not found",
  "details": [
    "Reservation with id 29642174-2f3c-4714-b922-c93af7e57395 is not found"
  ],
  "timeStamp": "2022-03-03 05:21:52"
}
```

* Updateing an existing reservation (using an existing id and valid request)

Example of a resulting PATCH request URl (with a valid UUID):
```
PATCH http://localhost:8080/api/v1/reservations/29642174-2f3c-4714-b922-c93af7e57397

```
and body
```
{
  "userFullName": "John DoeTwo",
  "userEmail": "john.doe_two@upgrade.com",
  "checkinDate": "2022-03-25",
  "checkoutDate": "2022-03-27"
}
```
In case above, the user changed their full name, email as well as checkin and check out date (provided that there no ovelap wiith other registations)

Returning a 200 response with the updated registration:
```
{
  "id": "29642174-2f3c-4714-b922-c93af7e57397",
  "active": true,
  "userFullName": "John DoeTwo",
  "userEmail": "john.doe_two@upgrade.com",
  "checkinDate": "2022-03-25",
  "checkoutDate": "2022-03-27"
}
```

Note, error cases exist, such as invalid id, invalid registration dates, invalid date format, the reservation does not exist or registration dates overlap with an existing reservation:

```
PATCH http://localhost:8080/api/v1/reservations/29642174-2f3c-4714-b922-c93af7e57397
```
and body
```
{
  "userFullName": "John DoeTwo",
  "userEmail": "john.doe_two@upgrade.com",
  "checkinDate": "2022-03-28",
  "checkoutDate": "2022-03-30"
}
```
Error:
```
{
  "status": "CONFLICT",
  "errorMessage": "Occupied period error",
  "details": [
    "There is at least one unavailable date in the provided time period"
  ],
  "timeStamp": "2022-03-03 05:33:59"
}
```

In the example above, there is already an active registration with dates 2022-03-29 - 2022-03-31, hence the attempt with 2022-03-28 - 2022-03-30 fails
<br />

* Cancelling an existing reservation (using an existing id)

Example of a resulting DELETE request URl is (with a valid UUID)
```
DELETE http://localhost:8080/api/v1/reservations/29642174-2f3c-4714-b922-c93af7e57397
```
Returning a 204 response (NO_CONTENT) indicating that the reservation has been cancelled

Note, error cases exist, such as invalid id or the reservation does not exist. For example, 404 on non-existing reservation:
```
DELETE http://localhost:8080/api/v1/reservations/29642174-2f3c-4714-b922-c93af7e57394
```
Error:
```
{
  "status": "NOT_FOUND",
  "errorMessage": "Resource not found",
  "details": [
    "Reservation with id 29642174-2f3c-4714-b922-c93af7e57394 is not found"
  ],
  "timeStamp": "2022-03-03 05:21:52"
}
```

A cancelled reservation still exists in the database, however, its 'active' field is set to false. The dates of that reservation become available for new reservations.

Updating the cancelled reservation via PATCH will be blocked and a 405 response is returned:
```
{
  "status": "METHOD_NOT_ALLOWED",
  "errorMessage": "Operation is not allowed",
  "details": [
    "The reservation that has been cancelled cannot be updated"
  ],
  "timeStamp": "2022-03-03 05:48:47"
}
```

### Postman and curl

To note, one could also issue API requests using [Postman](https://www.postman.com/) or curl Linux command.

- Postman example:
![Postman][postman]

- curl example:
```
curl -X 'POST' \
  'http://localhost:8080/api/v1/reservations' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "userFullName": "John Doe",
  "userEmail": "john.doe@upgrade.com",
  "checkinDate": "2022-03-20",
  "checkoutDate": "2022-03-22"
}'
```

<p align="right">(<a href="#top">back to top</a>)</p>

### Setting the log level via Spring actuator:
During application execution, one can set the appropriate log level on the root logger via the exposed Spring actuator web interface:

- Set log level to INFO:
```
curl -X 'POST' \
  'http://localhost:8080/actuator/loggers/ROOT' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "configuredLevel": "INFO"
}'
```

- Set log level to DEBUG:
```
curl -X 'POST' \
  'http://localhost:8080/actuator/loggers/ROOT' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "configuredLevel": "DEBUG"
}'
```


## General logic and implementation

The application is designed using a 3 layer architecture: Controller-Service-Repository.

Controller:



Service:


Repository:

Data model:


The operations in are marked with @Transactional to group read/write operations in a transaction.
Locking mechanism is also used in 

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
[product-screenshot]: ./readme-content/swagger-main.PNG
[ide-screenshot]: ./readme-content/eclipse-ide.PNG
[dev-run]: ./readme-content/start-with-dev.PNG
[prod-run]: ./readme-content/start-with-prod.PNG
[h2-login]: ./readme-content/h2-console-login.PNG
[h2-console]: ./readme-content/h2-web.PNG
[reservations-postgresql]: ./readme-content/reservations-postgres.PNG
[dates-postgresql]: ./readme-content/dates-postgres.PNG
[tests]: ./readme-content/run-tests-via-gradle.PNG
[postman]: ./readme-content/postman-create.PNG