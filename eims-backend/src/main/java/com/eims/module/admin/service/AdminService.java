package com.eims.module.admin.service;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.eims.common.exception.BusinessException;

import com.eims.common.result.PageResult;

import com.eims.common.service.ReferentialGuardService;

import com.eims.module.admin.dto.AssignMenuRequest;

import com.eims.module.admin.dto.AssignRoleRequest;

import com.eims.module.admin.entity.SysDept;

import com.eims.module.admin.entity.SysMenu;

import com.eims.module.admin.entity.SysRole;

import com.eims.module.admin.entity.SysUser;

import com.eims.module.admin.mapper.SysDeptMapper;

import com.eims.module.admin.mapper.SysMenuMapper;

import com.eims.module.admin.mapper.SysRoleMapper;

import com.eims.module.admin.mapper.SysUserMapper;

import com.eims.module.admin.vo.SysDeptTreeVO;

import com.eims.module.admin.vo.SysMenuTreeVO;

import com.eims.module.admin.vo.SysUserVO;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.util.StringUtils;



import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;

import java.util.Map;

import java.util.stream.Collectors;



@Service

@RequiredArgsConstructor

public class AdminService {



    private final SysUserMapper userMapper;

    private final SysDeptMapper deptMapper;

    private final SysRoleMapper roleMapper;

    private final SysMenuMapper menuMapper;

    private final JdbcTemplate jdbcTemplate;

    private final PasswordEncoder passwordEncoder;

    private final ReferentialGuardService referentialGuardService;



    public PageResult<SysUserVO> pageUsers(long current, long size, String keyword) {

        Page<SysUser> page = new Page<>(current, size);

        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<SysUser>()

                .eq(SysUser::getIsDeleted, 0)

                .orderByDesc(SysUser::getCreateTime);

        if (StringUtils.hasText(keyword)) {

            qw.and(w -> w.like(SysUser::getUserName, keyword)

                    .or().like(SysUser::getNickName, keyword)

                    .or().like(SysUser::getPhone, keyword));

        }

        userMapper.selectPage(page, qw);

        List<SysUserVO> records = page.getRecords().stream().map(this::toUserVO).collect(Collectors.toList());

        return new PageResult<>(records, page.getTotal(), current, size);

    }



    public List<Long> getUserRoleIds(Long userId) {

        return jdbcTemplate.queryForList(

                "SELECT role_id FROM sys_user_role WHERE user_id = ?", Long.class, userId);

    }



    @Transactional

    public void assignUserRoles(Long userId, AssignRoleRequest request) {

        SysUser user = userMapper.selectById(userId);

        if (user == null) throw new BusinessException("用户不存在");

        jdbcTemplate.update("DELETE FROM sys_user_role WHERE user_id = ?", userId);

        List<Long> roleIds = request != null ? request.getRoleIds() : null;

        if (roleIds == null) return;

        for (Long roleId : roleIds) {

            SysRole role = roleMapper.selectById(roleId);

            if (role == null || (role.getIsDeleted() != null && role.getIsDeleted() == 1)) {

                throw new BusinessException("角色不存在: " + roleId);

            }

            jdbcTemplate.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)", userId, roleId);

        }

    }



    public List<SysMenuTreeVO> menuTree() {

        List<SysMenu> menus = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()

                .eq(SysMenu::getIsDeleted, 0)

                .orderByAsc(SysMenu::getSortNo)

                .orderByAsc(SysMenu::getId));

        Map<Long, SysMenuTreeVO> map = new HashMap<>();

        List<SysMenuTreeVO> roots = new ArrayList<>();

        for (SysMenu menu : menus) {

            SysMenuTreeVO node = toMenuTreeVO(menu);

            map.put(menu.getId(), node);

        }

        for (SysMenu menu : menus) {

            SysMenuTreeVO node = map.get(menu.getId());

            Long parentId = menu.getParentId() == null ? 0L : menu.getParentId();

            if (parentId == 0L || !map.containsKey(parentId)) {

                roots.add(node);

            } else {

                map.get(parentId).getChildren().add(node);

            }

        }

        return roots;

    }



    public List<Long> getRoleMenuIds(Long roleId) {

        SysRole role = roleMapper.selectById(roleId);

        if (role == null) throw new BusinessException("角色不存在");

        return jdbcTemplate.queryForList(

                "SELECT menu_id FROM sys_role_menu WHERE role_id = ?", Long.class, roleId);

    }



    @Transactional

    public void assignRoleMenus(Long roleId, AssignMenuRequest request) {

        SysRole role = roleMapper.selectById(roleId);

        if (role == null) throw new BusinessException("角色不存在");

        jdbcTemplate.update("DELETE FROM sys_role_menu WHERE role_id = ?", roleId);

        List<Long> menuIds = request != null ? request.getMenuIds() : null;

        if (menuIds == null) return;

        for (Long menuId : menuIds) {

            SysMenu menu = menuMapper.selectById(menuId);

            if (menu == null || (menu.getIsDeleted() != null && menu.getIsDeleted() == 1)) {

                throw new BusinessException("菜单不存在: " + menuId);

            }

            jdbcTemplate.update("INSERT INTO sys_role_menu (role_id, menu_id) VALUES (?, ?)", roleId, menuId);

        }

    }



    public SysUser createUser(SysUser entity) {

        if (!StringUtils.hasText(entity.getPassword())) {

            throw new BusinessException("密码不能为空");

        }

        entity.setPassword(passwordEncoder.encode(entity.getPassword()));

        if (entity.getStatus() == null) entity.setStatus(1);

        userMapper.insert(entity);

        entity.setPassword(null);

        return entity;

    }



    public SysUser updateUser(Long id, SysUser entity) {

        SysUser existing = userMapper.selectById(id);

        if (existing == null) throw new BusinessException("用户不存在");

        entity.setId(id);

        if (StringUtils.hasText(entity.getPassword())) {

            entity.setPassword(passwordEncoder.encode(entity.getPassword()));

        } else {

            entity.setPassword(existing.getPassword());

        }

        userMapper.updateById(entity);

        entity.setPassword(null);

        return entity;

    }



    @Transactional

    public void deleteUser(Long id) {

        referentialGuardService.ensureUserDeletable(id);

        jdbcTemplate.update("DELETE FROM sys_user_role WHERE user_id = ?", id);

        userMapper.deleteById(id);

    }



    public PageResult<SysDept> pageDept(long current, long size, String keyword) {

        Page<SysDept> page = new Page<>(current, size);

        LambdaQueryWrapper<SysDept> qw = new LambdaQueryWrapper<SysDept>()

                .eq(SysDept::getIsDeleted, 0)

                .orderByAsc(SysDept::getSortNo)

                .orderByAsc(SysDept::getId);

        if (StringUtils.hasText(keyword)) {

            qw.and(w -> w.like(SysDept::getDeptName, keyword)

                    .or().like(SysDept::getDeptCode, keyword));

        }

        deptMapper.selectPage(page, qw);

        return new PageResult<>(page.getRecords(), page.getTotal(), current, size);

    }



    public List<SysDeptTreeVO> deptTree() {

        List<SysDept> depts = deptMapper.selectList(new LambdaQueryWrapper<SysDept>()

                .eq(SysDept::getIsDeleted, 0)

                .orderByAsc(SysDept::getSortNo)

                .orderByAsc(SysDept::getId));

        Map<Long, SysDeptTreeVO> map = new HashMap<>();

        List<SysDeptTreeVO> roots = new ArrayList<>();

        for (SysDept dept : depts) {

            SysDeptTreeVO node = toDeptTreeVO(dept);

            map.put(dept.getId(), node);

        }

        for (SysDept dept : depts) {

            SysDeptTreeVO node = map.get(dept.getId());

            Long parentId = dept.getParentId() == null ? 0L : dept.getParentId();

            if (parentId == 0L || !map.containsKey(parentId)) {

                roots.add(node);

            } else {

                map.get(parentId).getChildren().add(node);

            }

        }

        return roots;

    }



    public SysDept createDept(SysDept entity) {

        if (entity.getParentId() == null) entity.setParentId(0L);

        if (entity.getSortNo() == null) entity.setSortNo(0);

        if (entity.getStatus() == null) entity.setStatus(1);

        deptMapper.insert(entity);

        return entity;

    }



    public SysDept updateDept(Long id, SysDept entity) {

        entity.setId(id);

        deptMapper.updateById(entity);

        return entity;

    }



    public void deleteDept(Long id) {

        referentialGuardService.ensureDeptDeletable(id);

        deptMapper.deleteById(id);

    }



    @Transactional

    public void deleteRole(Long id) {

        referentialGuardService.ensureRoleDeletable(id);

        jdbcTemplate.update("DELETE FROM sys_role_menu WHERE role_id = ?", id);

        roleMapper.deleteById(id);

    }



    private SysUserVO toUserVO(SysUser user) {

        SysUserVO vo = new SysUserVO();

        vo.setId(user.getId());

        vo.setDeptId(user.getDeptId());

        vo.setDeptName(queryDeptName(user.getDeptId()));

        vo.setUserName(user.getUserName());

        vo.setNickName(user.getNickName());

        vo.setPhone(user.getPhone());

        vo.setEmail(user.getEmail());

        vo.setStatus(user.getStatus());

        vo.setCreateTime(user.getCreateTime());

        List<Long> roleIds = getUserRoleIds(user.getId());

        vo.setRoleIds(roleIds);

        vo.setRoleNames(queryRoleNames(roleIds));

        return vo;

    }



    private String queryRoleNames(List<Long> roleIds) {

        if (roleIds == null || roleIds.isEmpty()) return null;

        List<SysRole> roles = roleMapper.selectBatchIds(roleIds);

        return roles.stream()

                .filter(r -> r.getIsDeleted() == null || r.getIsDeleted() == 0)

                .map(SysRole::getRoleName)

                .collect(Collectors.joining("、"));

    }



    private SysDeptTreeVO toDeptTreeVO(SysDept dept) {

        SysDeptTreeVO vo = new SysDeptTreeVO();

        vo.setId(dept.getId());

        vo.setParentId(dept.getParentId());

        vo.setDeptName(dept.getDeptName());

        vo.setDeptCode(dept.getDeptCode());

        vo.setSortNo(dept.getSortNo());

        vo.setStatus(dept.getStatus());

        return vo;

    }



    private SysMenuTreeVO toMenuTreeVO(SysMenu menu) {

        SysMenuTreeVO vo = new SysMenuTreeVO();

        vo.setId(menu.getId());

        vo.setParentId(menu.getParentId());

        vo.setMenuName(menu.getMenuName());

        vo.setMenuCode(menu.getMenuCode());

        vo.setPath(menu.getPath());

        return vo;

    }



    private String queryDeptName(Long deptId) {

        if (deptId == null) return null;

        SysDept dept = deptMapper.selectById(deptId);

        return dept != null ? dept.getDeptName() : null;

    }

}

