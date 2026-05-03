package com.ecosync.domain.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class BaseSoftDelete extends BaseUpdated {
    private LocalDateTime deletedAt;

    public boolean isActive() {
        return deletedAt == null;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.deletedAt = null;
    }
}
