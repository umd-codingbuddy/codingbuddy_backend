package com.codingbuddy.dto.academics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModuleDetail {
    private String name;
    private int moduleId;
    private Long sequenceNumber;
    private List<PageDetail> pages;
}
