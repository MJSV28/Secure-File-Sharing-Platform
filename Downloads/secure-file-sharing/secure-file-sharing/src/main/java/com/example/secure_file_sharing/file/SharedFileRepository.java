package com.example.secure_file_sharing.file;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SharedFileRepository  extends JpaRepository<SharedFile,Long>{
	   List<SharedFile> findAllByOwner_EmailOrderByUploadedAtDesc(
	            String email);

	    Optional<SharedFile> findByIdAndOwner_Email(
	            Long id, String email);

}
