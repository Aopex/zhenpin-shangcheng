package com.miniprogram.backend.domain.Service;

import com.miniprogram.backend.common.BusinessException;
import com.miniprogram.backend.common.PageResponse;
import com.miniprogram.backend.domain.Entity.UserAddress;
import com.miniprogram.backend.domain.Entity.UserAddressDTO;
import com.miniprogram.backend.domain.DAO.UserAddressRepository;
import com.miniprogram.backend.domain.Mapper.UserAddressMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户收货地址服务类
 * 处理用户地址管理相关业务逻辑
 */
@Slf4j
@Service
public class UserAddressService {
    
    @Autowired
    private UserAddressRepository userAddressRepository;
    
    @Autowired
    private UserAddressMapper userAddressMapper;
    
    // ==================== JPA 方法（简单 CRUD）====================
    
    /**
     * 根据 ID 获取地址
     */
    @Transactional(readOnly = true)
    public UserAddressDTO getAddressById(Long id) {
        log.debug("Fetching address by id: {}", id);
        
        return userAddressRepository.findByIdAndIsDeletedFalse(id)
                .map(UserAddressDTO::new)
                .orElseThrow(() -> new BusinessException(404, "Address not found with id: " + id));
    }
    
    /**
     * 根据 ID 获取地址实体
     */
    @Transactional(readOnly = true)
    public UserAddress getAddressEntityById(Long id) {
        log.debug("Fetching address entity by id: {}", id);
        
        return userAddressRepository.findByIdAndIsDeletedFalse(id)
                .orElse(null);
    }
    
    /**
     * 创建新地址
     */
    @Transactional
    public UserAddressDTO createAddress(UserAddressDTO addressDTO) {
        log.info("Creating new address for user id: {}", addressDTO.getUserId());
        
        // 参数校验
        validateAddress(addressDTO, true);
        
        // 转换为 Entity
        UserAddress address = addressDTO.toEntity();
        
        // 如果设置为默认地址，先取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            userAddressRepository.clearDefaultAddresses(address.getUserId());
        }
        
        // 保存并返回 DTO
        UserAddress savedAddress = userAddressRepository.save(address);
        log.info("Address created successfully with id: {}", savedAddress.getId());
        return new UserAddressDTO(savedAddress);
    }
    
    /**
     * 更新地址信息
     */
    @Transactional
    public UserAddressDTO updateAddress(Long id, UserAddressDTO addressDTO) {
        log.info("Updating address with id: {}", id);
        
        UserAddress existingAddress = userAddressRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(404, "Address not found with id: " + id));
        
        // 参数校验
        validateAddressForUpdate(addressDTO);
        
        // 只更新传入的非空字段
        boolean updated = false;
        if (StringUtils.hasText(addressDTO.getReceiverName())) {
            existingAddress.setReceiverName(addressDTO.getReceiverName());
            updated = true;
        }
        if (StringUtils.hasText(addressDTO.getReceiverPhone())) {
            existingAddress.setReceiverPhone(addressDTO.getReceiverPhone());
            updated = true;
        }
        if (StringUtils.hasText(addressDTO.getProvince())) {
            existingAddress.setProvince(addressDTO.getProvince());
            updated = true;
        }
        if (StringUtils.hasText(addressDTO.getCity())) {
            existingAddress.setCity(addressDTO.getCity());
            updated = true;
        }
        if (StringUtils.hasText(addressDTO.getDistrict())) {
            existingAddress.setDistrict(addressDTO.getDistrict());
            updated = true;
        }
        if (StringUtils.hasText(addressDTO.getAddress())) {
            existingAddress.setAddress(addressDTO.getAddress());
            updated = true;
        }
        
        // 如果设置默认地址
        if (addressDTO.getIsDefault() != null && addressDTO.getIsDefault()) {
            // 先取消该用户的所有默认地址
            userAddressRepository.clearDefaultAddresses(existingAddress.getUserId());
            // 再设置当前地址为默认
            existingAddress.setIsDefault(1);
            updated = true;
        } else if (addressDTO.getIsDefault() != null && !addressDTO.getIsDefault()) {
            existingAddress.setIsDefault(0);
            updated = true;
        }
        
        if (!updated) {
            log.warn("No fields to update for address id: {}", id);
            throw new BusinessException(400, "No fields to update");
        }
        
        // 保存并返回 DTO
        UserAddress updatedAddress = userAddressRepository.save(existingAddress);
        log.info("Address updated successfully with id: {}", updatedAddress.getId());
        return new UserAddressDTO(updatedAddress);
    }
    
    /**
     * 删除地址（逻辑删除）
     */
    @Transactional
    public void deleteAddress(Long id) {
        log.info("Deleting address with id: {}", id);
        
        UserAddress address = userAddressRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(404, "Address not found with id: " + id));
        
        // 逻辑删除
        userAddressRepository.softDelete(id);
        log.info("Address deleted successfully with id: {}", id);
    }
    
    /**
     * 获取用户的所有地址（分页）
     */
    @Transactional(readOnly = true)
    public PageResponse<UserAddressDTO> getUserAddresses(Long userId, Integer page, Integer pageSize) {
        int validPage = (page != null && page > 0) ? page : 1;
        int validPageSize = (pageSize != null && pageSize > 0) ? pageSize : 10;
        
        log.debug("Fetching addresses for user id: {} - page: {}, pageSize: {}", userId, validPage, validPageSize);
        
        Pageable pageable = PageRequest.of(validPage - 1, validPageSize);
        Page<UserAddress> addressPage = userAddressRepository.findByUserIdAndIsDeletedFalse(userId, pageable);
        
        List<UserAddressDTO> addressDTOs = addressPage.getContent().stream()
                .map(UserAddressDTO::new)
                .collect(Collectors.toList());
        
        return new PageResponse<>(validPage, validPageSize, addressPage.getTotalElements(), addressDTOs);
    }
    
    /**
     * 获取用户的所有地址列表（默认地址优先）
     */
    @Transactional(readOnly = true)
    public List<UserAddressDTO> getUserAddressesList(Long userId) {
        log.debug("Fetching all addresses for user id: {}", userId);
        
        List<UserAddress> addresses = userAddressRepository.findByUserIdAndIsDeletedFalseOrderByIsDefaultDesc(userId);
        
        return addresses.stream()
                .map(UserAddressDTO::new)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户的默认地址
     */
    @Transactional(readOnly = true)
    public UserAddressDTO getDefaultAddress(Long userId) {
        log.debug("Fetching default address for user id: {}", userId);
        
        return userAddressRepository.findByUserIdAndIsDefaultTrueAndIsDeletedFalse(userId)
                .map(UserAddressDTO::new)
                .orElse(null);
    }
    
    /**
     * 设置默认地址
     */
    @Transactional
    public void setDefaultAddress(Long addressId) {
        log.info("Setting address id: {} as default", addressId);
        
        UserAddress address = userAddressRepository.findByIdAndIsDeletedFalse(addressId)
                .orElseThrow(() -> new BusinessException(404, "Address not found with id: " + addressId));
        
        // 先取消该用户的所有默认地址
        userAddressRepository.clearDefaultAddresses(address.getUserId());
        // 再设置当前地址为默认
        userAddressRepository.setAsDefault(addressId);
        
        log.info("Address id: {} set as default", addressId);
    }
    
    /**
     * 统计用户的地址数量
     */
    @Transactional(readOnly = true)
    public long countUserAddresses(Long userId) {
        log.debug("Counting addresses for user id: {}", userId);
        
        return userAddressRepository.countByUserIdAndIsDeletedFalse(userId);
    }
    
    // ==================== MyBatis 方法（复杂查询）====================
    
    /**
     * 根据收货人姓名搜索地址（分页）
     */
    @Transactional(readOnly = true)
    public PageResponse<UserAddressDTO> searchAddressesByReceiverNameWithPage(
            String receiverName, Integer page, Integer pageSize) {
        log.debug("Searching addresses by receiver name with pagination: {}, page: {}, size: {}", 
                receiverName, page, pageSize);
        
        if (!StringUtils.hasText(receiverName)) {
            throw new BusinessException(400, "Receiver name cannot be empty");
        }
        
        // 参数校验
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }
        
        int offset = (page - 1) * pageSize;
        
        // 查询总数
        long total = userAddressMapper.countByReceiverNameContaining(receiverName);
        
        // 查询数据
        List<UserAddress> addresses = userAddressMapper.findByReceiverNameContainingWithPage(
                receiverName, offset, pageSize);
        
        List<UserAddressDTO> dtoList = addresses.stream()
                .map(UserAddressDTO::new)
                .collect(Collectors.toList());
        
        return new PageResponse<>(page, pageSize, total, dtoList);
    }
    
    /**
     * 根据收货人姓名搜索地址
     */
    @Transactional(readOnly = true)
    public List<UserAddressDTO> searchAddressesByReceiverName(String receiverName) {
        log.debug("Searching addresses by receiver name: {}", receiverName);
        
        if (!StringUtils.hasText(receiverName)) {
            throw new BusinessException(400, "Receiver name cannot be empty");
        }
        
        List<UserAddress> addresses = userAddressMapper.findByReceiverNameContaining(receiverName);
        
        return addresses.stream()
                .map(UserAddressDTO::new)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据收货人手机号查找地址（分页）
     */
    @Transactional(readOnly = true)
    public PageResponse<UserAddressDTO> getAddressesByReceiverPhoneWithPage(
            String receiverPhone, Integer page, Integer pageSize) {
        log.debug("Fetching addresses by receiver phone with pagination: {}, page: {}, size: {}", 
                receiverPhone, page, pageSize);
        
        if (!StringUtils.hasText(receiverPhone)) {
            throw new BusinessException(400, "Receiver phone cannot be empty");
        }
        
        // 参数校验
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }
        
        int offset = (page - 1) * pageSize;
        
        // 查询总数
        long total = userAddressMapper.countByReceiverPhone(receiverPhone);
        
        // 查询数据
        List<UserAddress> addresses = userAddressMapper.findByReceiverPhoneWithPage(
                receiverPhone, offset, pageSize);
        
        List<UserAddressDTO> dtoList = addresses.stream()
                .map(UserAddressDTO::new)
                .collect(Collectors.toList());
        
        return new PageResponse<>(page, pageSize, total, dtoList);
    }
    
    /**
     * 根据收货人手机号查找地址
     */
    @Transactional(readOnly = true)
    public List<UserAddressDTO> getAddressesByReceiverPhone(String receiverPhone) {
        log.debug("Fetching addresses by receiver phone: {}", receiverPhone);
        
        if (!StringUtils.hasText(receiverPhone)) {
            throw new BusinessException(400, "Receiver phone cannot be empty");
        }
        
        List<UserAddress> addresses = userAddressMapper.findByReceiverPhone(receiverPhone);
        
        return addresses.stream()
                .map(UserAddressDTO::new)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据省市查询地址（分页）
     */
    @Transactional(readOnly = true)
    public PageResponse<UserAddressDTO> getAddressesByProvinceAndCityWithPage(
            String province, String city, Integer page, Integer pageSize) {
        log.debug("Fetching addresses by province: {}, city: {} with pagination, page: {}, size: {}", 
                province, city, page, pageSize);
        
        if (!StringUtils.hasText(province) || !StringUtils.hasText(city)) {
            throw new BusinessException(400, "Province and city cannot be empty");
        }
        
        // 参数校验
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }
        
        int offset = (page - 1) * pageSize;
        
        // 查询总数
        long total = userAddressMapper.countByProvinceAndCity(province, city);
        
        // 查询数据
        List<UserAddress> addresses = userAddressMapper.findByProvinceAndCityWithPage(
                province, city, offset, pageSize);
        
        List<UserAddressDTO> dtoList = addresses.stream()
                .map(UserAddressDTO::new)
                .collect(Collectors.toList());
        
        return new PageResponse<>(page, pageSize, total, dtoList);
    }
    
    /**
     * 根据省市查询地址
     */
    @Transactional(readOnly = true)
    public List<UserAddressDTO> getAddressesByProvinceAndCity(String province, String city) {
        log.debug("Fetching addresses by province: {}, city: {}", province, city);
        
        if (!StringUtils.hasText(province) || !StringUtils.hasText(city)) {
            throw new BusinessException(400, "Province and city cannot be empty");
        }
        
        List<UserAddress> addresses = userAddressMapper.findByProvinceAndCity(province, city);
        
        return addresses.stream()
                .map(UserAddressDTO::new)
                .collect(Collectors.toList());
    }
    
    /**
     * 批量设置默认地址
     */
    @Transactional
    public int batchSetDefaultAddress(Long userId, Long addressId) {
        log.info("Batch setting default address for user id: {}, address id: {}", userId, addressId);
        
        if (userId == null || addressId == null) {
            throw new BusinessException(400, "User ID and Address ID cannot be null");
        }
        
        // 验证地址是否存在且属于该用户
        UserAddress address = userAddressRepository.findByIdAndIsDeletedFalse(addressId)
                .orElseThrow(() -> new BusinessException(404, "Address not found with id: " + addressId));
        
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(400, "Address does not belong to this user");
        }
        
        int count = userAddressMapper.batchSetDefault(userId, addressId);
        log.info("Successfully set default address");
        return count;
    }
    
    /**
     * 批量逻辑删除用户地址
     */
    @Transactional
    public int batchSoftDeleteAddresses(List<Long> userIds) {
        log.info("Batch soft deleting addresses for {} users", userIds.size());
        
        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException(400, "User IDs cannot be empty");
        }
        
        int count = userAddressMapper.batchSoftDelete(userIds);
        log.info("Successfully soft deleted addresses for {} users", count);
        return count;
    }
    
    /**
     * 统计每个用户的地址数量
     */
    @Transactional(readOnly = true)
    public List<UserAddressMapper.UserAddressCount> countAddressesByUser() {
        log.debug("Counting addresses by user");
        
        return userAddressMapper.countAddressesByUser();
    }
    
    // ==================== 私有校验方法 ====================
    
    /**
     * 校验地址字段（用于新增）
     */
    private void validateAddress(UserAddressDTO addressDTO, boolean isCreate) {
        if (addressDTO == null) {
            throw new BusinessException("Address cannot be null");
        }
        
        // 用户 ID 校验
        if (addressDTO.getUserId() == null) {
            throw new BusinessException("User ID cannot be null");
        }
        
        // 收货人姓名校验
        if (!StringUtils.hasText(addressDTO.getReceiverName())) {
            throw new BusinessException("Receiver name cannot be empty");
        }
        
        // 收货人手机号校验
        if (!StringUtils.hasText(addressDTO.getReceiverPhone())) {
            throw new BusinessException("Receiver phone cannot be empty");
        }
        if (!addressDTO.getReceiverPhone().matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("Invalid receiver phone number format");
        }
        
        // 省份校验
        if (!StringUtils.hasText(addressDTO.getProvince())) {
            throw new BusinessException("Province cannot be empty");
        }
        
        // 城市校验
        if (!StringUtils.hasText(addressDTO.getCity())) {
            throw new BusinessException("City cannot be empty");
        }
        
        // 区县校验
        if (!StringUtils.hasText(addressDTO.getDistrict())) {
            throw new BusinessException("District cannot be empty");
        }
        
        // 详细地址校验
        if (!StringUtils.hasText(addressDTO.getAddress())) {
            throw new BusinessException("Address cannot be empty");
        }
    }
    
    /**
     * 校验地址字段（用于更新）
     */
    private void validateAddressForUpdate(UserAddressDTO addressDTO) {
        if (addressDTO == null) {
            throw new BusinessException("Address cannot be null");
        }
        
        // 收货人手机号格式校验（如果提供）
        if (StringUtils.hasText(addressDTO.getReceiverPhone())) {
            if (!addressDTO.getReceiverPhone().matches("^1[3-9]\\d{9}$")) {
                throw new BusinessException("Invalid receiver phone number format");
            }
        }
    }
}
