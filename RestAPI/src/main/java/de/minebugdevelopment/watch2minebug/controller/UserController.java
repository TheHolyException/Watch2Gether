package de.minebugdevelopment.watch2minebug.controller;

import com.sun.source.tree.SynchronizedTree;
import de.minebugdevelopment.watch2minebug.dtos.UserDTO;
import de.minebugdevelopment.watch2minebug.entity.UserEntity;
import de.minebugdevelopment.watch2minebug.repository.UserRepository;
import de.minebugdevelopment.watch2minebug.requests.UserChangeRequest;
import de.minebugdevelopment.watch2minebug.security.JwtTokenProvider;
import de.minebugdevelopment.watch2minebug.security.SecurityUtils;
import de.minebugdevelopment.watch2minebug.utils.dto.DTObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.swing.text.html.Option;
import java.util.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping()
    public void createUser(@RequestBody UserChangeRequest userChangeRequest) {
        userRepository.save(new UserEntity(userChangeRequest));
    }

    @GetMapping("/self")
    public DTObject get() {
        try {
            Optional<UserEntity> optionalUserEntity = userRepository.findById(SecurityUtils.getCurrentUser().getUserId());
            if (optionalUserEntity.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found");
            return new UserDTO().parseFrom(optionalUserEntity.get());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping()
    public List<DTObject> getUsers() {
        return userRepository.findAll().stream().map(entity -> new UserDTO().parseFrom(entity)).toList();
    }

    @GetMapping("/{uuid}")
    public DTObject getUser(@PathVariable("uuid") String strUUID) {
        Optional<UserEntity> oue = userRepository.findById(UUID.fromString(strUUID));
        if (oue.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        UserDTO dto = (UserDTO) new UserDTO().parseFrom(oue.get());
        try {
            SecurityUtils.checkAuthority("user.fullinfo");
        } catch (SecurityException ex) {
            // Hide personal userdata if the requester does not have the permissions
            dto.setFirstName(null);
            dto.setLastName(null);
        }
        return new UserDTO().parseFrom(oue.get());
    }

}
