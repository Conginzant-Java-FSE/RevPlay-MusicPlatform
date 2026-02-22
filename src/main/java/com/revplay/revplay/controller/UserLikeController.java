package com.revplay.revplay.controller;

import com.revplay.revplay.dto.request.UserLikeCreateRequest;
import com.revplay.revplay.dto.response.UserLikeResponse;
import com.revplay.revplay.service.UserLikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-likes")
@RequiredArgsConstructor
public class UserLikeController {

    private final UserLikeService service;

    @PostMapping
    public UserLikeResponse like(@Valid @RequestBody UserLikeCreateRequest request) {
        return service.likeSong(request);
    }

    @DeleteMapping
    public void unlike(@RequestParam Long userId,
                       @RequestParam Long songId) {
        service.unlikeSong(userId, songId);
    }

    @GetMapping("/user/{userId}")
    public List<UserLikeResponse> getUserLikes(@PathVariable Long userId) {
        return service.getUserLikes(userId);
    }
}