package com.UmiUni.shop.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class ForumPostDTO implements Serializable {

    private static final long serialVersionUID = 7727871091681128507L;

    private Long id;

    private Long forumTopicId;

    private Integer floor;

    private Long userId;

    private String content;

    private LocalDateTime publishTimestamp;

}
