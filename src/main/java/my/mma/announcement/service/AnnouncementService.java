package my.mma.announcement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.announcement.dto.AnnouncementContentDto;
import my.mma.announcement.dto.AnnouncementDto;
import my.mma.announcement.dto.AnnouncementSaveRequest;
import my.mma.announcement.entity.Announcement;
import my.mma.announcement.repository.AnnounceRepository;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnnouncementService {

    private final AnnounceRepository announceRepository;

    @Transactional
    public void save(AnnouncementSaveRequest request) {
        announceRepository.save(Announcement.builder()
                .title(request.title())
                .content(request.content())
                .build());
    }

    public AnnouncementContentDto getAnnouncement(Long id){
        Announcement announcement = announceRepository.findById(id).orElseThrow(
                () -> new CustomException(CustomErrorCode.BAD_REQUEST_400)
        );
        return new AnnouncementContentDto(announcement.getContent());
    }

    public Page<AnnouncementDto> getAnnouncements(Pageable pageable){
        Page<Announcement> announcements = announceRepository.findAll(pageable);
        return announcements.map(
                announcement -> AnnouncementDto.builder()
                        .id(announcement.getId())
                        .title(announcement.getTitle())
                        .pinned(announcement.isPinned())
                        .createdDate(announcement.getCreatedDateTime().toLocalDate())
                        .build()
        );
    }

}
