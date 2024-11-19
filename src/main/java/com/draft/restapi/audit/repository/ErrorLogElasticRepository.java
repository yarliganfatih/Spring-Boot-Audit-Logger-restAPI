package com.draft.restapi.audit.repository;

import com.draft.restapi.audit.document.ErrorLogDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorLogElasticRepository extends ElasticsearchRepository<ErrorLogDocument, String> {

}
