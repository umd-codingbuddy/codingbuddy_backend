package com.codingbuddy.models.courses;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Builder
@Document
public class PageContent {
    @Id
    private String id;
    @Indexed(unique = true)
    private int pageId;
    private Map<Object, Object> pageContent;
}
