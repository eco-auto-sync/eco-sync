package com.ecosync.domain.subscription;

import com.ecosync.domain.common.BaseSoftDelete;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class SubscriptionInterest extends BaseSoftDelete {
    private Long id;
    private Long subscriptionId;
    private InterestType interestType;
    private String interestValue;
}
