package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.dto.ForumPostDTO;
import com.UmiUni.shop.entity.ForumPost;
import com.UmiUni.shop.repository.ForumPostRepository;
import com.UmiUni.shop.service.ForumPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
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
    public Page<ForumPostDTO> getAllForumPostsSortedByTimestampAsc(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ForumPost> forumPostPage = forumPostRepository.findAllByOrderByPublishTimestampAsc(pageRequest);

        return forumPostPage.map(ForumPost::convertToForumPostDTO);
    }

    @Override
    public Page<ForumPostDTO> getAllForumPostsSortedByTimestampDesc(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ForumPost> forumPostPage = forumPostRepository.findAllByOrderByPublishTimestampDesc(pageRequest);

        return forumPostPage.map(ForumPost::convertToForumPostDTO);
    }
}
