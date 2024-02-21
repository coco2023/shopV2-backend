package com.UmiUni.shop.service;

import com.UmiUni.shop.dto.ForumPostDTO;
import com.UmiUni.shop.entity.ForumPost;
import org.springframework.data.domain.Page;

import java.awt.print.Pageable;
import java.util.List;

public interface ForumPostService {
    ForumPost saveForumPost(ForumPost forumPost);
    List<ForumPost> getAllForumPosts();
    ForumPost getForumPostById(Long id);
    ForumPost updateForumPost(Long id, ForumPost forumPost);
    void deleteForumPost(Long id);

    Page<ForumPostDTO> getAllForumPostsSortedByTimestampAsc(int page, int size);

    Page<ForumPostDTO> getAllForumPostsSortedByTimestampDesc(int page, int size);
}
