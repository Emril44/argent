package org.example.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/users")
public class UserController {
    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> newUser(@RequestBody UserRequest request) {
        User newUser = userService.register(request.getName(), request.getEmail(), request.getPassword());
        UserResponse userResponse = new UserResponse(newUser.getId(), newUser.getName(), newUser.getEmail(), newUser.getStatus(), newUser.getCreatedAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }
}
