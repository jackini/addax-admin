package com.wgzhao.addax.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T>
{
    private int code;
    private String message;
    private T data;

    // 静态方法构造统一返回结果
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    @Override
    public String toString() {
        return "CommResponseDto{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
