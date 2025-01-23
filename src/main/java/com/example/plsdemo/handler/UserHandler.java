package com.example.plsdemo.handler;

import com.example.plsdemo.model.User;
import com.example.plsdemo.repository.UserRepository;
import com.example.plsdemo.service.KafkaProducer;
import com.example.plsdemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class UserHandler {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private UserRepository userRepository;

    private final UserService userService;
    private final Validator validator;

    public UserHandler(UserService userService, Validator validator) {
        this.userService = userService;
        this.validator = validator;
    }

    public Mono<ServerResponse> getAllUsers(ServerRequest request) {
        return ServerResponse.ok().body(userService.getAllUsers().delayElements(Duration.ofSeconds(1)), User.class);
    }

    public Mono<ServerResponse> getUserById(ServerRequest request) {
        String userId = request.pathVariable("id");
        return userService.getUserById(userId)
                .flatMap(user -> ServerResponse.ok().bodyValue(user))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(User.class)
                .map(req -> new User(null, req.getName(), req.getSalary(), req.getAge()))
                .flatMap(userService::saveUser)
                .flatMap(user -> ServerResponse.ok().bodyValue(user));
    }

    public Mono<ServerResponse> updateUser(ServerRequest request) {
        String userId = request.pathVariable("id"); // Extract the user ID from the request URL

        return request.bodyToMono(User.class) // Parse the incoming request body into a User object
                .flatMap(updatedUser -> userRepository.findById(userId) // Check if the user exists
                        .flatMap(existingUser -> {
                            // Update the existing user's fields
                            existingUser.setName(updatedUser.getName());
                            existingUser.setSalary(updatedUser.getSalary());
                            existingUser.setAge(updatedUser.getAge());

                            // Save the updated user and produce a Kafka message
                            return userRepository.save(existingUser)
                                    .doOnSuccess(savedUser -> kafkaProducer.sendMessage("User updated: Name " + savedUser.getName() +" Age "+ savedUser.getAge()+" Salary "+ savedUser.getSalary()))
                                    .flatMap(savedUser -> ServerResponse.ok().bodyValue(savedUser)); // Respond with the updated user
                        })
                        .switchIfEmpty(ServerResponse.notFound().build()) // Return 404 if the user doesn't exist
                )
                .onErrorResume(e -> ServerResponse.badRequest().bodyValue("Error: " + e.getMessage())); // Handle errors
    }

}
