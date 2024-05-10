package com.codingbuddy.dto.contact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendMessage {
    private int senderId;
    private int receiverId;
    private int courseId;
    private String title;
    private String message;
}
