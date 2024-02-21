package com.UmiUni.shop.entity;

import com.UmiUni.shop.dto.ForumPostDTO;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
@Table(name = "forum_posts")
public class ForumPost implements Serializable {

    private static final long serialVersionUID = 7727871091681128507L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "forum_topic_id", nullable = false)
    private Long forumTopicId;

    @Column(nullable = false)
    private Integer floor;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "publish_timestamp", nullable = false, updatable = false)
    private LocalDateTime publishTimestamp;
    public ForumPostDTO convertToForumPostDTO() {
        ForumPostDTO dto = new ForumPostDTO();
        dto.setId(this.id);
        dto.setForumTopicId(this.forumTopicId);
        dto.setFloor(this.floor);
        dto.setUserId(this.userId);
        dto.setContent(this.content);
        dto.setPublishTimestamp(this.publishTimestamp);
        return dto;
    }

}