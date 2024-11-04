package com.example.signup.controller;

import com.example.signup.Movie;
import com.example.signup.MovieService;
import com.example.signup.UsersModel;
import com.example.signup.UsersService;
import com.example.signup.languages.MyLocaleResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.core.GrantedAuthority;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UsersController {

    private final UsersService usersService;
    private final MovieService movieService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private MyLocaleResolver myLocaleResolver;

    @Autowired
    public UsersController(UsersService usersService, MovieService movieService) {
        this.usersService = usersService;
        this.movieService = movieService;
    }



    @GetMapping("/")
    public String getIndex() {
        return "index";
    }

    @GetMapping("/register")
    public String getRegisterPage(Model model) {
        model.addAttribute("registerRequest", new UsersModel());
        return "register_page";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute UsersModel usersModel, Model model) {
        UsersModel registeredUser = usersService.registerUser(usersModel.getLogin(), usersModel.getPassword(), usersModel.getEmail(), usersModel.getRole());
        if (registeredUser == null) {
            model.addAttribute("error", "Registration failed. Username may already exist.");
            return "register_page";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "login_page";
    }

//    @GetMapping("/personal_page")
//    public String getPersonalPage(Model model, Principal principal) {
//        String username = principal.getName();
//        model.addAttribute("username", username);
//        return "personal_page";
//    }

    @GetMapping("/admin_page")
    @PreAuthorize("hasRole('ADMIN')")
    public String getAdminPage(Model model, Principal principal) {
        String username = principal.getName();
//        model.addAttribute("userLogin", username);
        model.addAttribute("userLogin", usersService.getLoggedInUserLogin());

        List<Movie> movies = movieService.getAllMovies();
        List<UsersModel> registeredUsers = usersService.getAllUsers();
        model.addAttribute("users", registeredUsers);
        model.addAttribute("movies",movies);
        return "admin_page";
    }




//    @PostMapping("/perform_login")
//    public String login(@ModelAttribute UsersModel usersModel, Model model) {
//        // Authenticate the user based on login and password (optionally role)
//        UsersModel authenticatedUser = usersService.authenticate(usersModel.getLogin(), usersModel.getPassword());
//
//        if (authenticatedUser != null) {
//            // Add user login and role to the model (for display or debugging purposes)
//            model.addAttribute("userLogin", authenticatedUser.getLogin());
//            model.addAttribute("userRole", authenticatedUser.getRole());
//
//            // Check user role and redirect accordingly
//            if ("ROLE_ADMIN".equals(authenticatedUser.getRole())) {
//                return "redirect:/admin_page"; // Redirect to the admin page for admins
//            } else {
//                return "redirect:/personal_page"; // Redirect to the personal page for regular users
//            }
//        } else {
//            // Add an error message to the model if authentication fails
//            model.addAttribute("error", "Invalid username or password.");
//            return "login_page"; // Return the user to the login page with an error
//        }
//    }

    @GetMapping("/personal_page")
    public String getPersonalPage(Model model, Principal principal) {
        // Print roles to console
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (principal != null) {
            String userLogin = principal.getName();
            String userRole = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(", ")); // Collect roles as comma-separated string

            model.addAttribute("userLogin", userLogin);
            model.addAttribute("userRole", userRole);
        } else {
            model.addAttribute("userLogin", "Guest");
            model.addAttribute("userRole", null);  // Explicitly set to null for guest users
        }

        List<Movie> movies = movieService.getAllMovies();
        model.addAttribute("movies", movies);

        return "personal_page";
    }




    @GetMapping("/forgotPassword")
    public String showForgotPasswordPage() {
        return "forgotPassword"; // The HTML file name
    }

//    @GetMapping("/resetPassword")
//    public String showResetPasswordPage() {
//        return "resetPassword"; // For the reset password form
//    }

    @GetMapping("/greeting")
    public String greeting(HttpServletRequest request){

        return messageSource.getMessage("greetiing",null,myLocaleResolver.resolveLocale(request));
    }


}

