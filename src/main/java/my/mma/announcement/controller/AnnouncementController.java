package my.mma.announcement.controller;

import lombok.RequiredArgsConstructor;
import my.mma.announcement.dto.AnnouncementContentDto;
import my.mma.announcement.dto.AnnouncementDto;
import my.mma.announcement.dto.AnnouncementSaveRequest;
import my.mma.announcement.service.AnnouncementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("")
    public ResponseEntity<Void> save(
            @RequestBody @Validated AnnouncementSaveRequest request
    ) {
        announcementService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @GetMapping("/announcements")
    public ResponseEntity<Page<AnnouncementDto>> getAnnouncements(
            @PageableDefault(sort = {"pinned", "createdDateTime"}, direction = DESC) Pageable pageable
    ){
        return ResponseEntity.ok().body(announcementService.getAnnouncements(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnnouncementContentDto> getAnnouncement(
            @PathVariable("id") Long id
    ){
        return ResponseEntity.ok().body(announcementService.getAnnouncement(id));
    }

}
