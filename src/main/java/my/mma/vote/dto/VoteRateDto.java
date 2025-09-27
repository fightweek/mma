package my.mma.vote.dto;

import lombok.Builder;

@Builder
public record VoteRateDto(long ffeId, double winnerVoteRate, double loserVoteRate) {}