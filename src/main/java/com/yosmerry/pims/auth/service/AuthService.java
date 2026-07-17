package com.yosmerry.pims.auth.service;

import com.yosmerry.pims.auth.dto.RegisterRequest;
import com.yosmerry.pims.auth.dto.RegisterResponse;
import com.yosmerry.pims.common.exception.ApiValidationException;
import com.yosmerry.pims.common.util.TsidGenerator;
import com.yosmerry.pims.user.entity.User;
import com.yosmerry.pims.user.entity.UserStatus;
import com.yosmerry.pims.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCaseAndMarkForDeleteFalse(normalizedEmail)) {
            throw new ApiValidationException(Map.of("email", List.of("Duplicate")));
        }

        long currentTime = System.currentTimeMillis();
        User user = new User();
        user.setId(TsidGenerator.next());
        user.setCode(userRepository.nextCode());
        user.setName(request.getName().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedDate(currentTime);
        user.setCreatedBy(normalizedEmail);
        user.setUpdatedDate(currentTime);
        user.setUpdatedBy(normalizedEmail);

        User savedUser = userRepository.save(user);
        return new RegisterResponse(
                savedUser.getCode(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getStatus().name(),
                savedUser.getCreatedDate(),
                savedUser.getUpdatedDate()
        );
    }
}
