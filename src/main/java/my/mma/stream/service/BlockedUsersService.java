package my.mma.stream.service;

import lombok.RequiredArgsConstructor;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.global.redis.utils.RedisUtils;
import my.mma.stream.dto.BlockedUserIdsDto;
import my.mma.stream.dto.BlockedUsersDto;
import my.mma.stream.dto.StreamUserDto;
import my.mma.user.entity.User;
import my.mma.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static my.mma.global.redis.prefix.RedisKeyPrefix.BLOCKED_USERS_PREFIX;

@Service
@RequiredArgsConstructor
public class BlockedUsersService {

    private final RedisUtils<BlockedUserIdsDto> blockedUsersRedisUtils;
    private final UserRepository userRepository;

    public BlockedUsersDto getBlockedUsers(String email) {
        User user = getUser(email);
        BlockedUserIdsDto blockedUserIds = blockedUsersRedisUtils.getData(BLOCKED_USERS_PREFIX.getPrefix() + user.getId());
        if (blockedUserIds == null)
            return new BlockedUsersDto(Collections.emptySet());
        List<User> blockedUsers = userRepository.findAllById(blockedUserIds.getBlockedUserIds());
        return new BlockedUsersDto(blockedUsers.stream()
                .map(blockedUser -> StreamUserDto.builder()
                        .id(blockedUser.getId())
                        .nickname(blockedUser.getNickname())
                        .build()).collect(Collectors.toSet()));
    }

    public void releaseBlock(String email, Long idToReleaseBlock) {
        User user = getUser(email);
        BlockedUserIdsDto blockedUserIds = blockedUsersRedisUtils.getData(BLOCKED_USERS_PREFIX.getPrefix() + user.getId());
        blockedUserIds.getBlockedUserIds().remove(idToReleaseBlock);
        blockedUsersRedisUtils.saveData(BLOCKED_USERS_PREFIX.getPrefix() + user.getId(), blockedUserIds);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(CustomErrorCode.NO_SUCH_USER_CONFIGURED_400)
        );
    }

}
