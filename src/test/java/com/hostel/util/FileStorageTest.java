package com.hostel.util;

import jakarta.servlet.http.Part;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageTest {
    @TempDir
    Path uploadDirectory;

    @Test
    void storesOnlyValidImagesWithSafeGeneratedNames() throws IOException {
        FileStorage storage = new FileStorage(uploadDirectory);
        String stored = storage.save(part("room.png", pngImage(), "image/png"));

        assertTrue(stored.matches("[0-9a-fA-F-]{36}\\.png"));
        assertTrue(Files.isRegularFile(storage.resolve(stored)));
        storage.delete(stored);
        assertTrue(Files.notExists(uploadDirectory.resolve(stored)));
    }

    @Test
    void rejectsRenamedTextAndOversizedImages() {
        FileStorage storage = new FileStorage(uploadDirectory);
        assertThrows(RuntimeException.class,
                () -> storage.save(part("fake.jpg", "not an image".getBytes(), "image/jpeg")));
        assertThrows(RuntimeException.class,
                () -> storage.save(part("large.jpg", new byte[(int) FileStorage.MAX_IMAGE_BYTES + 1],
                        "image/jpeg")));
        assertThrows(RuntimeException.class,
                () -> storage.resolve("../../outside.png"));
    }

    @Test
    void rejectsExtensionThatDoesNotMatchImageContents() throws IOException {
        FileStorage storage = new FileStorage(uploadDirectory);
        assertThrows(RuntimeException.class,
                () -> storage.save(part("wrong.jpg", pngImage(), "image/jpeg")));
    }

    private static byte[] pngImage() throws IOException {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }

    private static Part part(String filename, byte[] bytes, String contentType) {
        return new Part() {
            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(bytes);
            }

            @Override
            public String getContentType() {
                return contentType;
            }

            @Override
            public String getName() {
                return "image";
            }

            @Override
            public String getSubmittedFileName() {
                return filename;
            }

            @Override
            public long getSize() {
                return bytes.length;
            }

            @Override
            public void write(String fileName) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void delete() {
            }

            @Override
            public String getHeader(String name) {
                return null;
            }

            @Override
            public Collection<String> getHeaders(String name) {
                return List.of();
            }

            @Override
            public Collection<String> getHeaderNames() {
                return List.of();
            }
        };
    }
}
