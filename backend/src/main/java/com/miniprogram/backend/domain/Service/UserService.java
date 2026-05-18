package com.miniprogram.backend.domain.Service;

import com.miniprogram.backend.common.BusinessException;
import com.miniprogram.backend.common.PageResponse;
import com.miniprogram.backend.domain.Entity.User;
import com.miniprogram.backend.domain.Entity.UserDTO;
import com.miniprogram.backend.domain.DAO.UserRepository;
import com.miniprogram.backend.domain.Mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务类
 * 处理用户相关的业务逻辑
 */
@Slf4j
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserMapper userMapper;
    
    // ==================== JPA 方法（简单 CRUD）====================
    
    /**
     * 根据 ID 获取用户
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        log.debug("Fetching user by id: {}", id);
        return userRepository.findById(id)
                .filter(user -> user.getStatus() == 1) // 只返回正常状态的用户
                .map(UserDTO::new)
                .orElseThrow(() -> new BusinessException(404, "User not found with id: " + id));
    }
    
    /**
     * 根据 ID 获取用户实体
     */
    @Transactional(readOnly = true)
    public User getUserEntityById(Long id) {
        log.debug("Fetching user entity by id: {}", id);
        return userRepository.findById(id).orElse(null);
    }
    
    /**
     * 根据 OpenID 获取用户
     */
    @Transactional(readOnly = true)
    public UserDTO getUserByOpenid(String openid) {
        log.debug("Fetching user by openid: {}", openid);
        if (!StringUtils.hasText(openid)) {
            throw new BusinessException(400, "OpenID cannot be empty");
        }
        return userRepository.findByOpenid(openid)
                .filter(user -> user.getStatus() == 1) // 只返回正常状态的用户
                .map(UserDTO::new)
                .orElseThrow(() -> new BusinessException(404, "User not found with openid: " + openid));
    }
    
    /**
     * 根据 OpenID 获取用户实体
     */
    @Transactional(readOnly = true)
    public User getUserEntityByOpenid(String openid) {
        log.debug("Fetching user entity by openid: {}", openid);
        if (!StringUtils.hasText(openid)) {
            return null;
        }
        return userRepository.findByOpenid(openid).orElse(null);
    }
    
    /**
     * 创建新用户
     */
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        log.info("Creating new user with openid: {}", userDTO.getOpenid());
        
        // 参数校验
        validateUser(userDTO, true);
        
        // 检查 openid 是否已存在
        if (userRepository.findByOpenid(userDTO.getOpenid()).isPresent()) {
            throw new BusinessException(400, "User with this openid already exists");
        }
        
        // 转换为 Entity
        User user = userDTO.toEntity();
        
        // 保存并返回 DTO
        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {}", savedUser.getId());
        return new UserDTO(savedUser);
    }
    
    /**
     * 更新用户信息
     */
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.info("Updating user with id: {}", id);
        
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "User not found with id: " + id));
        
        // 参数校验
        validateUserForUpdate(userDTO);
        
        // 只更新传入的非空字段
        boolean updated = false;
        if (StringUtils.hasText(userDTO.getNickname())) {
            existingUser.setNickname(userDTO.getNickname());
            updated = true;
        }
        if (StringUtils.hasText(userDTO.getAvatarUrl())) {
            existingUser.setAvatarUrl(userDTO.getAvatarUrl());
            updated = true;
        }
        if (userDTO.getGender() != null) {
            existingUser.setGender(userDTO.getGender());
            updated = true;
        }
        if (StringUtils.hasText(userDTO.getPhone())) {
            existingUser.setPhone(userDTO.getPhone());
            updated = true;
        }
        
        if (!updated) {
            log.warn("No fields to update for user id: {}", id);
            throw new BusinessException(400, "No fields to update");
        }
        
        // 保存并返回 DTO
        User updatedUser = userRepository.save(existingUser);
        log.info("User updated successfully with id: {}", updatedUser.getId());
        return new UserDTO(updatedUser);
    }
    
    /**
     * 删除用户（逻辑删除 - 将状态设为禁用）
     * 注意：不会物理删除用户数据，保留订单等关联记录
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("Soft deleting user with id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "User not found with id: " + id));
        
        // 逻辑删除：将状态设为禁用（0）
        user.setStatus(0);
        userRepository.save(user);
        
        log.info("User soft deleted successfully with id: {}", id);
    }
    
    /**
     * 获取所有用户（分页）- 只返回正常状态的用户
     */
    @Transactional(readOnly = true)
    public PageResponse<UserDTO> getAllUsers(Integer page, Integer pageSize) {
        int validPage = (page != null && page > 0) ? page : 1;
        int validPageSize = (pageSize != null && pageSize > 0) ? pageSize : 10;
        
        log.debug("Fetching all active users - page: {}, pageSize: {}", validPage, validPageSize);
        
        Pageable pageable = PageRequest.of(validPage - 1, validPageSize);
        // 只查询 status=1 的用户
        Page<User> userPage = userRepository.findByStatus(1, pageable);
        
        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
        
        return new PageResponse<>(validPage, validPageSize, userPage.getTotalElements(), userDTOs);
    }
    
    /**
     * 根据状态获取用户（分页）
     */
    @Transactional(readOnly = true)
    public PageResponse<UserDTO> getUsersByStatus(Integer status, Integer page, Integer pageSize) {
        int validPage = (page != null && page > 0) ? page : 1;
        int validPageSize = (pageSize != null && pageSize > 0) ? pageSize : 10;
        
        log.debug("Fetching users by status: {} - page: {}, pageSize: {}", status, validPage, validPageSize);
        
        Pageable pageable = PageRequest.of(validPage - 1, validPageSize);
        Page<User> userPage = userRepository.findByStatus(status, pageable);
        
        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
        
        return new PageResponse<>(validPage, validPageSize, userPage.getTotalElements(), userDTOs);
    }
    
    /**
     * 根据昵称搜索用户（分页）
     */
    @Transactional(readOnly = true)
    public PageResponse<UserDTO> searchUsersByNickname(String nickname, Integer page, Integer pageSize) {
        int validPage = (page != null && page > 0) ? page : 1;
        int validPageSize = (pageSize != null && pageSize > 0) ? pageSize : 10;
        
        log.debug("Searching users by nickname: {} - page: {}, pageSize: {}", nickname, validPage, validPageSize);
        
        Pageable pageable = PageRequest.of(validPage - 1, validPageSize);
        Page<User> userPage = userRepository.findByNicknameContaining(nickname, pageable);
        
        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
        
        return new PageResponse<>(validPage, validPageSize, userPage.getTotalElements(), userDTOs);
    }
    
    /**
     * 更新最后登录时间
     */
    @Transactional
    public void updateLastLoginTime(Long userId) {
        log.info("Updating last login time for user id: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "User not found with id: " + userId));
        
        userRepository.updateLastLoginTime(userId, LocalDateTime.now());
        log.info("Last login time updated for user id: {}", userId);
    }
    
    /**
     * 禁用用户
     */
    @Transactional
    public void disableUser(Long userId) {
        log.info("Disabling user with id: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "User not found with id: " + userId));
        
        userRepository.disableUser(userId);
        log.info("User disabled successfully with id: {}", userId);
    }
    
    /**
     * 启用用户
     */
    @Transactional
    public void enableUser(Long userId) {
        log.info("Enabling user with id: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "User not found with id: " + userId));
        
        userRepository.enableUser(userId);
        log.info("User enabled successfully with id: {}", userId);
    }
    
    // ==================== MyBatis 方法（复杂查询）====================
    
    /**
     * 根据注册时间范围查询用户（分页）
     */
    @Transactional(readOnly = true)
    public PageResponse<UserDTO> getUsersByCreateTimeRange(LocalDateTime startTime, 
                                                           LocalDateTime endTime,
                                                           Integer page, 
                                                           Integer pageSize) {
        int validPage = (page != null && page > 0) ? page : 1;
        int validPageSize = (pageSize != null && pageSize > 0) ? pageSize : 10;
        
        log.debug("Fetching users by create time range - page: {}, pageSize: {}", validPage, validPageSize);
        
        int offset = (validPage - 1) * validPageSize;
        List<User> users = userMapper.findByCreateTimeRangeWithPage(startTime, endTime, offset, validPageSize);
        long total = userMapper.countByCreateTimeRange(startTime, endTime);
        
        List<UserDTO> userDTOs = users.stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
        
        return new PageResponse<>(validPage, validPageSize, total, userDTOs);
    }
    
    /**
     * 批量插入用户
     */
    @Transactional
    public int batchInsertUsers(List<UserDTO> userDTOs) {
        log.info("Batch inserting {} users", userDTOs.size());
        
        if (userDTOs == null || userDTOs.isEmpty()) {
            throw new BusinessException(400, "User list cannot be empty");
        }
        
        List<User> users = userDTOs.stream()
                .map(UserDTO::toEntity)
                .collect(Collectors.toList());
        
        // 校验每个用户
        users.forEach(user -> validateUser(new UserDTO(user), true));
        
        // 检查 openid 是否重复（批量数据内部）
        List<String> openids = users.stream()
                .map(User::getOpenid)
                .collect(Collectors.toList());
        
        long distinctCount = openids.stream().distinct().count();
        if (distinctCount < openids.size()) {
            throw new BusinessException(400, "Duplicate openid found in batch data");
        }
        
        // 检查数据库中是否已存在这些 openid
        for (String openid : openids) {
            if (userRepository.findByOpenid(openid).isPresent()) {
                throw new BusinessException(400, "User with openid already exists: " + openid);
            }
        }
        
        int count = userMapper.batchInsert(users);
        log.info("Successfully inserted {} users", count);
        return count;
    }
    
    /**
     * 批量更新用户状态
     */
    @Transactional
    public int batchUpdateUserStatus(List<Long> userIds, Integer status) {
        log.info("Batch updating status for {} users to status: {}", userIds.size(), status);
        
        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException(400, "User IDs cannot be empty");
        }
        
        int count = userMapper.batchUpdateStatus(userIds, status);
        log.info("Successfully updated {} users' status", count);
        return count;
    }
    
    /**
     * 查询活跃用户
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getActiveUsers(Integer days) {
        int validDays = (days != null && days > 0) ? days : 7;
        
        log.debug("Fetching active users from last {} days", validDays);
        
        List<User> users = userMapper.findActiveUsers(validDays);
        
        return users.stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }
    
    // ==================== 私有校验方法 ====================
    
    /**
     * 校验用户字段（用于新增）
     */
    private void validateUser(UserDTO userDTO, boolean isCreate) {
        if (userDTO == null) {
            throw new BusinessException("User cannot be null");
        }
        
        // OpenID 校验（必填）
        if (!StringUtils.hasText(userDTO.getOpenid())) {
            throw new BusinessException("OpenID cannot be empty");
        }
        
        // 性别校验
        if (userDTO.getGender() != null && (userDTO.getGender() < 0 || userDTO.getGender() > 2)) {
            throw new BusinessException("Gender must be 0 (unknown), 1 (male), or 2 (female)");
        }
        
        // 手机号格式校验（如果提供）
        if (StringUtils.hasText(userDTO.getPhone())) {
            if (!userDTO.getPhone().matches("^1[3-9]\\d{9}$")) {
                throw new BusinessException("Invalid phone number format");
            }
        }
        
        // 状态校验
        if (userDTO.getStatus() != null && (userDTO.getStatus() < 0 || userDTO.getStatus() > 1)) {
            throw new BusinessException("Status must be 0 (disabled) or 1 (active)");
        }
    }
    
    /**
     * 校验用户字段（用于更新）
     */
    private void validateUserForUpdate(UserDTO userDTO) {
        if (userDTO == null) {
            throw new BusinessException("User cannot be null");
        }
        
        // 性别校验（如果提供）
        if (userDTO.getGender() != null && (userDTO.getGender() < 0 || userDTO.getGender() > 2)) {
            throw new BusinessException("Gender must be 0 (unknown), 1 (male), or 2 (female)");
        }
        
        // 手机号格式校验（如果提供）
        if (StringUtils.hasText(userDTO.getPhone())) {
            if (!userDTO.getPhone().matches("^1[3-9]\\d{9}$")) {
                throw new BusinessException("Invalid phone number format");
            }
        }
    }
    
    // ==================== 微信登录相关方法 ====================
    
    /**
     * 根据 openid 查询或创建用户（微信登录使用）
     * 如果用户不存在则自动创建，存在则返回现有用户
     * 使用异常捕获+重试机制处理并发竞态条件
     * 
     * @param openid 微信 openid
     * @param nickname 昵称（可选）
     * @param avatarUrl 头像URL（可选）
     * @param gender 性别（可选）
     * @return 用户 DTO
     */
    @Transactional
    public UserDTO getOrCreateUserByOpenid(String openid, String nickname, String avatarUrl, Integer gender) {
        log.info("Getting or creating user by openid: {}", openid);
        
        if (!StringUtils.hasText(openid)) {
            throw new BusinessException(400, "OpenID cannot be empty");
        }
        
        // 最多重试3次，处理并发竞态条件
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return doCreateOrUpdateUser(openid, nickname, avatarUrl, gender);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // 捕获唯一约束冲突异常（并发导致）
                if (attempt < maxRetries) {
                    log.warn("Duplicate key conflict on attempt {}, retrying... openid: {}", attempt, openid);
                    try {
                        Thread.sleep(50 * attempt); // 递增等待时间
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new BusinessException(500, "Operation interrupted");
                    }
                } else {
                    log.error("Failed to create user after {} attempts, openid: {}", maxRetries, openid);
                    throw new BusinessException(500, "Failed to create user due to concurrent access");
                }
            }
        }
        
        // 理论上不会到达这里
        throw new BusinessException(500, "Unexpected error in user creation");
    }
    
    /**
     * 实际的用户创建或更新逻辑（内部方法）
     */
    private UserDTO doCreateOrUpdateUser(String openid, String nickname, String avatarUrl, Integer gender) {
        // 尝试查找现有用户
        User existingUser = userRepository.findByOpenid(openid).orElse(null);
        
        if (existingUser != null) {
            // 用户存在，检查状态
            if (existingUser.getStatus() == 0) {
                throw new BusinessException(403, "User account is disabled");
            }
            
            // 更新最后登录时间
            existingUser.setLastLoginTime(LocalDateTime.now());
            
            // 如果提供了新信息，更新用户资料
            boolean updated = false;
            if (StringUtils.hasText(nickname) && !nickname.equals(existingUser.getNickname())) {
                existingUser.setNickname(nickname);
                updated = true;
            }
            if (StringUtils.hasText(avatarUrl) && !avatarUrl.equals(existingUser.getAvatarUrl())) {
                existingUser.setAvatarUrl(avatarUrl);
                updated = true;
            }
            if (gender != null && !gender.equals(existingUser.getGender())) {
                existingUser.setGender(gender);
                updated = true;
            }
            
            if (updated) {
                User savedUser = userRepository.save(existingUser);
                log.info("User info updated for openid: {}", openid);
                return new UserDTO(savedUser);
            }
            
            log.info("Existing user found for openid: {}", openid);
            return new UserDTO(existingUser);
        } else {
            // 用户不存在，创建新用户
            User newUser = new User();
            newUser.setOpenid(openid);
            newUser.setNickname(StringUtils.hasText(nickname) ? nickname : "微信用户");
            newUser.setAvatarUrl(avatarUrl);
            newUser.setGender(gender != null ? gender : 0);
            newUser.setStatus(1); // 默认激活状态
            newUser.setLastLoginTime(LocalDateTime.now());
            
            User savedUser = userRepository.save(newUser);
            log.info("New user created with openid: {}, userId: {}", openid, savedUser.getId());
            return new UserDTO(savedUser);
        }
    }
}
