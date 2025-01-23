package com.example.plsdemo.router;


import com.example.plsdemo.handler.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<?> userRoutes(UserHandler userHandler) {
        return RouterFunctions.route()
                .PUT("/users/{id}", userHandler::updateUser)
                .GET("/users", userHandler::getAllUsers)
                .GET("/users/{id}", userHandler::getUserById)
                .POST("/users", userHandler::createUser)
                .build();
    }
}
