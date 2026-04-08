package com.elevate.sparkle.adapter.out.persistence.mapper;

import com.elevate.sparkle.adapter.out.persistence.entity.UserJpaEntity;
import com.elevate.sparkle.domain.model.User;
import com.elevate.sparkle.domain.valueobject.Email;
import com.elevate.sparkle.domain.valueobject.UserId;
import com.elevate.sparkle.domain.valueobject.UserRole;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper between User domain entity and JPA entity
 * Critical for hexagonal architecture - maintains separation
 */
@Component
public class UserPersistenceMapper {

    public UserJpaEntity toJpaEntity(User user) {
        return UserJpaEntity.builder()
                .id(user.getId().getValue())
                .username(user.getUsername())
                .email(user.getEmail().getValue())
                .passwordHash(user.getPasswordHash())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(toStringRoles(user.getRoles()))
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public User toDomainEntity(UserJpaEntity jpaEntity) {
        return User.builder()
                .id(UserId.of(jpaEntity.getId()))
                .username(jpaEntity.getUsername())
                .email(Email.of(jpaEntity.getEmail()))
                .passwordHash(jpaEntity.getPasswordHash())
                .firstName(jpaEntity.getFirstName())
                .lastName(jpaEntity.getLastName())
                .roles(toUserRoles(jpaEntity.getRoles()))
                .active(jpaEntity.isActive())
                .createdAt(jpaEntity.getCreatedAt())
                .updatedAt(jpaEntity.getUpdatedAt())
                .build();
    }

    private Set<String> toStringRoles(Set<UserRole> roles) {
        return roles.stream()
                .map(UserRole::name)
                .collect(Collectors.toSet());
    }

    private Set<UserRole> toUserRoles(Set<String> roles) {
        return roles.stream()
                .map(UserRole::valueOf)
                .collect(Collectors.toSet());
    }
}
