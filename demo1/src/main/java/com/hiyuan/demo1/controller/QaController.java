package com.hiyuan.demo1.controller;

import com.hiyuan.demo1.dto.QaRequest;
import com.hiyuan.demo1.dto.QaResponse;
import com.hiyuan.demo1.dto.response.ApiResponse;
import com.hiyuan.demo1.service.QaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

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

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(
            @RequestParam String question,
            @RequestParam(required = false) String previousQuestion,
            @RequestParam(required = false) String previousAnswer,
            @RequestParam(required = false) Integer topK,
            @RequestParam(value = "access_token", required = false) String ignoredToken // 仅用于通过JWT过滤器
    ) {
        QaRequest request = new QaRequest();
        request.setQuestion(question);
        request.setPreviousQuestion(previousQuestion);
        request.setPreviousAnswer(previousAnswer);
        if (topK != null) {
            request.setTopK(topK);
        }
        return qaService.streamAnswer(request);
    }
}
