package com.draft.restapi.common.payload;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private PageDto<T> page;
    private LocalDateTime timestamp;
    private List<ValidationError> validationErrors;

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, null, data);
    }

    @SuppressWarnings({"rawtypes", "unchecked"}) // to overcome the generics restriction
    public static <T> ApiResponse<T> success(PageDto<T> pageDto) {
        ApiResponse response = new ApiResponse(true, null, pageDto.getContent());
        pageDto.setContent(null);
        response.setPage(pageDto);
        return response;
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
