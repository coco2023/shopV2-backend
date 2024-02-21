package com.UmiUni.shop.service;

import com.UmiUni.shop.entity.ForumPost;

import java.util.List;

public interface ForumPostService {
    ForumPost saveForumPost(ForumPost forumPost);
    List<ForumPost> getAllForumPosts();
    ForumPost getForumPostById(Long id);
    ForumPost updateForumPost(Long id, ForumPost forumPost);
    void deleteForumPost(Long id);

    List<ForumPost> getAllForumPostsSortedByTimestampAsc();
}
