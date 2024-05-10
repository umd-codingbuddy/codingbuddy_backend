package com.codingbuddy.controllers.contact;

import com.codingbuddy.dto.contact.SendMessage;
import com.codingbuddy.services.contact.ContactService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contact")
@CrossOrigin
public class ContactController {

    private final ContactService contactService;

    @PostMapping("/sendmessage")
    public ResponseEntity<Object> sendMessage(@RequestBody SendMessage message){
        return contactService.sendMessage(message);
    }

    @GetMapping("getallmessages")
    public ResponseEntity<Object> getAllMessages(HttpServletRequest request) {
        String userEmail = request.getUserPrincipal().getName();
        return contactService.getAllMessages(userEmail);
    }
}
