package com.example.signup;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UsersService implements UserDetailsService {

    @Autowired
    private UsersRepository usersRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsersService(UsersRepository userRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsersModel authenticate(String login, String password) {
        // Step 1: Fetch user by login, which returns an Optional
        Optional<UsersModel> optionalUser = usersRepository.findByLogin(login);

        // Step 2: Check if user exists, then validate the password
        if (optionalUser.isPresent()) {
            UsersModel user = optionalUser.get(); // Get the actual UsersModel object from the Optional
            // Step 3: If the password matches, return the authenticated user
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        }

        // Return null if authentication fails (either user not found or password mismatch)
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch user from the database using login
        Optional<UsersModel> optionalUser = usersRepository.findByLogin(username);

        // Handle case when the user is not found
        UsersModel user = optionalUser.orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Prepend "ROLE_" to the user's role to match Spring Security's expectations
        String roleWithPrefix = "ROLE_" + user.getRole();  // e.g., "ADMIN" becomes "ROLE_ADMIN"

        // Return a UserDetails object with the user's login, password, and authorities (roles)
        return new org.springframework.security.core.userdetails.User(
                user.getLogin(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(roleWithPrefix))
        );
    }



    public UsersModel registerUser(String login, String password, String email, String role) {
        if (usersRepository.findByLogin(login).isPresent()) {
            return null; // User already exists
        }

        UsersModel user = new UsersModel();
        user.setLogin(login);
        user.setPassword(getPasswordEncoder().encode(password));
        user.setEmail(email);
        user.setRole(role);

        return usersRepository.save(user);
    }

    @Autowired
    private ObjectProvider<PasswordEncoder> passwordEncoderProvider;

    private PasswordEncoder getPasswordEncoder() {
        return passwordEncoderProvider.getIfAvailable(() -> new BCryptPasswordEncoder());
    }


    public List<UsersModel> getAllUsers() {
        return usersRepository.findAll();
    }

    public String getLoggedInUserLogin() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}



//@Service
//public class UsersService implements UserDetailsService {
//
//    @Autowired
//    private UsersRepository usersRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        UsersModel user = usersRepository.findByLogin(username)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        return new org.springframework.security.core.userdetails.User(
//                user.getLogin(),
//                user.getPassword(),
//                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
//        );
//    }
//
//    public UsersModel registerUser(String login, String password, String email, String role) {
//        if (usersRepository.findByLogin(login).isPresent()) {
//            return null; // User already exists
//        }
//
//        UsersModel user = new UsersModel();
//        user.setLogin(login);
//        user.setPassword(passwordEncoder.encode(password));
//        user.setEmail(email);
//        user.setRole(role);
//
//        return usersRepository.save(user);
//    }
//
//    // Remove the authenticate method as Spring Security will handle authentication
//}