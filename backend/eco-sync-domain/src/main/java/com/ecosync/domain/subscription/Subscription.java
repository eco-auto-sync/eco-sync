package com.ecosync.domain.subscription;

import com.ecosync.domain.common.BaseSoftDelete;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@SuperBuilder
@NoArgsConstructor
public class Subscription extends BaseSoftDelete {
    private Long id;
    private String email;
    private String calendarToken;

    public static Subscription create(String email) {
        return Subscription.builder()
                .email(email)
                .calendarToken(UUID.randomUUID().toString())
                .build();
    }
}
