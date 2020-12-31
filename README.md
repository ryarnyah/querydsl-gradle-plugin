# QueryDSL Gradle Plugin

Port of [QueryDSL](https://github.com/querydsl/querydsl) Maven plugin for Gradle.

## HowTo To use
```kotlin
plugins {
    //...
    id("com.github.ryarnyah.querydsl") version "0.0.2"
}

tasks {
    queryDslMetadataExport {
        jdbcDriver = "org.h2.Driver"
        jdbcUser = "sa"
        jdbcPassword = ""
        jdbcUrl = "jdbc:h2:./test;AUTO_SERVER=TRUE"
        packageName = "com.example.querydsl"

        exportBeans = true
        exportTables = true
    }
}
```

## Tasks supported
### queryDslMetadataExport
```kotlin
tasks {
    queryDslMetadataExport {
        jdbcDriver = "org.h2.Driver"
        jdbcUser = "sa"
        jdbcPassword = ""
        jdbcUrl = "jdbc:h2:./test;AUTO_SERVER=TRUE"
        packageName = "com.example.querydsl"

        exportBeans = true
        exportTables = true
    }
}
```
### queryDslJdoExport
```kotlin
tasks {
    queryDslJdoExport {
        packages = arrayOf("com.example.test")
    }
}
```
### queryDslJpaExport
```kotlin
tasks {
    queryDslJpaExport {
        packages = arrayOf("com.example.test")
    }
}
```
### queryDslGenericExport
```kotlin
tasks {
    queryDslGenericExport {
        packages = arrayOf("com.example.test")
    }
}
```

## Current status
- [X] Support metadata export
- [X] Experimental support for other export tasks