package com.draft.restapi.audit.repository;

import com.draft.restapi.audit.document.AuditLogDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogElasticRepository extends ElasticsearchRepository<AuditLogDocument, String> {

}
