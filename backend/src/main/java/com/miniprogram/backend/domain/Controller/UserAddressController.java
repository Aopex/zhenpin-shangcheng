package com.miniprogram.backend.domain.Controller;

import com.miniprogram.backend.common.ApiResponse;
import com.miniprogram.backend.common.BusinessException;
import com.miniprogram.backend.common.PageResponse;
import com.miniprogram.backend.common.RequireAdmin;
import com.miniprogram.backend.common.UserContext;
import com.miniprogram.backend.domain.Entity.UserAddressDTO;
import com.miniprogram.backend.domain.Mapper.UserAddressMapper;
import com.miniprogram.backend.domain.Service.UserAddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class UserAddressController {
    
    @Autowired
    private UserAddressService userAddressService;
    
    // 根据 ID 获取地址 - 需要验证用户权限
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserAddressDTO>> getAddressById(@PathVariable Long id) {
        UserAddressDTO address = userAddressService.getAddressById(id);
        requireOwnAddress(address);
        return ResponseEntity.ok(ApiResponse.success(address));
    }
    
    // 创建新地址 - 从Token中获取用户ID
    @PostMapping
    public ResponseEntity<ApiResponse<UserAddressDTO>> createAddress(@Valid @RequestBody UserAddressDTO addressDTO) {
        // 从Token中获取用户ID，确保安全性
        Long userId = UserContext.getCurrentUserId();
        addressDTO.setUserId(userId);
        UserAddressDTO address = userAddressService.createAddress(addressDTO);
        return ResponseEntity.ok(ApiResponse.success(address));
    }
    
    // 更新地址信息 - 需要验证用户权限
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserAddressDTO>> updateAddress(
            @PathVariable Long id, 
            @Valid @RequestBody UserAddressDTO addressDTO) {
        requireOwnAddress(userAddressService.getAddressById(id));
        UserAddressDTO address = userAddressService.updateAddress(id, addressDTO);
        return ResponseEntity.ok(ApiResponse.success(address));
    }
    
    // 删除地址（逻辑删除）- 需要验证用户权限
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable Long id) {
        requireOwnAddress(userAddressService.getAddressById(id));
        userAddressService.deleteAddress(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    // 获取用户的所有地址（分页）- 从Token中获取用户ID
    @GetMapping("/my-addresses")
    public ResponseEntity<ApiResponse<PageResponse<UserAddressDTO>>> getUserAddresses(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // 从Token中获取用户ID，确保安全性
        Long userId = UserContext.getCurrentUserId();
        PageResponse<UserAddressDTO> result = userAddressService.getUserAddresses(userId, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    // 获取用户的所有地址列表（默认地址优先）- 从Token中获取用户ID
    @GetMapping("/my-addresses/list")
    public ResponseEntity<ApiResponse<List<UserAddressDTO>>> getUserAddressesList() {
        // 从Token中获取用户ID，确保安全性
        Long userId = UserContext.getCurrentUserId();
        List<UserAddressDTO> addresses = userAddressService.getUserAddressesList(userId);
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }
    
    // 获取用户的默认地址 - 从Token中获取用户ID
    @GetMapping("/my-addresses/default")
    public ResponseEntity<ApiResponse<UserAddressDTO>> getDefaultAddress() {
        // 从Token中获取用户ID，确保安全性
        Long userId = UserContext.getCurrentUserId();
        UserAddressDTO address = userAddressService.getDefaultAddress(userId);
        return ResponseEntity.ok(ApiResponse.success(address));
    }
    
    // 设置默认地址 - 从Token中获取用户ID
    @PostMapping("/{id}/set-default")
    public ResponseEntity<ApiResponse<Void>> setDefaultAddress(@PathVariable Long id) {
        requireOwnAddress(userAddressService.getAddressById(id));
        userAddressService.setDefaultAddress(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    // 统计用户的地址数量 - 从Token中获取用户ID
    @GetMapping("/my-addresses/count")
    public ResponseEntity<ApiResponse<Long>> countUserAddresses() {
        // 从Token中获取用户ID，确保安全性
        Long userId = UserContext.getCurrentUserId();
        long count = userAddressService.countUserAddresses(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    private void requireOwnAddress(UserAddressDTO address) {
        Long userId = UserContext.getCurrentUserId();
        if (address == null || address.getUserId() == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(403, "You don't have permission to access this address");
        }
    }
    
    // ==================== MyBatis 复杂查询接口 ====================
    
    /**
     * 根据收货人姓名搜索地址（分页）- 需要管理员权限
     */
    @RequireAdmin
    @GetMapping("/search/receiver-name")
    public ResponseEntity<ApiResponse<PageResponse<UserAddressDTO>>> searchAddressesByReceiverName(
            @RequestParam String receiverName,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResponse<UserAddressDTO> result = userAddressService.searchAddressesByReceiverNameWithPage(
                receiverName, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 根据收货人手机号查找地址（分页）- 需要管理员权限
     */
    @RequireAdmin
    @GetMapping("/search/phone")
    public ResponseEntity<ApiResponse<PageResponse<UserAddressDTO>>> getAddressesByReceiverPhone(
            @RequestParam String receiverPhone,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResponse<UserAddressDTO> result = userAddressService.getAddressesByReceiverPhoneWithPage(
                receiverPhone, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 根据省市查询地址（分页）- 需要管理员权限
     */
    @RequireAdmin
    @GetMapping("/search/location")
    public ResponseEntity<ApiResponse<PageResponse<UserAddressDTO>>> getAddressesByProvinceAndCity(
            @RequestParam String province,
            @RequestParam String city,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResponse<UserAddressDTO> result = userAddressService.getAddressesByProvinceAndCityWithPage(
                province, city, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 批量设置默认地址 - 需要管理员权限
     */
    @RequireAdmin
    @PostMapping("/batch/set-default")
    public ResponseEntity<ApiResponse<Integer>> batchSetDefaultAddress(
            @RequestParam Long userId,
            @RequestParam Long addressId) {
        int count = userAddressService.batchSetDefaultAddress(userId, addressId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
    
    /**
     * 批量逻辑删除用户地址 - 需要管理员权限
     */
    @RequireAdmin
    @PostMapping("/batch/delete")
    public ResponseEntity<ApiResponse<Integer>> batchSoftDeleteAddresses(@RequestBody List<Long> userIds) {
        int count = userAddressService.batchSoftDeleteAddresses(userIds);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
    
    /**
     * 统计每个用户的地址数量 - 需要管理员权限
     */
    @RequireAdmin
    @GetMapping("/stats/by-user")
    public ResponseEntity<ApiResponse<List<UserAddressMapper.UserAddressCount>>> countAddressesByUser() {
        List<UserAddressMapper.UserAddressCount> stats = userAddressService.countAddressesByUser();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
