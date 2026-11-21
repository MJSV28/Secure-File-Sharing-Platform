package com.example.secure_file_sharing.file;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.secure_file_sharing.user.AppUser;
import com.example.secure_file_sharing.user.AppUserRepository;
@Service
public class FileService {
	  @Autowired
	    private SharedFileRepository sharedFileRepository;

	    @Autowired
	    private AppUserRepository appUserRepository;

	    @Value("${app.file.storage-dir:uploads}")
	    private String storageDirectory;

	    @Value("${app.file.max-size-bytes:10485760}")
	    private long maxFileSize;

	    private Path getStoragePath() throws IOException {
	        Path path = Paths.get(storageDirectory)
	                .toAbsolutePath().normalize();

	        Files.createDirectories(path);
	        return path;
	    }

	    public Map<String, Object> upload(
	            MultipartFile file, String email) throws IOException {

	        if (file == null || file.isEmpty()) {
	            throw new ResponseStatusException(
	                    HttpStatus.BAD_REQUEST, "Select a file");
	        }

	        if (file.getSize() > maxFileSize) {
	        	  throw new ResponseStatusException(
	        	            HttpStatus.CONTENT_TOO_LARGE,
	        	            "Maximum file size is 10 MB");
	        }

	        String originalName = Paths.get(
	                file.getOriginalFilename() == null
	                        ? "file"
	                        : file.getOriginalFilename())
	                .getFileName().toString();

	        String lowerName = originalName.toLowerCase();

	        if (!(lowerName.endsWith(".pdf")
	                || lowerName.endsWith(".png")
	                || lowerName.endsWith(".jpg")
	                || lowerName.endsWith(".jpeg")
	                || lowerName.endsWith(".txt"))) {

	            throw new ResponseStatusException(
	                    HttpStatus.BAD_REQUEST,
	                    "Only PDF, PNG, JPG, JPEG and TXT files are allowed");
	        }

	        AppUser owner = appUserRepository.findByEmail(email)
	                .orElseThrow(() -> new ResponseStatusException(
	                        HttpStatus.UNAUTHORIZED, "User not found"));

	        String storedName = UUID.randomUUID().toString();
	        Path target = getStoragePath().resolve(storedName).normalize();

	        if (!target.startsWith(getStoragePath())) {
	            throw new ResponseStatusException(
	                    HttpStatus.BAD_REQUEST, "Invalid file path");
	        }

	        Files.copy(file.getInputStream(), target);

	        try {
	            SharedFile sharedFile = new SharedFile();
	            sharedFile.setOriginalFilename(originalName);
	            sharedFile.setStorageFilename(storedName);
	            sharedFile.setContentType(
	                    file.getContentType() == null
	                            ? "application/octet-stream"
	                            : file.getContentType());
	            sharedFile.setSize(file.getSize());
	            sharedFile.setUploadedAt(LocalDateTime.now());
	            sharedFile.setOwner(owner);

	            SharedFile saved = sharedFileRepository.save(sharedFile);

	            return toMap(saved);

	        } catch (RuntimeException ex) {
	            Files.deleteIfExists(target);
	            throw ex;
	        }
	    }

	    public List<Map<String, Object>> listFiles(String email) {
	        return sharedFileRepository
	                .findAllByOwner_EmailOrderByUploadedAtDesc(email)
	                .stream()
	                .map(this::toMap)
	                .toList();
	    }

	    public SharedFile getOwnedFile(Long id, String email) {
	        return sharedFileRepository.findByIdAndOwner_Email(id, email)
	                .orElseThrow(() -> new ResponseStatusException(
	                        HttpStatus.NOT_FOUND, "File not found"));
	    }

	    public Path getFilePath(SharedFile file) throws IOException {
	        Path base = getStoragePath();
	        Path target = base.resolve(file.getStorageFilename()).normalize();

	        if (!target.startsWith(base) || !Files.isRegularFile(target)) {
	            throw new ResponseStatusException(
	                    HttpStatus.NOT_FOUND, "Stored file not found");
	        }

	        return target;
	    }

	    public void delete(Long id, String email) throws IOException {
	        SharedFile file = getOwnedFile(id, email);
	        Path path = getFilePath(file);

	        Files.delete(path);
	        sharedFileRepository.delete(file);
	    }

	    private Map<String, Object> toMap(SharedFile file) {
	        return Map.of(
	                "id", file.getId(),
	                "originalFilename", file.getOriginalFilename(),
	                "contentType", file.getContentType(),
	                "size", file.getSize(),
	                "uploadedAt", file.getUploadedAt().toString()
	        );
	    }
}
