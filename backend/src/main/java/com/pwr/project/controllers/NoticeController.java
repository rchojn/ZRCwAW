package com.pwr.project.controllers;

import com.pwr.project.dto.NoticeDTO;
import com.pwr.project.entities.Notice;
import com.pwr.project.entities.datatypes.File;
import com.pwr.project.services.NoticeService;
import com.pwr.project.services.search.NoticeSearchService;
import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/notices")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private NoticeSearchService noticeSearchService;

    @PostMapping
    public ResponseEntity<NoticeDTO> createNotice(@RequestBody NoticeDTO noticeDTO) {
        NoticeDTO savedNotice = noticeService.createNotice(noticeDTO);
        return ResponseEntity.ok()
            .header("Access-Control-Allow-Origin", "http://localhost:4200")
            .header("Access-Control-Allow-Methods", "*")
            .header("Access-Control-Allow-Headers", "*")
            .header("Access-Control-Allow-Credentials", "true")
            .body(savedNotice);
    }

    @GetMapping("{id}")
    public ResponseEntity<NoticeDTO> getNoticeById(@PathVariable("id") Long noticeId) {
        NoticeDTO notice = noticeService.getNoticeById(noticeId);
        return ResponseEntity.ok()
            .header("Access-Control-Allow-Origin", "http://localhost:4200")
            .header("Access-Control-Allow-Methods", "*")
            .header("Access-Control-Allow-Headers", "*")
            .header("Access-Control-Allow-Credentials", "true")
            .body(notice);
    }

    @GetMapping
    public ResponseEntity<List<NoticeDTO>> getAllNotices() {
        List<NoticeDTO> notices = noticeService.getAllNotices();
        return ResponseEntity.ok(notices);
    }

    @PutMapping("{id}")
    public ResponseEntity<String> updateNotice(@PathVariable("id") Long noticeId, @RequestBody NoticeDTO notice) {
        try {
            notice.setId(noticeId);
            NoticeDTO updatedNotice = noticeService.updateNotice(notice);
            return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "http://localhost:4200")
                .header("Access-Control-Allow-Methods", "*")
                .header("Access-Control-Allow-Headers", "*")
                .header("Access-Control-Allow-Credentials", "true")
                .body(String.format("Notice %s has been updated successfully!", noticeId));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("Access-Control-Allow-Origin", "http://localhost:4200")
                .header("Access-Control-Allow-Methods", "*")
                .header("Access-Control-Allow-Headers", "*")
                .header("Access-Control-Allow-Credentials", "true")
                .body(String.format("You don't have right to update notice with ID: %s", noticeId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .header("Access-Control-Allow-Origin", "http://localhost:4200")
                .header("Access-Control-Allow-Methods", "*")
                .header("Access-Control-Allow-Headers", "*")
                .header("Access-Control-Allow-Credentials", "true")
                .body(String.format("Notice with ID: %s is not present in database", noticeId));
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteNotice(@PathVariable("id") Long noticeId) {
        try {
            noticeService.deleteNotice(noticeId);
            return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "http://localhost:4200")
                .header("Access-Control-Allow-Methods", "*")
                .header("Access-Control-Allow-Headers", "*")
                .header("Access-Control-Allow-Credentials", "true")
                .body(String.format("Notice with id: %s has been deleted successfully!", noticeId));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("Access-Control-Allow-Origin", "http://localhost:4200")
                .header("Access-Control-Allow-Methods", "*")
                .header("Access-Control-Allow-Headers", "*")
                .header("Access-Control-Allow-Credentials", "true")
                .body(String.format("You don't have right to delete notice with id: %s", noticeId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .header("Access-Control-Allow-Origin", "http://localhost:4200")
                .header("Access-Control-Allow-Methods", "*")
                .header("Access-Control-Allow-Headers", "*")
                .header("Access-Control-Allow-Credentials", "true")
                .body(String.format("Notice with id: %s does not exist in database", noticeId));
        }
    }

    @PostMapping("/{noticeId}/files")
    public ResponseEntity<NoticeDTO> addFilesToNotice(@PathVariable("noticeId") Long noticeId, @RequestBody List<File> files) {
        for (File file : files) {
            noticeService.addFileToNotice(noticeId, file);
        }
        NoticeDTO updatedNotice = noticeService.getNoticeById(noticeId);
        return ResponseEntity.ok()
            .header("Access-Control-Allow-Origin", "http://localhost:4200")
            .header("Access-Control-Allow-Methods", "*")
            .header("Access-Control-Allow-Headers", "*")
            .header("Access-Control-Allow-Credentials", "true")
            .body(updatedNotice);
    }

    @GetMapping("/{noticeId}/files")
    public ResponseEntity<List<File>> getAllFilesForNotice(@PathVariable("noticeId") Long noticeId) {
        List<File> files = noticeService.getAllFiles(noticeId);
        return ResponseEntity.ok()
            .header("Access-Control-Allow-Origin", "http://localhost:4200")
            .header("Access-Control-Allow-Methods", "*")
            .header("Access-Control-Allow-Headers", "*")
            .header("Access-Control-Allow-Credentials", "true")
            .body(files);
    }

    @GetMapping("/tags")
    public ResponseEntity<List<String>> populateTags() {
        List<String> tags = noticeService.getAllTags();
        return ResponseEntity.ok()
            .header("Access-Control-Allow-Origin", "http://localhost:4200")
            .header("Access-Control-Allow-Methods", "*")
            .header("Access-Control-Allow-Headers", "*")
            .header("Access-Control-Allow-Credentials", "true")
            .body(tags);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Notice>> searchNotices(@RequestParam String query) {
        return ResponseEntity.ok()
            .header("Access-Control-Allow-Origin", "http://localhost:4200")
            .header("Access-Control-Allow-Methods", "*")
            .header("Access-Control-Allow-Headers", "*")
            .header("Access-Control-Allow-Credentials", "true")
            .body(noticeSearchService.searchNotices(query));
    }
}
