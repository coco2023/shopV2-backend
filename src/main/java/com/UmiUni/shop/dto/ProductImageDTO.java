package com.UmiUni.shop.dto;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class ProductImageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productId;

    private String fileName; // The name of the image file

    private String filePath; // The path to the image file on the server

    private Long fileSize;

}
