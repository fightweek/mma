package my.mma.stream.dto;

import java.util.Set;

public record BlockedUsersDto(Set<StreamUserDto> blockedUsers) {
}
