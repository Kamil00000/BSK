package com.example.chatservice;

import com.example.chatservice.dto.CreateChatRequest;
import com.example.chatservice.dto.SendMessageRequest;
import com.example.chatservice.dto.UserDTO;
import com.example.chatservice.models.Chat;
import com.example.chatservice.models.Message;
import com.example.chatservice.repositories.ChatRepository;
import com.example.chatservice.repositories.MessageRepository;
import com.example.chatservice.repositories.ChatParticipantRepository;
import com.example.chatservice.services.UserClient;
import com.example.chatservice.services.NotificationClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"eureka.client.enabled=false",
		"notification.service.url=http://localhost:9999",
		"user.service.url=http://localhost:9998",
		"jwt.secret=Zm9vYmFyYmF6cXV4eWp3ZWtydGh5dWlvcGFzZGZnaGprbA==",
		"chat.encryption.key=1234567890abcdefghijklmn"
})
@Transactional
public class ChatServiceApplicationTests {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ChatRepository chatRepository;

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private ChatParticipantRepository chatParticipantRepository;

	@MockBean
	private UserClient userClient;

	@MockBean
	private NotificationClient notificationClient;

	private MockMvc mockMvc;

	private final String secret = "Zm9vYmFyYmF6cXV4eWp3ZWtydGh5dWlvcGFzZGZnaGprbA==";
	private String userToken;
	private String user2Token;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders
				.webAppContextSetup(context)
				.apply(springSecurity())
				.build();

		// Przygotuj testowych użytkowników
		UserDTO user1 = new UserDTO();
		user1.setId(1L);
		user1.setUsername("testuser1");
		user1.setEmail("test1@test.com");
		user1.setRole("USER");

		UserDTO user2 = new UserDTO();
		user2.setId(2L);
		user2.setUsername("testuser2");
		user2.setEmail("test2@test.com");
		user2.setRole("USER");

		// Mock odpowiedzi UserClient
		when(userClient.getUserByUsername("testuser1")).thenReturn(user1);
		when(userClient.getUserByUsername("testuser2")).thenReturn(user2);

		// Wygeneruj tokeny JWT
		userToken = generateToken(1L, "testuser1", "USER");
		user2Token = generateToken(2L, "testuser2", "USER");

		// Mock NotificationClient
		doNothing().when(notificationClient).sendMessageNotification(anyLong(), anyString());
	}

	@Test
	void shouldCreateChatAndSendMessage() throws Exception {
		// 1. Utwórz czat
		CreateChatRequest chatRequest = new CreateChatRequest();
		chatRequest.setName("Test Chat");
		chatRequest.setDescription("Test description");

		String chatResponse = mockMvc.perform(post("/chat/create")
						.header("Authorization", "Bearer " + userToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(chatRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Czat utworzony pomyślnie"))
				.andReturn()
				.getResponse()
				.getContentAsString();

		// Pobierz ID utworzonego czatu
		Map<String, Object> response = objectMapper.readValue(chatResponse, Map.class);
		Long chatId = ((Number) response.get("chatId")).longValue();

		// Sprawdź czy czat został utworzony
		assertEquals(1, chatRepository.count());
		Chat chat = chatRepository.findById(chatId).orElse(null);
		assertNotNull(chat);
		assertEquals("Test Chat", chat.getName());

		// 2. Wyślij wiadomość
		SendMessageRequest messageRequest = new SendMessageRequest();
		messageRequest.setChatId(chatId);
		messageRequest.setContent("Hello, this is a test message!");

		mockMvc.perform(post("/chat/send")
						.header("Authorization", "Bearer " + userToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(messageRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Wiadomość wysłana"));

		// Sprawdź czy wiadomość została zapisana
		assertEquals(1, messageRepository.count());
		Message message = messageRepository.findAll().get(0);
		assertEquals("Hello, this is a test message!", message.getContent());
		assertEquals(chatId, message.getChatId());
		assertEquals(1L, message.getSenderId());

		// 3. Pobierz wiadomości z czatu
		mockMvc.perform(get("/chat/messages")
						.param("chatId", chatId.toString())
						.header("Authorization", "Bearer " + userToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].content").value("Hello, this is a test message!"));
	}

	@Test
	void shouldGetUserChats() throws Exception {
		// Utwórz kilka czatów
		createTestChat("Chat 1");
		createTestChat("Chat 2");

		mockMvc.perform(get("/chat/my-chats")
						.header("Authorization", "Bearer " + userToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	void shouldCreatePrivateChat() throws Exception {
		CreateChatRequest request = new CreateChatRequest();
		request.setName("Private Chat");
		request.setReceiverId(2L); // user2

		mockMvc.perform(post("/chat/create")
						.header("Authorization", "Bearer " + userToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk());

		// Sprawdź czy oba użytkownicy są uczestnikami
		assertEquals(2, chatParticipantRepository.count());
	}

	@Test
	void shouldNotAllowUnauthorizedAccess() throws Exception {
		mockMvc.perform(get("/chat/my-chats"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void shouldNotSendMessageToNonExistentChat() throws Exception {
		SendMessageRequest request = new SendMessageRequest();
		request.setChatId(999L);
		request.setContent("Test message");

		mockMvc.perform(post("/chat/send")
						.header("Authorization", "Bearer " + userToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	// Helper methods
	private void createTestChat(String name) throws Exception {
		CreateChatRequest request = new CreateChatRequest();
		request.setName(name);

		mockMvc.perform(post("/chat/create")
				.header("Authorization", "Bearer " + userToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));
	}

	private String generateToken(Long userId, String username, String role) {
		SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));

		Map<String, Object> claims = new HashMap<>();
		claims.put("userId", userId);
		claims.put("role", role);

		return Jwts.builder()
				.setClaims(claims)
				.setSubject(username)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 900000))
				.signWith(key)
				.compact();
	}
}