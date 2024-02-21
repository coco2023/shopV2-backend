package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.entity.ForumPost;
import com.UmiUni.shop.repository.ForumPostRepository;
import com.UmiUni.shop.service.ForumPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ForumPostServiceImpl implements ForumPostService {

    private final ForumPostRepository forumPostRepository;

    @Autowired
    public ForumPostServiceImpl(ForumPostRepository forumPostRepository) {
        this.forumPostRepository = forumPostRepository;
    }

    @Override
    public ForumPost saveForumPost(ForumPost forumPost) {
        return forumPostRepository.save(forumPost);
    }

    @Override
    public List<ForumPost> getAllForumPosts() {
        return forumPostRepository.findAll();
    }

    @Override
    public ForumPost getForumPostById(Long id) {
        Optional<ForumPost> forumPost = forumPostRepository.findById(id);
        if(forumPost.isPresent()) {
            return forumPost.get();
        } else {
            throw new RuntimeException("Post not found for id :: " + id);
        }
    }

    @Override
    public ForumPost updateForumPost(Long id, ForumPost forumPostDetails) {
        ForumPost forumPost = getForumPostById(id);
        forumPost.setContent(forumPostDetails.getContent());
        // Set other properties
        return forumPostRepository.save(forumPost);
    }

    @Override
    public void deleteForumPost(Long id) {
        forumPostRepository.deleteById(id);
    }

    @Override
    public List<ForumPost> getAllForumPostsSortedByTimestampAsc() {
        return forumPostRepository.findAll(Sort.by(Sort.Direction.ASC, "publishTimestamp"));
    }
}
