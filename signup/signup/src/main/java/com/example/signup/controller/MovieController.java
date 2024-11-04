package com.example.signup.controller;

import com.example.signup.Movie;
import com.example.signup.MovieService;
import com.example.signup.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/movies")
public class MovieController {

    private final MovieService movieService;
    private final PdfService pdfService;

    public MovieController(MovieService movieService,PdfService pdfService) {
        this.movieService = movieService;
        this.pdfService=pdfService;
    }

    // Endpoint to create a new movie
    @PostMapping("/create")
    public String createMovie(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam("imageFile") MultipartFile imageFile,
            Model model) throws IOException {
        try {

            // Save the uploaded image file to a specific location
            String imageUrl = movieService.saveImage(imageFile);

            // Create the movie using the service with the correct parameters
            movieService.createMovie(name, description, imageUrl);

            List<Movie> movies = movieService.getAllMovies();
            model.addAttribute("movies", movies);

            return "admin_page"; // Redirect to the admin page after creation
        }catch (Exception e){
            // Log the exception for debugging
            System.out.println("Error while creating movie: " + e.getMessage());
            throw new RuntimeException("An error occurred while creating the movie.");
        }
    }


    // Endpoint to get all movies for both admin and regular users
    @GetMapping
    public String getAllMovies(Model model) {
        List<Movie> movies = movieService.getAllMovies();
        model.addAttribute("movies", movies);
        return "movies"; // This should point to the movie list template for normal users
    }

    @GetMapping("/search")
    public String searchMovie(@RequestParam("id") Long id, Model model) {
        Optional<Movie> movieOptional = movieService.getMovieById(id);

        if (movieOptional.isPresent()) {
            model.addAttribute("searchedMovie", movieOptional.get()); // Add the actual Movie object, not the Optional
        } else {
            model.addAttribute("searchError", "Movie not found"); // Add an error message if movie doesn't exist
        }

        // Also add the list of all movies so the page still shows the full list
        List<Movie> movies = movieService.getAllMovies();
        model.addAttribute("movies", movies);

        return "admin_page";
    }


    // Update movie by ID
    @PostMapping("/update/{id}")
    public String updateMovie(@PathVariable Long id,
                              @RequestParam String name,
                              @RequestParam String description,
                              @RequestParam("imageFile") MultipartFile imageFile,
                              Model model) throws IOException {
        String imageUrl = movieService.saveImage(imageFile);

        // Create an updated movie object with the new information
        Movie updatedMovie = new Movie();
        updatedMovie.setName(name);
        updatedMovie.setDescription(description);
        updatedMovie.setImageUrl(imageUrl);

        // Call the update service method
        movieService.updateMovie(id, updatedMovie);

        // Redirect back to the admin page after update
        return "redirect:/admin_page";
    }


    // Endpoint to display movies for admin
    @GetMapping("/admin")
    public String getAllMoviesForAdmin(Model model) {
        List<Movie> movies = movieService.getAllMovies();
        model.addAttribute("movies", movies);
        return "admin_page"; // Pointing to the admin page where movies are listed
    }

    // Endpoint to get movie by ID
    @GetMapping("/{id}")
    public String getMovieById(@PathVariable Long id, Model model) {
        Optional<Movie> movie = movieService.getMovieById(id);
        movie.ifPresent(m -> model.addAttribute("movie", m));
        return movie.isPresent() ? "movie_detail" : "404"; // Show details page or 404 if not found
    }


    // Endpoint to delete a movie
    @PostMapping("/delete/{id}")
    public String deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return "redirect:/admin_page"; // Redirect to admin page after deletion
    }


    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadMoviePdf(@PathVariable Long id) {
        try {
            Movie movie = movieService.getMovieById(id)
                    .orElseThrow(() -> new RuntimeException("Movie not found"));

            byte[] pdfContent = pdfService.generateMoviePdf(movie);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", movie.getName() + ".pdf");

            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}
