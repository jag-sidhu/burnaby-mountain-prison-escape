# Spring2026Team10

Members:  
Aujla, Tarnjot,	tsa198@sfu.ca  
Behniwal, Arsh,	asb62@sfu.ca  
Janus, Dominic,	dtj@sfu.ca  
Sidhu, Jagdeep,	jss80@sfu.ca  
Zhou, Nick,	nza53@sfu.ca  

## Project Overview

Escape from the Burnaby Mountain Prison is a 2D tile-based prison escape game written in Java. The player must move through the prison, collect all three rewards, avoid guards and hazards, and reach the exit to win.

## Build

Build the project from the repository root with:

```bash
mvn clean package
```

This creates the game jar at:

```text
target/Spring2026Team10-1.0-SNAPSHOT.jar
```

If you want to build the jar without running the tests, use:

```bash
mvn clean package -DskipTests
```

## Run

Run the packaged jar with:

```bash
java -jar target/Spring2026Team10-1.0-SNAPSHOT.jar
```

You can also run the game directly from an IDE by starting:

```text
Spring2026Team10.Main
```

## Test

Run the full automated test suite with:

```bash
mvn test
```

Run a single test class with:

```bash
mvn test -Dtest=TestGuard
```

Examples of other targeted test runs:

```bash
mvn test -Dtest=TestGameAndPlayer
mvn test -Dtest=TestPrisonMapAndPanel
mvn test -Dtest=TestPowerups
mvn test -Dtest=TestHazard
mvn test -Dtest=TestHazardsAndPowerups
mvn test -Dtest=TestRewards
mvn test -Dtest=TestSound
```

JaCoCo coverage reports are generated after the test run and can be found at:

```text
target/site/jacoco/index.html
```
