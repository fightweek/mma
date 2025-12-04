package my.mma.alert.dto;

import my.mma.alert.constant.AlertTarget;

import java.util.Set;

public record UserPreferencesDto(Set<AlertTarget> alertTargets) {
}
