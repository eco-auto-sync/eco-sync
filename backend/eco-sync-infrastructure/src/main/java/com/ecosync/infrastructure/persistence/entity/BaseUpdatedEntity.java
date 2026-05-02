package com.ecosync.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
public abstract class BaseUpdatedEntity extends BaseCreatedEntity {

    @Comment("수정자")
    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @Comment("수정일시")
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
