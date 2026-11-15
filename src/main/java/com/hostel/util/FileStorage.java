package com.hostel.util;

import com.hostel.exception.DataAccessException;
import com.hostel.exception.NotFoundException;
import com.hostel.exception.ValidationException;
import jakarta.servlet.http.Part;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

public final class FileStorage {
    public static final long MAX_IMAGE_BYTES = 2L * 1024 * 1024;
    private static final long MAX_IMAGE_PIXELS = 20_000_000L;
    private final Path uploadDirectory;

    public FileStorage() {
        this(Path.of(DBConnection.getAppProperty("upload.dir")
                .replace("${user.home}", System.getProperty("user.home"))));
    }

    public FileStorage(Path uploadDirectory) {
        this.uploadDirectory = uploadDirectory.toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDirectory);
        } catch (IOException exception) {
            throw new DataAccessException("Could not create the upload directory.", exception);
        }
    }

    public String save(Part part) {
        if (part == null || part.getSize() == 0) {
            return null;
        }
        if (part.getSize() > MAX_IMAGE_BYTES) {
            throw invalidImage("Image must not exceed 2 MB.");
        }
        String extension = allowedExtension(part.getSubmittedFileName());
        inspect(part, extension);
        String storedName = UUID.randomUUID() + "." + extension;
        Path destination = uploadDirectory.resolve(storedName).normalize();
        if (!destination.getParent().equals(uploadDirectory)) {
            throw new IllegalStateException("Generated upload path escaped its directory.");
        }
        try (InputStream input = part.getInputStream()) {
            Files.copy(input, destination);
            return storedName;
        } catch (IOException exception) {
            try {
                Files.deleteIfExists(destination);
            } catch (IOException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            throw new DataAccessException("Could not store the uploaded image.", exception);
        }
    }

    public Path resolve(String storedName) {
        if (storedName == null || !storedName.matches(
                "[0-9a-fA-F-]{36}\\.(jpg|jpeg|png)")) {
            throw new NotFoundException("Image was not found.");
        }
        Path path = uploadDirectory.resolve(storedName).normalize();
        if (!path.getParent().equals(uploadDirectory) || !Files.isRegularFile(path)) {
            throw new NotFoundException("Image was not found.");
        }
        return path;
    }

    public void delete(String storedName) {
        if (storedName == null) {
            return;
        }
        Path path = resolvePathForCleanup(storedName);
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new DataAccessException("Could not remove the stored image.", exception);
        }
    }

    private void inspect(Part part, String expectedExtension) {
        try (InputStream input = part.getInputStream();
             ImageInputStream imageInput = ImageIO.createImageInputStream(input)) {
            if (imageInput == null) {
                throw invalidImage("The uploaded file is not a supported image.");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                throw invalidImage("The uploaded file is not a real JPG or PNG image.");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInput, true, true);
                String detectedFormat = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!extensionMatches(expectedExtension, detectedFormat)) {
                    throw invalidImage("The image content does not match its file extension.");
                }
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || (long) width * height > MAX_IMAGE_PIXELS) {
                    throw invalidImage("The image dimensions are not supported.");
                }
                BufferedImage decoded = reader.read(0);
                if (decoded == null) {
                    throw invalidImage("The uploaded file is not a real image.");
                }
            } finally {
                reader.dispose();
            }
        } catch (IOException exception) {
            throw new ValidationException(java.util.Map.of("image", "The uploaded image could not be read."));
        }
    }

    private Path resolvePathForCleanup(String storedName) {
        if (storedName == null || !storedName.matches("[0-9a-fA-F-]{36}\\.(jpg|jpeg|png)")) {
            throw new IllegalArgumentException("Stored image name is invalid.");
        }
        Path path = uploadDirectory.resolve(storedName).normalize();
        if (!path.getParent().equals(uploadDirectory)) {
            throw new IllegalArgumentException("Stored image path is outside the upload directory.");
        }
        return path;
    }

    private static String allowedExtension(String submittedName) {
        if (submittedName == null || submittedName.isBlank()) {
            throw invalidImage("Choose a JPG or PNG image.");
        }
        String normalizedName = submittedName.replace('\\', '/');
        String filename = normalizedName.substring(normalizedName.lastIndexOf('/') + 1);
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            throw invalidImage("Only JPG and PNG images are allowed.");
        }
        String extension = filename.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!extension.equals("jpg") && !extension.equals("jpeg") && !extension.equals("png")) {
            throw invalidImage("Only JPG and PNG images are allowed.");
        }
        return extension;
    }

    private static boolean extensionMatches(String extension, String format) {
        return extension.equals("png") ? format.equals("png")
                : format.equals("jpeg") || format.equals("jpg");
    }

    private static ValidationException invalidImage(String message) {
        return new ValidationException(java.util.Map.of("image", message));
    }

}
