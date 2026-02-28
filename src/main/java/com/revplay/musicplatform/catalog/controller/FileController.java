package com.revplay.musicplatform.catalog.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.revplay.musicplatform.common.constants.ApiPaths;
import com.revplay.musicplatform.common.response.ApiResponse;
import com.revplay.musicplatform.catalog.dto.response.ImageUploadResponse;
import com.revplay.musicplatform.catalog.util.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaTypeFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(ApiPaths.FILES)
@Tag(name = "Files", description = "Media streaming endpoints")
public class FileController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);
    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/songs/{fileName}")
    @Operation(summary = "Stream song audio by file name")
    public ResponseEntity<Resource> getSong(@PathVariable String fileName) {
        Resource resource = fileStorageService.loadSong(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @GetMapping("/podcasts/{fileName}")
    @Operation(summary = "Stream podcast audio by file name")
    public ResponseEntity<Resource> getPodcast(@PathVariable String fileName) {
        Resource resource = fileStorageService.loadPodcast(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @GetMapping("/images/{fileName}")
    @Operation(summary = "Fetch image by file name")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
        LOGGER.debug("Serving image file {}", fileName);
        Resource resource = fileStorageService.loadImage(fileName);
        MediaType imageMediaType = MediaTypeFactory.getMediaType(fileName)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(imageMediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload image file (.jpg, .jpeg, .png)")
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadImage(@RequestPart("file") MultipartFile file) {
        LOGGER.info("Uploading image file via /api/v1/files/images");
        String storedFileName = fileStorageService.storeImage(file);
        String imageUrl = ApiPaths.FILES + "/images/" + storedFileName;
        ImageUploadResponse response = new ImageUploadResponse(storedFileName, imageUrl);
        return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully", response));
    }
}
