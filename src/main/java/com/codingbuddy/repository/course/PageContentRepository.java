package com.codingbuddy.repository.course;

import com.codingbuddy.models.courses.PageContent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PageContentRepository extends MongoRepository<PageContent, String> {
}
