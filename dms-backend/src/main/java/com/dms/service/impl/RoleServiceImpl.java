package com.dms.service.impl;

import com.dms.dto.response.RoleResponse;
import com.dms.mapper.UserMapper;
import com.dms.repository.RoleRepository;
import com.dms.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(userMapper::toRoleResponse)
                .collect(Collectors.toList());
    }
}
