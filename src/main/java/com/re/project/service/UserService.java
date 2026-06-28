package com.re.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.re.project.dto.UserDetailsDto;
import com.re.project.dto.UserProfileDto;
import com.re.project.dto.request.UserUpdateRequest;
import com.re.project.dto.response.UserUpdateResponse;
import com.re.project.entity.User;
import com.re.project.exception.BadRequestException;
import com.re.project.exception.ResourceNotFoundException;
import com.re.project.mapper.UserMapper;
import com.re.project.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<UserDetailsDto> getUsers(String role) {
        List<User> entities = userRepository.findAll();
        
        return entities.stream()
                .filter(user -> role == null || user.getUserRoles().stream()
                        .anyMatch(ur -> ur.getRole().getName().equalsIgnoreCase("ROLE_" + role)))
                .map(userMapper::toUserDetailsDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserProfileDto getUser(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toUserProfileDto)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
    }

    public UserUpdateResponse updateUser(Long id, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không thể cập nhật. User ID " + id + " không tồn tại"));

        user.setFullName(userUpdateRequest.getFullName());
        user.setEmail(userUpdateRequest.getEmail());
        user.setPhone(userUpdateRequest.getPhone());
        user.setAddress(userUpdateRequest.getAddress());

        User savedUser = userRepository.save(user);
        return userMapper.toUserUpdateResponse(savedUser);
    }

    public void removeRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        boolean removed = user.getUserRoles().removeIf(ur -> ur.getRole().getId().equals(roleId));

        if (!removed) {
            throw new BadRequestException("Người dùng không có vai trò (Role ID: " + roleId + ") này");
        }

        User updatedUser = userRepository.save(user);
        userMapper.toUserUpdateResponse(updatedUser);
    }
}
