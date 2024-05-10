package com.codingbuddy.services.contact;

import com.codingbuddy.dto.contact.GetAllMessages;
import com.codingbuddy.dto.contact.SendMessage;
import com.codingbuddy.dto.course.GetAllCourses;
import com.codingbuddy.dto.user.GetUser;
import com.codingbuddy.models.contact.Contact;
import com.codingbuddy.models.courses.Course;
import com.codingbuddy.models.user.User;
import com.codingbuddy.repository.contact.ContactRepository;
import com.codingbuddy.repository.course.CourseRepository;
import com.codingbuddy.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public ResponseEntity<Object> sendMessage(SendMessage message){
        try{
            User sender = userRepository.findById(message.getSenderId())
                    .orElseThrow(() -> new Exception("Sender not found"));

            User receiver = userRepository.findById(message.getReceiverId())
                    .orElseThrow(() -> new Exception("Receiver not found"));

            Course course = courseRepository.findById(message.getCourseId())
                    .orElseThrow(() -> new Exception("Course not found"));

            Contact contact = Contact.builder()
                    .sender(sender)
                    .receiver(receiver)
                    .course(course)
                    .title(message.getTitle())
                    .message(message.getMessage())
                    .build();
            contactRepository.save(contact);
            return ResponseEntity.ok().body(Map.of("status", 1));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }

    public ResponseEntity<Object> getAllMessages(String userEmail){
        try{
            User receiver = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new Exception("Receiver not found"));

            List<GetAllMessages> messagesList = new ArrayList<>();

            List<Contact> messages = contactRepository.findByReceiver(receiver);

            for(Contact message:messages){
                Course course = message.getCourse();
                GetAllCourses courseDto = GetAllCourses.builder()
                        .name(course.getName())
                        .tags(course.getTags())
                        .description(course.getDescription())
                        .id(course.getCourseId())
                        .difficultyLevel(course.getDifficultyLevel())
                        .build();

                User sender = message.getSender();

                GetUser user = GetUser.builder()
                        .firstname(sender.getFirstName())
                        .id(sender.getUser_id())
                        .lastname(sender.getLastName())
                        .build();

                GetAllMessages contact = GetAllMessages.builder()
                        .course(courseDto)
                        .createdAt(message.getJoinedAt().getTime())
                        .student(user)
                        .title(message.getTitle())
                        .message(message.getMessage())
                        .build();

                messagesList.add(contact);
            }
            return ResponseEntity.ok().body(Map.of("status", 1, "messages", messagesList));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 2, "errorMsg", e.getMessage()));
        }
    }
}
