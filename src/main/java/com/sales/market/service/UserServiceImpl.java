package com.sales.market.service;

import com.sales.market.model.Employee;
import com.sales.market.model.Role;
import com.sales.market.model.RoleType;
import com.sales.market.model.User;
import com.sales.market.repository.GenericRepository;
import com.sales.market.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends GenericServiceImpl<User> implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final EmployeeService employeeService;

    public UserServiceImpl(UserRepository userSystemRepository, EmployeeService employeeService) {
        this.userRepository = userSystemRepository;
        this.employeeService = employeeService;
    }

    @Override
    public Page<User> findUsers(Pageable pageable) {
        return userRepository.findAllWithRoles(pageable);
    }

    @Override
    @Transactional
    public User save(String firstName, String lastName, String email, String password) {
        Role role = new Role();
        Set<Role> roles = new HashSet<>();
        Employee employee = new Employee();
        User user = new User();

        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employeeService.save(employee);

        user.setEmail(email);
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        user.setEnabled(true);
        role.setId(RoleType.GENERAL.getId());
        roles.add(role);
        user.setRoles(roles);
        user.setEmployee(employee);
        return userRepository.saveAndFlush(user);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User findSystemUser() {
        return userRepository.findBySystemTrue();
    }

    @Override
    public User findById(Long id) {
        Optional<User> optional = userRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        throw new NoResultException();
    }

    @Override
    public User findOnlyEnabledByEmailWithRoles(String email) {
        return userRepository.findOnlyEnabledByEmailWithRoles(email);
    }

    @Override
    public boolean isUserRegistered(String email) {
        return findByEmail(email) != null;
    }

    @Override
    public int updatePasswordByEmail(String email, String newPassword) {
        return userRepository.updatePasswordByEmail(email, new BCryptPasswordEncoder().encode(newPassword));
    }

    @Override
    public UserDetails findUserDetails(String email) {
        User user = findOnlyEnabledByEmailWithRoles(email);
        user.setPassword("");
        return convertUserToUserDetails(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = findOnlyEnabledByEmailWithRoles(email);

        if (user == null) {
            throw new UsernameNotFoundException(email);
        }
        return convertUserToUserDetails(user);
    }

    @Override
    protected GenericRepository<User> getRepository() {
        return userRepository;
    }

    private UserDetails convertUserToUserDetails(User user) {
        List<SimpleGrantedAuthority> authorities = getAuthorities(user.getRoles());
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
    }

    private List<SimpleGrantedAuthority> getAuthorities(Collection<Role> roles) {
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public User update(User model) {
        User user = findById(model.getId());
        Employee employee = user.getEmployee();
        // Setting employee values
        employee.setFirstName(model.getEmployee().getFirstName());
        employee.setLastName(model.getEmployee().getLastName());
        employeeService.save(employee);
        // Setting user values
        user.getRoles().clear();
        for (Role role : model.getRoles()) {
            user.getRoles().add(role);
        }
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllByRoleEquals(RoleType roleType) {
        List<User> users = userRepository.findAll();
        List<User> usersFiltered = users.stream().filter(user -> haveRole(user.getRoles(), roleType)).collect(Collectors.toList());
        return usersFiltered;
    }

    private boolean haveRole(Set<Role> roles, RoleType roleType) {
        for (Role role: roles) {
            if(role.getId() == roleType.getId()) return true;
        }
        return false;
    }
}
