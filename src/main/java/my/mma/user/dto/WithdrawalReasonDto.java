package my.mma.user.dto;

import my.mma.user.entity.WithdrawalReasonCategory;

public record WithdrawalReasonDto(WithdrawalReasonCategory category, String description) {
}
