package com.codingbuddy.repository.contact;

import com.codingbuddy.models.contact.Contact;
import com.codingbuddy.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
    List<Contact> findByReceiver(User receiver);
}
