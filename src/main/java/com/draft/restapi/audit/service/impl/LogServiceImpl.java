package com.draft.restapi.audit.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.stereotype.Service;

import com.draft.restapi.audit.document.AuditLogDocument;
import com.draft.restapi.audit.dto.AuditLogDto;
import com.draft.restapi.audit.dto.AuditLogFilter;
import com.draft.restapi.audit.mapper.AuditLogMapper;
import com.draft.restapi.audit.service.LogService;
import com.draft.restapi.common.payload.PageDto;

import java.util.stream.Collectors;

@Service
public class LogServiceImpl implements LogService {

    public static final Map<String, String> ORDERABLE_FIELD_MAP;
    static {
        Map<String, String> tempMap = new HashMap<>();
        tempMap.put("id", "_id");
        tempMap.put("entityName", "entityName.keyword");
        tempMap.put("entityId", "entityId");
        tempMap.put("operation", "operation.keyword");
        tempMap.put("operatorId", "operatorId");
        tempMap.put("operatorName", "operatorName.keyword");
        tempMap.put("traceId", "traceId.keyword");
        tempMap.put("timestamp", "timestamp");
        ORDERABLE_FIELD_MAP = Collections.unmodifiableMap(tempMap);
    }

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Override
    public PageDto<AuditLogDto> getAuditLogs(AuditLogFilter filter, Pageable pageable) {
        pageable = validateAndFixPageable(pageable);
        QueryBuilder filterQuery = buildQueryByFilter(filter);

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withPageable(pageable)
                .withQuery(filterQuery);

        Query query = queryBuilder.build();
        SearchHits<AuditLogDocument> searchHits = elasticsearchOperations.search(query, AuditLogDocument.class);
        List<AuditLogDocument> logs = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        List<AuditLogDto> dtoList = auditLogMapper.toDtoList(logs);

        Page<AuditLogDto> page = new PageImpl<>(dtoList, pageable, searchHits.getTotalHits());
        return new PageDto<>(page);
    }

    private QueryBuilder buildQueryByFilter(AuditLogFilter filter) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        if (filter.getId() != null) {
            boolQuery.must(QueryBuilders.termQuery("_id", filter.getId()));
        }
        if (filter.getEntityName() != null) {
            boolQuery.must(QueryBuilders.wildcardQuery("entityName", filter.getEntityName().toLowerCase()));
        }
        if (filter.getEntityId() != null) {
            boolQuery.must(QueryBuilders.termQuery("entityId", filter.getEntityId()));
        }
        if (filter.getOperation() != null) {
            boolQuery.must(QueryBuilders.wildcardQuery("operation", filter.getOperation().toLowerCase()));
        }
        if (filter.getOperatorId() != null) {
            boolQuery.must(QueryBuilders.termQuery("operatorId", filter.getOperatorId()));
        }
        if (filter.getOperatorName() != null) {
            boolQuery.must(QueryBuilders.wildcardQuery("operatorName", filter.getOperatorName().toLowerCase()));
        }
        if (filter.getTraceId() != null) {
            boolQuery.must(QueryBuilders.termQuery("traceId", filter.getTraceId()));
        }
        if (filter.getStartTime() != null) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("timestamp");
            rangeQuery.gte(filter.getStartTime());
            boolQuery.must(rangeQuery);
        }
        if (filter.getEndTime() != null) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("timestamp");
            rangeQuery.lte(filter.getEndTime());
            boolQuery.must(rangeQuery);
        }

        return boolQuery.hasClauses() ? boolQuery : QueryBuilders.matchAllQuery();
    }

    private Pageable validateAndFixPageable(Pageable pageable) {
        List<Sort.Order> esCompatibleOrders = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            if (!ORDERABLE_FIELD_MAP.containsKey(order.getProperty()))
                throw new PropertyReferenceException(order.getProperty(), ClassTypeInformation.from(AuditLogDocument.class), Collections.emptyList());
            esCompatibleOrders.add(new Sort.Order(order.getDirection(), ORDERABLE_FIELD_MAP.get(order.getProperty())));
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(esCompatibleOrders));
    }
}
