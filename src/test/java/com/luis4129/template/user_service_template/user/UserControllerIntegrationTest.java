package com.luis4129.template.user_service_template.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luis4129.template.user_service_template.support.AbstractIntegrationTest;
import com.luis4129.template.user_service_template.user.dto.CreateUserRequest;
import com.luis4129.template.user_service_template.user.dto.UpdateUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class UserControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void create_returns201_andLocationHeader() throws Exception {
        CreateUserRequest request = new CreateUserRequest(null, "alice@example.com", "Alice", "Smith", "+1 555 0111");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void create_returns400_whenEmailInvalid() throws Exception {
        CreateUserRequest request = new CreateUserRequest(null, "not-an-email", "Alice", "Smith", null);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void create_returns409_whenEmailAlreadyExists() throws Exception {
        CreateUserRequest request = new CreateUserRequest(null, "bob@example.com", "Bob", "Jones", null);
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getById_returns404_whenUserMissing() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", containsString("not found")));
    }

    @Test
    void getById_returns404_localizedToPortuguese_whenAcceptLanguageIsPtBr() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", UUID.randomUUID())
                        .header("Accept-Language", "pt-BR"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", containsString("não encontrado")));
    }

    @Test
    void create_returns400_localizedToPortuguese_whenAcceptLanguageIsPtBr() throws Exception {
        CreateUserRequest request = new CreateUserRequest(null, "not-an-email", "Alice", "Smith", null);

        mockMvc.perform(post("/api/v1/users")
                        .header("Accept-Language", "pt-BR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Falha de validação em um ou mais campos"))
                .andExpect(jsonPath("$.errors.email").value("O e-mail deve ter um formato válido"));
    }

    @Test
    void create_returns400_withUnsupportedAcceptLanguage_fallsBackToEnglish() throws Exception {
        CreateUserRequest request = new CreateUserRequest(null, "not-an-email", "Alice", "Smith", null);

        mockMvc.perform(post("/api/v1/users")
                        .header("Accept-Language", "fr-FR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("Email must be a well-formed address"));
    }

    @Test
    void fullLifecycle_createReadUpdateDelete() throws Exception {
        CreateUserRequest createRequest =
                new CreateUserRequest(null, "carol@example.com", "Carol", "White", null);

        String createBody = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID id = UUID.fromString(objectMapper.readTree(createBody).get("id").asText());

        mockMvc.perform(get("/api/v1/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Carol"));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "carol@example.com", "Carol", "Green", "+1 555 0122", UserStatus.INACTIVE);

        mockMvc.perform(put("/api/v1/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Green"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        mockMvc.perform(delete("/api/v1/users/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/users/{id}", id))
                .andExpect(status().isNotFound());
    }
}
