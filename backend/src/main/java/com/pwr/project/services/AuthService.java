package com.pwr.project.services;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.*;
import com.pwr.project.dto.JwtDTO;
import com.pwr.project.dto.LoginDTO;
import com.pwr.project.dto.RegisterDTO;
import com.pwr.project.entities.User;
import com.pwr.project.exceptions.InvalidJWTException;
import com.pwr.project.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private AWSCognitoIdentityProvider cognitoClient;

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${aws.cognito.clientId}")
    private String clientId;


    @Override
    public UserDetails loadUserByUsername(String login) {
        return userRepository.findByLogin(login)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    //toDo : ustawienie stałego hasła
    public void register(RegisterDTO registerDTO) throws InvalidJWTException {
        if (userRepository.findByLogin(registerDTO.login()).isPresent()) {
            throw new InvalidJWTException("Username already exists");
        }
        try {
            AdminCreateUserRequest request = new AdminCreateUserRequest()
                    .withUserPoolId(userPoolId)
                    .withUsername(registerDTO.login())
                    .withUserAttributes(
                            new AttributeType().withName("email").withValue(registerDTO.login()),
                            new AttributeType().withName("given_name").withValue(registerDTO.firstname()),
                            new AttributeType().withName("family_name").withValue(registerDTO.surname()),
                            new AttributeType().withName("custom:isSeller").withValue(registerDTO.isSeller().toString())
                    )
                    .withMessageAction("SUPPRESS")
                    .withTemporaryPassword(registerDTO.password());

            AdminCreateUserResult cognitoResult = cognitoClient.adminCreateUser(request);
            String cognitoSub = cognitoResult.getUser().getUsername();

            User newUser = User.builder()
                    .firstName(registerDTO.firstname())
                    .surname(registerDTO.surname())
                    .login(registerDTO.login())
                    .email(registerDTO.login())
                    .isSeller(registerDTO.isSeller())
                    .cognitoSub(cognitoSub)
                    .build();
            System.out.println("Saving user to database: " + newUser);
            userRepository.save(newUser);

            AdminSetUserPasswordRequest setPasswordRequest = new AdminSetUserPasswordRequest()
                    .withUserPoolId(userPoolId)
                    .withUsername(registerDTO.login())
                    .withPassword(registerDTO.password())
                    .withPermanent(true);

            cognitoClient.adminSetUserPassword(setPasswordRequest);

        } catch (AWSCognitoIdentityProviderException e) {
            throw new InvalidJWTException("Error while creating user in Cognito " + e.getMessage());
        }
    }
    public JwtDTO login(LoginDTO loginDTO) throws InvalidJWTException {
        try {
            AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
                    .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .withUserPoolId(userPoolId)
                    .withClientId(clientId)
                    .withAuthParameters(Map.of(
                            "USERNAME", loginDTO.login(),
                            "PASSWORD", loginDTO.password()
                    ));

            AdminInitiateAuthResult result = cognitoClient.adminInitiateAuth(authRequest);
            return new JwtDTO(result.getAuthenticationResult().getIdToken());

        } catch (AWSCognitoIdentityProviderException e) {
            throw new InvalidJWTException("Error while logging user in Cognito " + e.getMessage());
        }
    }


    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String login = ((UserDetails) principal).getUsername();
            return userRepository.findUserByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found"));
        } else if (principal instanceof String) {
            return userRepository.findUserByLogin((String) principal)
                .orElseThrow(() -> new RuntimeException("User not found"));
        }
        throw new IllegalStateException("Current user is not authenticated");
    }

    public List<User> getAllUsers() {
        ListUsersRequest request = new ListUsersRequest()
                .withUserPoolId(userPoolId);
        ListUsersResult result = cognitoClient.listUsers(request);

        return result.getUsers().stream().map(user -> {
            String username = user.getUsername();
            String email = user.getAttributes().stream()
                    .filter(attr -> "email".equals(attr.getName()))
                    .findFirst()
                    .map(AttributeType::getValue)
                    .orElse(null);
            String givenName = user.getAttributes().stream()
                    .filter(attr -> "given_name".equals(attr.getName()))
                    .findFirst()
                    .map(AttributeType::getValue)
                    .orElse(null);
            String familyName = user.getAttributes().stream()
                    .filter(attr -> "family_name".equals(attr.getName()))
                    .findFirst()
                    .map(AttributeType::getValue)
                    .orElse(null);
            Boolean isSeller = user.getAttributes().stream()
                    .filter(attr -> "custom:isSeller".equals(attr.getName()))
                    .findFirst()
                    .map(attr -> Boolean.valueOf(attr.getValue()))
                    .orElse(false);

            return User.builder()
                    .firstName(givenName)
                    .surname(familyName)
                    .login(username)
                    .email(email)
                    .isSeller(isSeller)
                    .build();
        }).collect(Collectors.toList());
    }
}
