package com.draft.restapi.audit.repository;

import com.draft.restapi.audit.document.AuditLogDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogElasticRepository extends ElasticsearchRepository<AuditLogDocument, String> {
    
    List<AuditLogDocument> findByEntityNameAndEntityIdOrderByTimestampDesc(String entityName, Integer entityId);

}
