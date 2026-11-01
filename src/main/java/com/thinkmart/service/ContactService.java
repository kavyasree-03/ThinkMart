package com.thinkmart.service;

import com.thinkmart.dto.ContactRequest;
import com.thinkmart.model.ContactMessage;
import com.thinkmart.repository.ContactMessageRepository;
import org.springframework.stereotype.Service;

@Service
public class ContactService {

    private final ContactMessageRepository contactMessageRepository;

    public ContactService(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    public ContactMessage saveMessage(ContactRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Contact request is required.");
        }

        ContactMessage message = new ContactMessage();
        message.setName(request.getName().trim());
        message.setEmail(request.getEmail().trim());
        message.setMessage(request.getMessage().trim());

        return contactMessageRepository.save(message);
    }
}
