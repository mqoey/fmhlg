package com.example.interview.controller;

import com.example.interview.dto.UserRequestDto;
import com.example.interview.dto.UserResponseDto;
import com.example.interview.exception.ResourceNotFoundException;
import com.example.interview.model.User;
import com.example.interview.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> new UserResponseDto(user.getId(), user.getName(), user.getEmail()))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserResponseDto getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return new UserResponseDto(user.getId(), user.getName(), user.getEmail());
    }

    @PostMapping
    public UserResponseDto createUser(@RequestBody UserRequestDto userRequestDto) {
        User user = new User();
        user.setName(userRequestDto.getName());
        user.setEmail(userRequestDto.getEmail());
        User savedUser = userRepository.save(user);
        return new UserResponseDto(savedUser.getId(), savedUser.getName(), savedUser.getEmail());
    }

    @PutMapping("/{id}")
    public UserResponseDto updateUser(@PathVariable Long id, @RequestBody UserRequestDto userRequestDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        existingUser.setName(userRequestDto.getName());
        existingUser.setEmail(userRequestDto.getEmail());
        User updatedUser = userRepository.save(existingUser);
        return new UserResponseDto(updatedUser.getId(), updatedUser.getName(), updatedUser.getEmail());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}