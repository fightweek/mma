package my.mma.announcement.dto;

import jakarta.validation.constraints.NotBlank;

public record AnnouncementSaveRequest(@NotBlank String title, @NotBlank String content, boolean pinned) {
}
