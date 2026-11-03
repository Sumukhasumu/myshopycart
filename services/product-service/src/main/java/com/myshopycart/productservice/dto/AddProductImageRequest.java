package com.myshopycart.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AddProductImageRequest {

    @NotBlank(message = "Image URL is required")
    @Pattern(
            regexp = "https?://.+",
            message = "Image URL must start with http:// or https://"
    )
    private String imageUrl;

    public AddProductImageRequest() {
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}