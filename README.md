# Shorty

It's example Scala application for shortening urls.

## Building

To build project you need [sbt](http://www.scala-sbt.org/) build tool. After installation call:

```
$ sbt compile
```

## Testing

To run unit tests call:

```
$ sbt test
```

## Packaging

To package project into single runnable jar call:

```
$ sbt assembly
```

## Running

Run from sbt with:

```
$ sbt run
```

Or from runnable jar (you may specify additional options):

```
$ java -Dhttp.port=3000 -jar target/scala-2.11/shorty-scala-assembly-0.1-SNAPSHOT.jar
```

Full list of options with defaults:

```
db.host = localhost
db.port = 5432
db.name = shorty
db.user = shorty
db.pass = shorty

http.host = 0.0.0.0
http.port = 8080
```
