package com.ecosync.domain.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class BaseUpdated extends BaseCreated {
    private LocalDateTime updatedAt;
}
