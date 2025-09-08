package com.techRestore.tech.restore.common.services.chat;


import com.techRestore.tech.restore.common.model.entities.ChatRoom;
import com.techRestore.tech.restore.common.repository.ChatRepository;
import com.techRestore.tech.restore.shop.repository.ShopRepository;
import com.techRestore.tech.restore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    public Optional<String> getChatRoomId(String senderEmail, String recipientEmail, boolean createNewRoomIfNotExists) {
        return chatRoomRepository.findBySenderEmailAndRecipientEmail(senderEmail, recipientEmail)
                .map(ChatRoom::getChatId)
                .or(() -> {
                    if (!createNewRoomIfNotExists) {
                        return Optional.empty();
                    }

                    boolean isSenderUser = userRepository.existsByEmail(senderEmail);
                    boolean isRecipientUser = userRepository.existsByEmail(recipientEmail);
                    boolean isSenderShop = shopRepository.existsByEmail(senderEmail);
                    boolean isRecipientShop = shopRepository.existsByEmail(recipientEmail);

                    if (!((isSenderUser && isRecipientShop) || (isSenderShop && isRecipientUser))) {
                        throw new IllegalArgumentException("Chat must be between GUEST and SHOP_OWNER");
                    }

                    String first = senderEmail.compareTo(recipientEmail) < 0 ? senderEmail : recipientEmail;
                    String second = senderEmail.compareTo(recipientEmail) < 0 ? recipientEmail : senderEmail;
                    String chatId = first + "_" + second;

                    ChatRoom senderRecipient = ChatRoom.builder()
                            .chatId(chatId)
                            .senderEmail(senderEmail)
                            .recipientEmail(recipientEmail)
                            .build();
                    chatRoomRepository.save(senderRecipient);

                    ChatRoom recipientSender = ChatRoom.builder()
                            .chatId(chatId)
                            .senderEmail(recipientEmail)
                            .recipientEmail(senderEmail)
                            .build();
                    chatRoomRepository.save(recipientSender);

                    return Optional.of(chatId);
                });
    }
  }

