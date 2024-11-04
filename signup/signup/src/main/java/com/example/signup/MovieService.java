package com.example.signup;


import com.example.signup.Movie;
import com.example.signup.MovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MovieService {

    private static final String UPLOAD_DIR = "uploads/";
    private static final String IMAGE_URL_PREFIX = "/uploads/";

//    private static final String UPLOAD_DIR = "C:/path/to/uploaded/images/";

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public Optional<Movie> getMovieById(Long id) {
        return movieRepository.findById(id);
    }

    // Method to save the image file to the server

    public String saveImage(MultipartFile imageFile) throws IOException {
        // Ensure the upload directory exists
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate a unique filename to prevent overwrites
        String originalFilename = imageFile.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Save the image file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return the URL path that will be used to serve the image
        return IMAGE_URL_PREFIX + uniqueFilename;
    }

    // Method to create a new movie
    public Movie createMovie(String name, String description, String imageUrl) {
        Movie movie = new Movie(name, description, imageUrl);
        return movieRepository.save(movie);
    }

    public Movie createMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    public Movie updateMovie(Long id, Movie updatedMovie) {
        return movieRepository.findById(id)
                .map(movie -> {
                    movie.setName(updatedMovie.getName());
                    movie.setDescription(updatedMovie.getDescription());
                    movie.setImageUrl(updatedMovie.getImageUrl());
                    return movieRepository.save(movie);
                }).orElseThrow(() -> new RuntimeException("Data not found"));
    }

    public void deleteMovie(Long id) {
        movieRepository.deleteById(id);
    }
}
