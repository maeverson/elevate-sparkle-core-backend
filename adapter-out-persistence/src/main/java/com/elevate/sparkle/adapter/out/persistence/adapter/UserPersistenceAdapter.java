package com.elevate.sparkle.adapter.out.persistence.adapter;

import com.elevate.sparkle.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.elevate.sparkle.adapter.out.persistence.repository.UserJpaRepository;
import com.elevate.sparkle.application.port.out.UserRepositoryPort;
import com.elevate.sparkle.domain.model.User;
import com.elevate.sparkle.domain.valueobject.Email;
import com.elevate.sparkle.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementing UserRepositoryPort
 * Implements the output port using Spring Data JPA
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;
    private final UserPersistenceMapper mapper;

    @Override
    @Transactional
    public User save(User user) {
        log.debug("Saving user: {}", user.getId());
        var jpaEntity = mapper.toJpaEntity(user);
        var saved = userJpaRepository.save(jpaEntity);
        return mapper.toDomainEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UserId userId) {
        log.debug("Finding user by ID: {}", userId);
        return userJpaRepository.findById(userId.getValue())
                .map(mapper::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        return userJpaRepository.findByUsername(username)
                .map(mapper::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(Email email) {
        log.debug("Finding user by email: {}", email);
        return userJpaRepository.findByEmail(email.getValue())
                .map(mapper::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userJpaRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(Email email) {
        return userJpaRepository.existsByEmail(email.getValue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        log.debug("Finding all users");
        return userJpaRepository.findAll().stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllByTenantId(String tenantId) {
        log.debug("Finding users by tenant: {}", tenantId);
        return userJpaRepository.findAllByTenantId(tenantId).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(UserId userId) {
        log.debug("Deleting user: {}", userId);
        userJpaRepository.deleteById(userId.getValue());
    }
}
