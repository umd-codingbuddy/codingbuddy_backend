package com.codingbuddy.repository.course;

import com.codingbuddy.models.courses.Module;
import com.codingbuddy.models.courses.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PageRepository extends JpaRepository<Page, Integer> {

    Page findByPageId(int pageId);
    List<Page> findByModule(Module module);

    @Query(value = "SELECT * from Page where module_module_id=1 ORDER BY sequence_number ASC", nativeQuery = true)
    List<Page> findByModuleIdSorted(int moduleId);
}
