package com.sales.market.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sales.market.dto.*;
import com.sales.market.model.Affair;
import com.sales.market.model.RoleType;
import com.sales.market.model.User;
import com.sales.market.service.EmailService;
import com.sales.market.service.GenericService;
import com.sales.market.service.TokenService;
import com.sales.market.service.UserService;
import io.jsonwebtoken.JwtException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
public class UserController extends GenericController<User, UserDto> {

    private final UserService userService;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;

    public UserController(UserService userService, TokenService tokenService, EmailService emailService,
            AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/users/employee")
    public ResponseEntity<EmployeeDto> getUserEmployee(@RequestBody UserDto userDto) {
        try {
            return new ResponseEntity<>(getEmployee(userService.findByEmail(userDto.getEmail())), HttpStatus.OK);
        } catch (NoSuchElementException ex) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @PostMapping("login")
    public ResponseEntity<Object> signIn(@RequestBody UserDto userDto) throws JsonProcessingException {
        ResponseEntity<Object> responseEntity = null;
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(userDto.getEmail(), userDto.getPassword()));
            responseEntity = new ResponseEntity<>(
                    new TokenDto(tokenService.generateTokenByDay(10, authentication.getPrincipal(), true)),
                    HttpStatus.OK);
        } catch (AuthenticationException e) {
            responseEntity = new ResponseEntity<>(new OperationResultDto<>("messages.user.invalidCredentials"),
                    HttpStatus.UNAUTHORIZED);
        }
        return responseEntity;
    }

    /**
     * This method check if the token is valid and also checks if the user
     * performing the action is the same for whom the token was created.
     *
     * @throws IOException
     */
    @PostMapping("/users")
    public ResponseEntity<Object> signUp(@RequestBody UserDto userDto, @RequestParam("token") String token)
            throws IOException {
        ResponseEntity<Object> responseEntity = null;
        try {
            UserDto tokenInformation = tokenService.getTokenInformation(token, UserDto.class);

            if (!userService.isUserRegistered(tokenInformation.getEmail())) {
                userService.save(userDto.getFirstName(), userDto.getLastName(), tokenInformation.getEmail(),
                        userDto.getPassword());
                responseEntity = new ResponseEntity<>(new TokenDto(tokenService.generateTokenByDay(10,
                        userService.findUserDetails(tokenInformation.getEmail()), true)), HttpStatus.OK);
            } else {
                throw new ValidationException();
            }
        } catch (JwtException e) {
            responseEntity = new ResponseEntity<>(new OperationResultDto<>("messages.user.unauthorized"),
                    HttpStatus.UNAUTHORIZED);
        } catch (ValidationException e) {
            responseEntity = new ResponseEntity<>(new OperationResultDto<>("messages.user.duplicatedUser"),
                    HttpStatus.BAD_REQUEST);
        }
        return responseEntity;
    }

    @PostMapping("sendSignUpInvitation")
    public ResponseEntity<Object> inviteNewUser(@Valid @RequestBody UserDto user,
            @RequestParam("redirect") String redirect)
            throws JsonProcessingException {
        Map<String, Object> parameters = new HashMap<>();
        String[] to = {user.getEmail()};
        String url = redirect + "?token=" + tokenService.generateTokenByDay(1, user, false);
        parameters.put("invitationLink", url);
        emailService.sendMail(new MailDto(to, "Subscription link", "invitation-template", parameters));
//        emailService.sendMailOnly(RoleType.GROCER, Affair.INVENTARY_LOWER_BOUND);
        return new ResponseEntity<>(new OperationResultDto<>("messages.user.invitedUser"), HttpStatus.OK);
    }

    @PostMapping("/forgottenPassword")
    public ResponseEntity<Object> sendForgottenPasswordEmail(@Valid @RequestBody UserDto user,
            @RequestParam("redirect") String redirect)
            throws JsonProcessingException {
        ResponseEntity<Object> responseEntity = null;
        if (userService.isUserRegistered(user.getEmail())) {
            Map<String, Object> parameters = new HashMap<>();
            String[] to = {user.getEmail()};
            String url = redirect + "?token=" + tokenService.generateTokenByDay(1, user, false);
            parameters.put("forgottenPasswordLink", url);
            emailService.sendMail(new MailDto(to, "Forgotten password", "forgotten-password-template", parameters));
            responseEntity = new ResponseEntity<>(new OperationResultDto<>("messages.user.forgottenPasswordSent"),
                    HttpStatus.OK);
        } else {
            responseEntity = new ResponseEntity<>(new OperationResultDto<>("messages.user.userNotFound"),
                    HttpStatus.NOT_FOUND);
        }
        return responseEntity;
    }

    @PostMapping("restorePassword")
    public ResponseEntity<Object> restorePassword(@RequestBody UserDto userDto, @RequestParam("token") String token)
            throws IOException {
        ResponseEntity<Object> responseEntity = null;
        try {
            UserDto tokenInformation = tokenService.getTokenInformation(token, UserDto.class);
            int operationResult = userService.updatePasswordByEmail(tokenInformation.getEmail(), userDto.getPassword());
            responseEntity = (operationResult == 1)
                    ? new ResponseEntity<>(new TokenDto(tokenService.generateTokenByDay(10,
                    userService.findUserDetails(tokenInformation.getEmail()), true)), HttpStatus.OK)
                    : new ResponseEntity<>(new OperationResultDto<>("messages.user.notRestoredPassword"),
                    HttpStatus.BAD_REQUEST);
        } catch (JwtException e) {
            responseEntity = new ResponseEntity<>(new OperationResultDto<>("messages.token.invalidToken"),
                    HttpStatus.UNAUTHORIZED);
        }
        return responseEntity;
    }

    @GetMapping("/users")
    public Page<UserDto> findPaginatedUsers(@RequestParam("page") int page, @RequestParam("size") int size,
            @RequestParam("filter") String filter,
            @RequestParam("isAsc") boolean isAsc) {
        Page<UserDto> paginatedResults = userService
                .findUsers(PageRequest.of(page - 1, size, super.getSortType(isAsc, filter))).map(this::toDto);
        return paginatedResults;
    }

    @GetMapping("/users" + "/{id}")
    public ResponseEntity<UserDto> findUserById(@PathVariable("id") long id) {
        User user = userService.findById(id);
        return new ResponseEntity<>(toDto(user), HttpStatus.OK);
    }

    @PutMapping("/users")
    public ResponseEntity<OperationResultDto<UserDto>> updateUser(@RequestBody UserDto userDto) {
        User model = toModel(userDto);
        userService.update(model);
        return new ResponseEntity<>(new OperationResultDto<>("messages.user.updatedUser", toDto(model)), HttpStatus.OK);
    }

    @Override
    @GetMapping("/users" + "/generic")
    public List<UserDto> findAll(@RequestParam(FILTER) String filter) {
        return super.findAll(filter);
    }

    @Override
    protected GenericService<User> getService() {
        return userService;
    }

    private EmployeeDto getEmployee(User user) {
        if (user != null && user.getEmployee() != null) {
            return new EmployeeDto().toDto(user.getEmployee(), modelMapper);
        }
        throw new NoSuchElementException("User does not have an employee asociated or does not exist");
    }

    @Override
    @GetMapping(value = "/user/model/{id}")
    public User findModelById(Long id) {
        return super.findModelById(id);
    }

}
