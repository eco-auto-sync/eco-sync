package com.ecosync.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "subscriptions")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SubscriptionEntity extends BaseSoftDeleteEntity {

    @Comment("구독 PK")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("구독자 이메일")
    @Column(nullable = false, unique = true)
    private String email;

    @Comment("ICS URL 식별자 (UUID)")
    @Column(name = "calendar_token", nullable = false, unique = true, length = 36)
    private String calendarToken;
}
