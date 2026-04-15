
# SnowflakeAnnotation

SnowflakeAnnotation is a lightweight Java library designed for Spring Boot applications to generate unique, ordered, and distributed IDs. Based on Twitter's **Snowflake** algorithm, it allows you to automatically populate ID fields in your entity classes using a simple annotation.

## Features

- **Spring Boot Integration:** Quick setup with minimal configuration.
    
- **Annotation-Based:** Keeps your business logic clean by using `@SnowflakeId` to handle ID generation.
    
- **Distributed System Support:** Prevents collisions across microservices with `Worker ID` and `Datacenter ID` support.
    
- **Time-Ordered:** IDs are generated based on timestamps, making them naturally sortable.
    
- **High Performance:** Generates IDs at the application level without relying on external database sequences or locks.
    

## Installation

### Maven

Add the following dependency to your `pom.xml` (after installing the library to your local or central repository):

XML

```
<dependency>
    <groupId>io.github.tolunaym</groupId>
    <artifactId>SnowflakeAnnotation</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

### 1. Configuration

Define the following properties in your `application.properties` or `application.yml` to ensure unique ID generation across different nodes:

Properties

```
snowflake.worker-id=1
snowflake.datacenter-id=1
```

### 2. Implementation in Entities

Simply add the `@SnowflakeId` annotation to the field you want and `@SnowflakeData` to the class you want to be automatically populated:

Java

```

@SnowflakeData
public class User {

    @SnowflakeId
    private Long id;

    private String username;
    private String email;

    // Getters and Setters
}
```

### 3. How it Works

The library intercepts the entity lifecycle (via an Aspect or a pre-persist hook depending on your configuration) and injects a 64-bit unique ID before the object is saved.

## ID Structure

The 64-bit ID follows the standard Snowflake structure:

- **1 Bit:** Unused (always 0).
    
- **41 Bits:** Timestamp in milliseconds (provides ~69 years of IDs).
    
- **5 Bits:** Datacenter ID.
    
- **5 Bits:** Worker ID.
    
- **12 Bits:** Sequence number (allows 4096 IDs per millisecond per node).
    

## Contributing

Contributions are welcome! If you find a bug or have a feature request, please open an issue or submit a pull request.

## License

This project is licensed under the MIT License - see the `LICENSE` file for details.
