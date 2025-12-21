package my.mma.stream.dto;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class BlockedUserIdsDto {
    private final Set<Long> blockedUserIds = new HashSet<>();
}
