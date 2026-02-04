package com.hiyuan.demo1.controller;

import com.hiyuan.demo1.dto.QaRequest;
import com.hiyuan.demo1.dto.QaResponse;
import com.hiyuan.demo1.dto.response.ApiResponse;
import com.hiyuan.demo1.service.QaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 问答控制器
 */
@Slf4j
@RestController
@RequestMapping("/qa")
@RequiredArgsConstructor
public class QaController {

    private final QaService qaService;

    /**
     * 提问接口
     */
    @PostMapping("/ask")
    public ApiResponse<QaResponse> ask(@Valid @RequestBody QaRequest request) {
        log.info("收到问答请求: {}", request.getQuestion());
        
        try {
            QaResponse response = qaService.ask(request);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("问答失败: {}", e.getMessage(), e);
            return ApiResponse.serverError("问答失败: " + e.getMessage());
        }
    }
}
