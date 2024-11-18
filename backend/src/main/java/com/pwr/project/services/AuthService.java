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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
    public UserDetails loadUserByUsername(String login){
        return userRepository.findByLogin(login);
    }
    public void register(RegisterDTO registerDTO) throws InvalidJWTException {
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
                    .withTemporaryPassword(registerDTO.password());

            cognitoClient.adminCreateUser(request);

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
            return userRepository.findUserByLogin(login);
        } else if (principal instanceof String) {
            return userRepository.findUserByLogin((String) principal);
        }
        throw new IllegalStateException("Current user is not authenticated");
    }

}
