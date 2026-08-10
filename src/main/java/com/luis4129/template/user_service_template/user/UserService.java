package com.luis4129.template.user_service_template.user;

import com.luis4129.template.user_service_template.common.exception.DuplicateEmailException;
import com.luis4129.template.user_service_template.common.exception.UserNotFoundException;
import com.luis4129.template.user_service_template.user.dto.CreateUserRequest;
import com.luis4129.template.user_service_template.user.dto.UpdateUserRequest;
import com.luis4129.template.user_service_template.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateEmailException(request.email());
        }
        User saved = userRepository.save(userMapper.toEntity(request));
        return userMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return userMapper.toResponse(findUserOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = findUserOrThrow(id);

        if (!user.getEmail().equalsIgnoreCase(request.email())
                && userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhoneNumber(request.phoneNumber());
        user.setStatus(request.status());

        return userMapper.toResponse(user);
    }

    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    private User findUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
