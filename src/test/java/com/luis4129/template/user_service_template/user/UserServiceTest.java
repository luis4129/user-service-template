package com.luis4129.template.user_service_template.user;

import com.luis4129.template.user_service_template.common.exception.DuplicateEmailException;
import com.luis4129.template.user_service_template.common.exception.UserNotFoundException;
import com.luis4129.template.user_service_template.user.dto.CreateUserRequest;
import com.luis4129.template.user_service_template.user.dto.UpdateUserRequest;
import com.luis4129.template.user_service_template.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private final UserMapper userMapper = new UserMapper();

    private UserService userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userMapper);
        existingUser = User.builder()
                .id(UUID.randomUUID())
                .email("jane.doe@example.com")
                .firstName("Jane")
                .lastName("Doe")
                .phoneNumber("+1 555 0100")
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void create_savesUser_whenEmailIsUnique() {
        CreateUserRequest request = new CreateUserRequest(null, "new.user@example.com", "New", "User", null);
        when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.create(request);

        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.firstName()).isEqualTo(request.firstName());
        assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void create_throws_whenEmailAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest(null, existingUser.getEmail(), "Jane", "Doe", null);
        when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(DuplicateEmailException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void getById_returnsUser_whenFound() {
        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));

        UserResponse response = userService.getById(existingUser.getId());

        assertThat(response.id()).isEqualTo(existingUser.getId());
        assertThat(response.email()).isEqualTo(existingUser.getEmail());
    }

    @Test
    void getById_throws_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(id))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getAll_returnsMappedUsers() {
        when(userRepository.findAll()).thenReturn(List.of(existingUser));

        List<UserResponse> responses = userService.getAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(existingUser.getId());
    }

    @Test
    void update_modifiesUser_whenEmailUnchanged() {
        UpdateUserRequest request = new UpdateUserRequest(
                existingUser.getEmail(), "Janet", "Doe", "+1 555 0101", UserStatus.INACTIVE);
        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));

        UserResponse response = userService.update(existingUser.getId(), request);

        assertThat(response.firstName()).isEqualTo("Janet");
        assertThat(response.status()).isEqualTo(UserStatus.INACTIVE);
        verify(userRepository, never()).existsByEmailIgnoreCase(any());
    }

    @Test
    void update_throws_whenNewEmailAlreadyTaken() {
        UpdateUserRequest request = new UpdateUserRequest(
                "taken@example.com", "Janet", "Doe", null, UserStatus.ACTIVE);
        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailIgnoreCase("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.update(existingUser.getId(), request))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void update_throws_whenUserNotFound() {
        UUID id = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest("a@example.com", "A", "B", null, UserStatus.ACTIVE);
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(id, request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void delete_removesUser_whenExists() {
        when(userRepository.existsById(existingUser.getId())).thenReturn(true);

        userService.delete(existingUser.getId());

        verify(userRepository, times(1)).deleteById(existingUser.getId());
    }

    @Test
    void delete_throws_whenUserDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(userRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(id))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).deleteById(any());
    }
}
