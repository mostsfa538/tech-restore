package com.techRestore.tech.restore.common.controller.chat;

import com.techRestore.tech.restore.common.dto.chat.StatusDto;
import com.techRestore.tech.restore.common.services.chat.StatusService;
import com.techRestore.tech.restore.shop.dto.shop.ShopResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StatusController {
    private final StatusService statusService;

    @MessageMapping("/user.addUser")
    @SendTo("/user/public")
    @PreAuthorize("isAuthenticated()")
    public StatusDto addUser(@Payload StatusDto statusDto) {
        statusService.connect(statusDto.getEmail());
        return statusDto;
    }

    @MessageMapping("/user.disconnectUser")
    @SendTo("/user/public")
    @PreAuthorize("isAuthenticated()")
    public StatusDto disconnectUser(@Payload StatusDto statusDto) {
        statusService.disconnect(statusDto.getEmail());
        return statusDto;
    }

    @GetMapping("/shops/connected")
    @PreAuthorize("hasRole('GUEST')")
    public ResponseEntity<List<ShopResponseDto>> findConnectedShops() {
        return ResponseEntity.ok(statusService.findConnectedShops());
    }
}
