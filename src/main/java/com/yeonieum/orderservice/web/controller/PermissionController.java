package com.yeonieum.orderservice.web.controller;

import com.yeonieum.orderservice.global.auth.RoleMetaDataCollector;
import com.yeonieum.orderservice.global.responses.ApiResponse;
import com.yeonieum.orderservice.global.responses.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

// 롤메타데이터수집기의 대상에서 제외되기 위해 @Controller 어노테이션
@Controller
@RequiredArgsConstructor
public class PermissionController {
    private final RoleMetaDataCollector roleMetaDataCollector;

    @GetMapping("/api/permissions")
    public ResponseEntity<?> retrieveProductReviews() {
        return new ResponseEntity<>(roleMetaDataCollector.getRoleMetadata(), HttpStatus.OK);
    }
}
