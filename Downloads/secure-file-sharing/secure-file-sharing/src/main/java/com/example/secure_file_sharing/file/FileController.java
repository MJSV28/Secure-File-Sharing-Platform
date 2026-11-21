package com.example.secure_file_sharing.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/files")
public class FileController {
	@Autowired
	private FileService fileService;

	@PostMapping("/upload")
	public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file,
			Authentication authentication) throws IOException {

		Map<String, Object> result = fileService.upload(file, authentication.getName());

		return ResponseEntity.ok(result);
	}

	@GetMapping
	public ResponseEntity<List<Map<String, Object>>> getFiles(Authentication authentication) {

		return ResponseEntity.ok(fileService.listFiles(authentication.getName()));
	}

	@GetMapping("/{id}/download")
	public ResponseEntity<Resource> downloadFile(@PathVariable Long id, Authentication authentication,
			HttpServletRequest request) throws IOException {

		SharedFile file = fileService.getOwnedFile(id, authentication.getName());

		Path path = fileService.getFilePath(file);
		Resource resource = new UrlResource(path.toUri());

		String contentType = Files.probeContentType(path);

		if (contentType == null) {
			contentType = "application/octet-stream";
		}

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION,
						ContentDisposition.attachment().filename(file.getOriginalFilename()).build().toString())
				.body(resource);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteFile(@PathVariable Long id, Authentication authentication) throws IOException {

		fileService.delete(id, authentication.getName());

		return ResponseEntity.noContent().build();
	}
}
