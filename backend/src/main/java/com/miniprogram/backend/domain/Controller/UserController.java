package com.miniprogram.backend.domain.Controller;

import com.miniprogram.backend.common.ApiResponse;
import com.miniprogram.backend.common.PageResponse;
import com.miniprogram.backend.common.RequireAdmin;
import com.miniprogram.backend.common.UserContext;
import com.miniprogram.backend.domain.Entity.UpdateProfileRequest;
import com.miniprogram.backend.domain.Entity.UserDTO;
import com.miniprogram.backend.domain.Service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // 获取所有用户（分页）- 需要管理员权限
    @RequireAdmin
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResponse<UserDTO> result = userService.getAllUsers(page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    // 根据 ID 获取用户 - 需要管理员权限
    @RequireAdmin
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    // 根据 OpenID 获取用户 - 需要管理员权限
    @RequireAdmin
    @GetMapping("/openid/{openid}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByOpenid(@PathVariable String openid) {
        UserDTO user = userService.getUserByOpenid(openid);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    // 创建新用户（用户注册）- 公开访问
    // 注意：生产环境建议增加微信 code 验证，确保 openid 真实性
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDTO>> registerUser(@Valid @RequestBody UserDTO userDTO) {
        // 安全校验：确保 openid 不为空
        if (userDTO.getOpenid() == null || userDTO.getOpenid().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "OpenID cannot be empty"));
        }
        
        UserDTO user = userService.createUser(userDTO);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    // 获取当前登录用户信息
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        Long userId = UserContext.getCurrentUserId();
        UserDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    // 更新当前登录用户资料
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> updateCurrentUser(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = UserContext.getCurrentUserId();
        
        UserDTO userDTO = new UserDTO();
        userDTO.setNickname(request.getNickname());
        userDTO.setAvatarUrl(request.getAvatarUrl());
        userDTO.setGender(request.getGender());
        userDTO.setPhone(request.getPhone());
        
        UserDTO user = userService.updateUser(userId, userDTO);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    // 更新用户信息 - 需要管理员权限
    @RequireAdmin
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        UserDTO user = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    // 删除用户 - 需要管理员权限
    @RequireAdmin
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    // 根据状态获取用户（分页）- 需要管理员权限
    @RequireAdmin
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> getUsersByStatus(
            @PathVariable Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResponse<UserDTO> result = userService.getUsersByStatus(status, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    // 根据昵称搜索用户（分页）- 需要管理员权限
    @RequireAdmin
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> searchUsersByNickname(
            @RequestParam String nickname,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResponse<UserDTO> result = userService.searchUsersByNickname(nickname, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    // 更新最后登录时间
    @PostMapping("/{id}/login")
    public ResponseEntity<ApiResponse<Void>> updateLastLoginTime(@PathVariable Long id) {
        userService.updateLastLoginTime(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    // 禁用用户 - 需要管理员权限
    @RequireAdmin
    @PostMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<Void>> disableUser(@PathVariable Long id) {
        userService.disableUser(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    // 启用用户 - 需要管理员权限
    @RequireAdmin
    @PostMapping("/{id}/enable")
    public ResponseEntity<ApiResponse<Void>> enableUser(@PathVariable Long id) {
        userService.enableUser(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    // ==================== MyBatis 复杂查询接口 ====================
    
    /**
     * 根据注册时间范围查询用户（分页）- 需要管理员权限
     */
    @RequireAdmin
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> getUsersByCreateTimeRange(
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResponse<UserDTO> result = userService.getUsersByCreateTimeRange(startTime, endTime, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 批量插入用户 - 需要管理员权限
     */
    @RequireAdmin
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<Integer>> batchInsertUsers(@RequestBody List<UserDTO> userDTOs) {
        int count = userService.batchInsertUsers(userDTOs);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
    
    /**
     * 批量更新用户状态 - 需要管理员权限
     */
    @RequireAdmin
    @PostMapping("/batch/status")
    public ResponseEntity<ApiResponse<Integer>> batchUpdateUserStatus(
            @RequestBody List<Long> userIds,
            @RequestParam Integer status) {
        int count = userService.batchUpdateUserStatus(userIds, status);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
    
    /**
     * 查询活跃用户 - 需要管理员权限
     */
    @RequireAdmin
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getActiveUsers(
            @RequestParam(defaultValue = "7") Integer days) {
        List<UserDTO> users = userService.getActiveUsers(days);
        return ResponseEntity.ok(ApiResponse.success(users));
    }
}
