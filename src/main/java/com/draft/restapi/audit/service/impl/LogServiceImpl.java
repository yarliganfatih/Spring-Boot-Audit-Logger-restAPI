package com.draft.restapi.audit.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import com.draft.restapi.audit.document.FieldChange;
import com.draft.restapi.audit.document.ErrorLogDocument;
import com.draft.restapi.audit.dto.AuditLogDto;
import com.draft.restapi.audit.dto.AuditLogFilter;
import com.draft.restapi.audit.dto.ErrorLogDto;
import com.draft.restapi.audit.dto.ErrorLogFilter;
import com.draft.restapi.audit.dto.UpdateHistoryDto;
import com.draft.restapi.audit.mapper.AuditLogMapper;
import com.draft.restapi.audit.mapper.ErrorLogMapper;
import com.draft.restapi.audit.service.LogService;
import com.draft.restapi.common.payload.PageDto;

import java.util.stream.Collectors;

@Service
public class LogServiceImpl implements LogService {

    public static final Map<String, String> ORDERABLE_FIELD_MAP;
    public static final Map<String, String> ERROR_ORDERABLE_FIELD_MAP;
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

        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("id", "_id");
        errorMap.put("endpointUrl", "endpointUrl.keyword");
        errorMap.put("httpMethod", "httpMethod.keyword");
        errorMap.put("errorType", "errorType.keyword");
        errorMap.put("httpStatusCode", "httpStatusCode");
        errorMap.put("traceId", "traceId.keyword");
        errorMap.put("occurredById", "occurredById");
        errorMap.put("occurredByUsername", "occurredByUsername.keyword");
        errorMap.put("timestamp", "timestamp");
        ERROR_ORDERABLE_FIELD_MAP = Collections.unmodifiableMap(errorMap);
    }

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Autowired
    private ErrorLogMapper errorLogMapper;

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

    @Override
    public PageDto<ErrorLogDto> getErrorLogs(ErrorLogFilter filter, Pageable pageable) {
        pageable = validateAndFixPageableForErrorLog(pageable);
        QueryBuilder filterQuery = buildErrorQueryByFilter(filter);

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withPageable(pageable)
                .withQuery(filterQuery);

        Query query = queryBuilder.build();
        SearchHits<ErrorLogDocument> searchHits = elasticsearchOperations.search(query, ErrorLogDocument.class);
        List<ErrorLogDocument> logs = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        List<ErrorLogDto> dtoList = errorLogMapper.toDtoList(logs);

        Page<ErrorLogDto> page = new PageImpl<>(dtoList, pageable, searchHits.getTotalHits());
        return new PageDto<>(page);
    }

    @Override
    public PageDto<UpdateHistoryDto> getEntityFieldUpdateLogs(Pageable pageable, String entityName, Integer entityId, String fieldName) {
        pageable = validateAndFixPageable(pageable, Sort.by(Sort.Order.desc("timestamp")));
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.wildcardQuery("entityName", entityName.toLowerCase()))
                .must(QueryBuilders.termQuery("entityId", entityId))
                .must(QueryBuilders.wildcardQuery("changes.fieldName.keyword", fieldName.toLowerCase()));

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(pageable);

        SearchHits<AuditLogDocument> searchHits = elasticsearchOperations.search(queryBuilder.build(), AuditLogDocument.class);

        List<UpdateHistoryDto> dtoList = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(doc -> mapToUpdateHistoryDto(fieldName, doc))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Page<UpdateHistoryDto> page = new PageImpl<>(dtoList, pageable, searchHits.getTotalHits());
        return new PageDto<>(page);
    }

    private UpdateHistoryDto mapToUpdateHistoryDto(String fieldName, AuditLogDocument doc) {
        FieldChange specificChange = doc.getChanges().stream()
                .filter(change -> fieldName.equals(change.getFieldName()))
                .findFirst()
                .orElse(null);
        if (specificChange == null) return null;
        return auditLogMapper.toUpdateHistoryDto(doc, specificChange.getPreviousValue());
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
    private QueryBuilder buildErrorQueryByFilter(ErrorLogFilter filter) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        if (filter.getId() != null) {
            boolQuery.must(QueryBuilders.termQuery("_id", filter.getId()));
        }
        if (filter.getEndpointUrl() != null) {
            boolQuery.must(QueryBuilders.wildcardQuery("endpointUrl", filter.getEndpointUrl().toLowerCase()));
        }
        if (filter.getHttpMethod() != null) {
            boolQuery.must(QueryBuilders.wildcardQuery("httpMethod", filter.getHttpMethod().toLowerCase()));
        }
        if (filter.getErrorType() != null) {
            boolQuery.must(QueryBuilders.wildcardQuery("errorType", filter.getErrorType().toLowerCase()));
        }
        if (filter.getHttpStatusCode() != null) {
            boolQuery.must(QueryBuilders.termQuery("httpStatusCode", filter.getHttpStatusCode()));
        }
        if (filter.getTraceId() != null) {
            boolQuery.must(QueryBuilders.termQuery("traceId", filter.getTraceId()));
        }
        if (filter.getOccurredById() != null) {
            boolQuery.must(QueryBuilders.termQuery("occurredById", filter.getOccurredById()));
        }
        if (filter.getOccurredByUsername() != null) {
            boolQuery.must(QueryBuilders.wildcardQuery("occurredByUsername", filter.getOccurredByUsername().toLowerCase()));
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

    private Pageable validateAndFixPageableForErrorLog(Pageable pageable) {
        List<Sort.Order> esCompatibleOrders = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            if (!ERROR_ORDERABLE_FIELD_MAP.containsKey(order.getProperty()))
                throw new PropertyReferenceException(order.getProperty(), ClassTypeInformation.from(ErrorLogDocument.class), Collections.emptyList());
            esCompatibleOrders.add(new Sort.Order(order.getDirection(), ERROR_ORDERABLE_FIELD_MAP.get(order.getProperty())));
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(esCompatibleOrders));
    }

    private Pageable validateAndFixPageable(Pageable pageable, Sort defaultSort) {
        Pageable pageableWithDefaultSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), defaultSort);
        return validateAndFixPageable(pageableWithDefaultSort);
    }
}
