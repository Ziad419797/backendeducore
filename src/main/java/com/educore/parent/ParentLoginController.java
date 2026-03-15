package com.educore.parent;

import com.educore.dto.request.ParentCompleteLoginRequest;
import com.educore.dto.request.ParentStartLoginRequest;
import com.educore.dto.response.ParentCompleteLoginResponse;
import com.educore.dto.response.ParentStartLoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/parent")
@RequiredArgsConstructor
public class ParentLoginController {

    private final ParentLoginService parentLoginService;

    @PostMapping("/start-login")
    public ParentStartLoginResponse startLogin(@RequestBody @Valid ParentStartLoginRequest request) {
        return parentLoginService.startLogin(request);
    }

    @PostMapping("/complete-login")
    public ParentCompleteLoginResponse completeLogin(@RequestBody @Valid ParentCompleteLoginRequest request) {
        return parentLoginService.completeLogin(request);
    }
}
