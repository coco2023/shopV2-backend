package com.UmiUni.shop.repository;

import com.UmiUni.shop.entity.ForumPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
    List<ForumPost> findAll(Sort sort);

    Page<ForumPost> findAllByOrderByPublishTimestampAsc(PageRequest pageable);

    Page<ForumPost> findAllByOrderByPublishTimestampDesc(PageRequest pageRequest);
}
