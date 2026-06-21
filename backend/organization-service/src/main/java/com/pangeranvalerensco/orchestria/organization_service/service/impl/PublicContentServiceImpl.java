package com.pangeranvalerensco.orchestria.organization_service.service.impl;

import com.pangeranvalerensco.orchestria.organization_service.entity.PublicContentEntry;
import com.pangeranvalerensco.orchestria.organization_service.entity.PublicContentType;
import com.pangeranvalerensco.orchestria.organization_service.entity.PublicationStatus;
import com.pangeranvalerensco.orchestria.organization_service.exception.BadRequestException;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.PublicContentRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.PublicContentResponse;
import com.pangeranvalerensco.orchestria.organization_service.repository.PublicContentEntryRepository;
import com.pangeranvalerensco.orchestria.organization_service.service.PublicContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicContentServiceImpl implements PublicContentService {

    private final PublicContentEntryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<PublicContentResponse> getAllContents() {
        return repository.findAll().stream()
                .map(PublicContentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicContentResponse> getContentsByType(PublicContentType type) {
        return repository.findAll().stream()
                .filter(c -> c.getContentType() == type && c.isActive())
                .map(PublicContentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicContentResponse> getPublishedContentsByType(PublicContentType type) {
        return repository.findByContentTypeAndActiveTrueAndPublicationStatusOrderByDisplayOrderAscPublishedAtDesc(type, PublicationStatus.PUBLISHED).stream()
                .map(PublicContentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicContentResponse> getPublishedActivitiesByCategory(String category) {
        return repository.findByContentTypeAndCategoryAndActiveTrueAndPublicationStatusOrderByDisplayOrderAscPublishedAtDesc(PublicContentType.ACTIVITY, category, PublicationStatus.PUBLISHED).stream()
                .map(PublicContentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PublicContentResponse createContent(PublicContentRequest request, String currentUserEmail) {
        validateRequest(request);

        PublicContentEntry entry = PublicContentEntry.builder()
                .contentType(request.getContentType())
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
                .body(request.getBody())
                .category(request.getCategory())
                .statusLabel(request.getStatusLabel())
                .eventDate(request.getEventDate())
                .mediaUrl(request.getMediaUrl())
                .linkUrl(request.getLinkUrl())
                .authorName(request.getAuthorName())
                .authorRole(request.getAuthorRole())
                .displayOrder(request.getDisplayOrder())
                .publicationStatus(PublicationStatus.DRAFT)
                .active(true)
                .createdByEmail(currentUserEmail)
                .updatedByEmail(currentUserEmail)
                .build();

        return PublicContentResponse.fromEntity(repository.save(entry));
    }

    @Override
    @Transactional
    public PublicContentResponse updateContent(String id, PublicContentRequest request, String currentUserEmail) {
        validateRequest(request);

        PublicContentEntry entry = getEntry(id);
        
        if (!entry.isActive()) {
            throw new BadRequestException("Konten sudah dihapus");
        }

        entry.setTitle(request.getTitle());
        entry.setSubtitle(request.getSubtitle());
        entry.setBody(request.getBody());
        entry.setCategory(request.getCategory());
        entry.setStatusLabel(request.getStatusLabel());
        entry.setEventDate(request.getEventDate());
        entry.setMediaUrl(request.getMediaUrl());
        entry.setLinkUrl(request.getLinkUrl());
        entry.setAuthorName(request.getAuthorName());
        entry.setAuthorRole(request.getAuthorRole());
        entry.setDisplayOrder(request.getDisplayOrder());
        entry.setUpdatedByEmail(currentUserEmail);

        return PublicContentResponse.fromEntity(repository.save(entry));
    }

    @Override
    @Transactional
    public PublicContentResponse publishContent(String id, String currentUserEmail) {
        PublicContentEntry entry = getEntry(id);
        
        if (!entry.isActive()) {
            throw new BadRequestException("Konten sudah dihapus");
        }
        
        if (entry.getPublicationStatus() == PublicationStatus.PUBLISHED) {
            throw new BadRequestException("Konten sudah dipublikasikan");
        }
        
        if (entry.getPublicationStatus() == PublicationStatus.ARCHIVED) {
            throw new BadRequestException("Konten sudah diarsipkan, tidak bisa langsung di-publish. Kembalikan ke DRAFT dulu.");
        }

        if (entry.getContentType() == PublicContentType.HERO) {
            long publishedHeroes = repository.countByContentTypeAndPublicationStatusAndActiveTrue(PublicContentType.HERO, PublicationStatus.PUBLISHED);
            if (publishedHeroes >= 1) {
                throw new BadRequestException("Hanya boleh ada 1 konten HERO yang PUBLISHED");
            }
        }

        entry.setPublicationStatus(PublicationStatus.PUBLISHED);
        entry.setPublishedAt(LocalDateTime.now());
        entry.setUpdatedByEmail(currentUserEmail);

        return PublicContentResponse.fromEntity(repository.save(entry));
    }

    @Override
    @Transactional
    public PublicContentResponse archiveContent(String id, String currentUserEmail) {
        PublicContentEntry entry = getEntry(id);
        
        if (!entry.isActive()) {
            throw new BadRequestException("Konten sudah dihapus");
        }
        
        if (entry.getPublicationStatus() == PublicationStatus.ARCHIVED) {
            throw new BadRequestException("Konten sudah diarsipkan");
        }

        entry.setPublicationStatus(PublicationStatus.ARCHIVED);
        entry.setUpdatedByEmail(currentUserEmail);

        return PublicContentResponse.fromEntity(repository.save(entry));
    }

    @Override
    @Transactional
    public PublicContentResponse restoreDraftContent(String id, String currentUserEmail) {
        PublicContentEntry entry = getEntry(id);
        
        if (!entry.isActive()) {
            throw new BadRequestException("Konten sudah dihapus");
        }
        
        if (entry.getPublicationStatus() == PublicationStatus.DRAFT) {
            throw new BadRequestException("Konten sudah DRAFT");
        }

        entry.setPublicationStatus(PublicationStatus.DRAFT);
        entry.setUpdatedByEmail(currentUserEmail);

        return PublicContentResponse.fromEntity(repository.save(entry));
    }

    @Override
    @Transactional
    public void deleteContent(String id) {
        PublicContentEntry entry = getEntry(id);
        if (entry.getPublicationStatus() == PublicationStatus.PUBLISHED) {
            throw new BadRequestException("Tidak dapat menghapus konten yang sedang PUBLISHED. Archive atau jadikan DRAFT terlebih dahulu.");
        }
        entry.setActive(false);
        repository.save(entry);
    }

    private PublicContentEntry getEntry(String id) {
        return repository.findById(id).orElseThrow(() -> new BadRequestException("Konten tidak ditemukan"));
    }

    private void validateRequest(PublicContentRequest request) {
        if (request.getDisplayOrder() == null || request.getDisplayOrder() < 0) {
            throw new BadRequestException("Urutan tampilan tidak boleh negatif");
        }

        if (StringUtils.hasText(request.getMediaUrl()) && !isValidUrl(request.getMediaUrl())) {
            throw new BadRequestException("URL media tidak valid");
        }
        
        if (StringUtils.hasText(request.getLinkUrl()) && !isValidUrl(request.getLinkUrl())) {
            throw new BadRequestException("URL tautan tidak valid");
        }

        switch (request.getContentType()) {
            case HERO:
                if (!StringUtils.hasText(request.getTitle())) throw new BadRequestException("Title wajib untuk konten HERO");
                if (!StringUtils.hasText(request.getMediaUrl())) throw new BadRequestException("Media URL wajib untuk konten HERO");
                break;
            case ABOUT:
            case VISION:
            case MISSION:
                if (!StringUtils.hasText(request.getBody())) throw new BadRequestException("Body wajib untuk konten " + request.getContentType());
                break;
            case PROGRAM:
            case FACILITY:
                if (!StringUtils.hasText(request.getTitle())) throw new BadRequestException("Title wajib untuk konten " + request.getContentType());
                if (!StringUtils.hasText(request.getBody())) throw new BadRequestException("Body wajib untuk konten " + request.getContentType());
                break;
            case TESTIMONIAL:
                if (!StringUtils.hasText(request.getAuthorName())) throw new BadRequestException("Nama penulis wajib untuk TESTIMONIAL");
                if (!StringUtils.hasText(request.getBody())) throw new BadRequestException("Isi testimoni wajib untuk TESTIMONIAL");
                break;
            case ACTIVITY:
                if (!StringUtils.hasText(request.getTitle())) throw new BadRequestException("Title wajib untuk ACTIVITY");
                if (!StringUtils.hasText(request.getBody())) throw new BadRequestException("Body wajib untuk ACTIVITY");
                if (request.getEventDate() == null) throw new BadRequestException("Tanggal aktivitas wajib untuk ACTIVITY");
                break;
            case MEDIA:
                if (!StringUtils.hasText(request.getMediaUrl())) throw new BadRequestException("Media URL wajib untuk konten MEDIA");
                break;
            default:
                throw new BadRequestException("Tipe konten tidak didukung");
        }
    }

    private boolean isValidUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
