# dbtests

A spike project for embedded DB comparisons with Spring Boot

## Purpose

With this code we want to test the scaling & perofmance of an embedded DB for use a ledger in our app.

The DB needs to:

* be responsive to high loads,
* scale to millions of rows,
* be non-blocking for all requests

The model used is [DataBatchLedgerEntry](./src/main/java/com/example/restservice/models/DataBatchLedgerEntry.java)

With a [Repoitory](./src/main/java/com/example/restservice/config/DataBatchLedgerRepository.java)
and a [Service] (./src/main/java/com/example/restservice/config/LedgerService.java)

The [DB config is available here](./src/main/java/com/example/restservice/config/DbConfig.java) - This constains the confog options for each type of DB to test.

Set the `@PropertySource` to the type from the [resource properties files](./src/main/resources)

> Note: for microservuce deployments, these properties files shoudl be replaced with environment variables from the deployment config
>

## Method

[Scheduled Tasks](./src/main/java/com/example/restservice/schedulingtasks/ScheduledTasks.java)
does all the work here.

* `reportCurrentStatus` is our main control loop, logging the corrent count and exiting when we hit our target.
* `writeRandom` creates 100 records and adds them to the DB every 20 milliseconds.
  * Each field is just randomly generated for simplicity
* `readRandom` reads a random row every 10 ms
* `deleteRandom` deleted a random row every 50 ms
* etc...
* additional tasks are added from the exitsing code to add to the stress test

The script is left to run until 2 million records have been added to the DB, or until a fatal error ends the process for us.

The warning+ level log files are recorded for inspection.
The time taken to complete the processs and the final file size for storage are recorded for comparison.

## Results

### SQLite

result: success
time:
file size:

### HyperSQL

result: 
time:
file size:

### H2

result: 
time:
file size:

### Derby

result: 
time:
file size:
