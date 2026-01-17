[![badge](https://img.shields.io/maven-central/v/nl.bronsenproject/dispatcher)](https://search.maven.org/artifact/nl.bronsenproject/dispatcher)
[![CodeQL](https://github.com/bronsenproject/dispatcher/actions/workflows/github-code-scanning/codeql/badge.svg?event=check_run)](https://github.com/bronsenproject/dispatcher/actions/workflows/github-code-scanning/codeql)

# Dispatcher

A lightweight, MediatR-inspired mediator pattern implementation for Java and Spring. This project provides a robust way to decouple your application's request/response logic. It is designed to be used directly in your API controllers or within other handlers, avoiding unnecessary service layers.

## Features

- **Direct Usage**: Intended for use directly in Controllers or from within other Handlers.
- **Synchronous and Asynchronous Dispatching**: Send requests and get results immediately or via `CompletableFuture`.
- **Automatic Handler Discovery**: Automatically finds and registers all `TypedHandler` beans from the Spring application context.
- **Pipeline Behaviors**: Intercept requests with cross-cutting concerns like logging, validation, or transaction management.
- **Strongly Typed**: Uses Java generics to ensure type safety for requests and responses.
- **Command/Query Separation**: Marker interfaces for `Command` (write) and `Query` (read) operations.

## Core Concepts

### Request
A marker interface for a request that returns a result of type `R`.
```java
public record MyRequest(String data) implements Request<MyResponse> {}
```

### Command and Query
Specialized versions of `Request` for CQRS patterns.
```java
public record CreateUserCommand(String name) implements Command<Long> {}
public record GetUserQuery(Long id) implements Query<UserDto> {}
```

### Handler
The logic that processes a specific request. Implement `TypedHandler<RequestType, ResponseType>`.
```java
@Component
public class MyHandler implements TypedHandler<MyRequest, MyResponse> {
    @Override
    public MyResponse handle(MyRequest request) {
        return new MyResponse("Processed: " + request.data());
    }
}
```

### Behavior
Allows you to implement cross-cutting concerns.
```java
@Component
@Order(1)
public class LoggingBehavior implements Behavior {
    @Override
    public <R> R handle(Request<R> request, Next<R> next) {
        System.out.println("Executing request: " + request.getClass().getSimpleName());
        R result = next.call();
        System.out.println("Finished request: " + request.getClass().getSimpleName());
        return result;
    }
}
```

## Usage Examples

### Usage in a Controller (Recommended)
```java
@RestController
@RequestMapping("/users")
public class UserController {
    private final Dispatcher dispatcher;

    public UserController(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @PostMapping
    public ResponseEntity<Long> createUser(@RequestBody CreateUserCommand command) {
        Long userId = dispatcher.send(command);
        return ResponseEntity.ok(userId);
    }
}
```

### Usage within another Handler
```java
@Component
public class ComplexOperationHandler implements TypedHandler<ComplexRequest, Boolean> {
    private final Dispatcher dispatcher;

    public ComplexOperationHandler(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public Boolean handle(ComplexRequest request) {
        // You can call other commands or queries from within a handler
        var userInfo = dispatcher.send(new GetUserQuery(request.userId()));
        // ... more logic
        return true;
    }
}
```

### Asynchronous Usage
```java
public void createUserAsync(String name) {
    CreateUserCommand command = new CreateUserCommand(name);
    dispatcher.sendAsync(command)
        .thenAccept(userId -> System.out.println("User created asynchronously with ID: " + userId));
}
```

## Getting Started

1. **Add Dependency**: Add the dispatcher library to your project's dependencies.
2. **Auto-Configuration**: For Spring Boot projects, the `Dispatcher` bean is automatically configured. No manual scanning or `@Import` is required for the library's package.
3. **Define Requests and Handlers**: Create your request records/classes and their corresponding handlers annotated with `@Component`.
4. **Inject Dispatcher**: Inject the `Dispatcher` bean wherever you need to send requests.

## Requirements

- Java 17 or higher
- Spring Framework 6.x (or Spring Boot 3.x)
