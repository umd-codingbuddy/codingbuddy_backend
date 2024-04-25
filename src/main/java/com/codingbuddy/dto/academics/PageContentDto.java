package com.codingbuddy.dto.academics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageContentDto {
    private int pageId;
    private Map<Object, Object> pageContent;
}
