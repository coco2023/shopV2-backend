package com.UmiUni.shop.controller;

import com.UmiUni.shop.entity.ForumPost;
import com.UmiUni.shop.service.ForumPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/forumPosts")
public class ForumPostController {

    private final ForumPostService forumPostService;

    @Autowired
    public ForumPostController(ForumPostService forumPostService) {
        this.forumPostService = forumPostService;
    }

    @PostMapping
    public ResponseEntity<ForumPost> createForumPost(@RequestBody ForumPost forumPost) {
        return ResponseEntity.ok(forumPostService.saveForumPost(forumPost));
    }

    @GetMapping("/desc")
    public List<ForumPost> getAllForumPosts() {
        return forumPostService.getAllForumPosts();
    }

    @GetMapping()
    public List<ForumPost> getAllForumPostsASEC() {
        return forumPostService.getAllForumPostsSortedByTimestampAsc();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ForumPost> getForumPostById(@PathVariable Long id) {
        return ResponseEntity.ok(forumPostService.getForumPostById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ForumPost> updateForumPost(@PathVariable Long id, @RequestBody ForumPost forumPost) {
        return ResponseEntity.ok(forumPostService.updateForumPost(id, forumPost));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteForumPost(@PathVariable Long id) {
        forumPostService.deleteForumPost(id);
        return ResponseEntity.ok("Forum post deleted successfully!");
    }
}
