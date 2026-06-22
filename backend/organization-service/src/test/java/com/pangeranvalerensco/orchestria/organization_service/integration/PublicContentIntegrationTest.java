package com.pangeranvalerensco.orchestria.organization_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pangeranvalerensco.orchestria.organization_service.entity.PublicContentEntry;
import com.pangeranvalerensco.orchestria.organization_service.entity.PublicContentType;
import com.pangeranvalerensco.orchestria.organization_service.entity.PublicationStatus;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.PublicContentRequest;
import com.pangeranvalerensco.orchestria.organization_service.repository.PublicContentEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PublicContentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;
    {
        objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.module.SimpleModule module = new com.fasterxml.jackson.databind.module.SimpleModule();
        module.addSerializer(java.time.LocalDate.class, new com.fasterxml.jackson.databind.JsonSerializer<java.time.LocalDate>() {
            @Override
            public void serialize(java.time.LocalDate value, com.fasterxml.jackson.core.JsonGenerator gen, com.fasterxml.jackson.databind.SerializerProvider serializers) throws java.io.IOException {
                gen.writeString(value.toString());
            }
        });
        objectMapper.registerModule(module);
    }

    @Autowired
    private PublicContentEntryRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    private PublicContentRequest createValidRequest(PublicContentType type) {
        return PublicContentRequest.builder()
                .contentType(type)
                .title("Valid Title")
                .subtitle("Valid Subtitle")
                .body("Valid Body")
                .category("Valid Category")
                .statusLabel("New")
                .eventDate(LocalDate.now())
                .mediaUrl("http://example.com/image.jpg")
                .linkUrl("http://example.com/link")
                .authorName("Author Name")
                .authorRole("Author Role")
                .displayOrder(1)
                .build();
    }

    private PublicContentEntry saveEntry(PublicContentType type, PublicationStatus status) {
        PublicContentEntry entry = PublicContentEntry.builder()
                .contentType(type)
                .title("Test Title")
                .body("Test Body")
                .category("Test Category")
                .authorName("Test Author")
                .eventDate(LocalDate.now())
                .mediaUrl("http://example.com/image.jpg")
                .displayOrder(1)
                .publicationStatus(status)
                .active(true)
                .createdByEmail("admin@test.com")
                .updatedByEmail("admin@test.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        if (status == PublicationStatus.PUBLISHED) {
            entry.setPublishedAt(LocalDateTime.now());
        }
        return repository.save(entry);
    }

    // 1. public list tanpa token
    @Test
    void shouldGetPublicListWithoutToken() throws Exception {
        saveEntry(PublicContentType.ABOUT, PublicationStatus.PUBLISHED);

        mockMvc.perform(get("/api/organization/public/content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    // 2. public detail tanpa token
    @Test
    void shouldGetPublicDetailWithoutToken() throws Exception {
        PublicContentEntry entry = saveEntry(PublicContentType.ABOUT, PublicationStatus.PUBLISHED);

        mockMvc.perform(get("/api/organization/public/content/" + entry.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(entry.getId()));
    }

    // 3. public hanya PUBLISHED aktif
    @Test
    void shouldOnlyGetActivePublishedContentsForPublic() throws Exception {
        saveEntry(PublicContentType.ABOUT, PublicationStatus.DRAFT);
        saveEntry(PublicContentType.PROGRAM, PublicationStatus.ARCHIVED);
        PublicContentEntry activeEntry = saveEntry(PublicContentType.FACILITY, PublicationStatus.PUBLISHED);
        PublicContentEntry inactiveEntry = saveEntry(PublicContentType.TESTIMONIAL, PublicationStatus.PUBLISHED);
        inactiveEntry.setActive(false);
        repository.save(inactiveEntry);

        mockMvc.perform(get("/api/organization/public/content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(activeEntry.getId()));
    }

    // 4. sorting displayOrder
    @Test
    void shouldSortByDisplayOrderAscThenPublishedAtDesc() throws Exception {
        PublicContentEntry e1 = saveEntry(PublicContentType.ABOUT, PublicationStatus.PUBLISHED);
        e1.setDisplayOrder(2);
        e1.setPublishedAt(LocalDateTime.now().minusDays(1));
        repository.save(e1);

        PublicContentEntry e2 = saveEntry(PublicContentType.ABOUT, PublicationStatus.PUBLISHED);
        e2.setDisplayOrder(1);
        e2.setPublishedAt(LocalDateTime.now().minusDays(2));
        repository.save(e2);

        PublicContentEntry e3 = saveEntry(PublicContentType.ABOUT, PublicationStatus.PUBLISHED);
        e3.setDisplayOrder(1);
        e3.setPublishedAt(LocalDateTime.now());
        repository.save(e3);

        mockMvc.perform(get("/api/organization/public/content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].id").value(e3.getId())) // order 1, newest
                .andExpect(jsonPath("$.data[1].id").value(e2.getId())) // order 1, older
                .andExpect(jsonPath("$.data[2].id").value(e1.getId())); // order 2
    }

    // 5. admin list filter type/status/active
    @Test
    @WithMockUser(authorities = {"public.content.read"})
    void shouldFilterAdminList() throws Exception {
        saveEntry(PublicContentType.ABOUT, PublicationStatus.DRAFT);
        saveEntry(PublicContentType.HERO, PublicationStatus.PUBLISHED);

        mockMvc.perform(get("/api/organization/public-content")
                .param("type", "ABOUT")
                .param("status", "DRAFT")
                .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].contentType").value("ABOUT"));
    }

    // 6. admin detail
    @Test
    @WithMockUser(authorities = {"public.content.manage"})
    void shouldGetAdminDetail() throws Exception {
        PublicContentEntry entry = saveEntry(PublicContentType.ABOUT, PublicationStatus.DRAFT);

        mockMvc.perform(get("/api/organization/public-content/" + entry.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(entry.getId()));
    }

    // 7. permission spoofing empat kasus
    // a. user public.activity.manage tidak dapat update HERO dengan request contentType ACTIVITY
    @Test
    @WithMockUser(authorities = {"public.activity.manage"})
    void spoof1_activityManageCannotUpdateHeroWithActivityType() throws Exception {
        PublicContentEntry entry = saveEntry(PublicContentType.HERO, PublicationStatus.DRAFT);
        PublicContentRequest request = createValidRequest(PublicContentType.ACTIVITY);
        
        mockMvc.perform(put("/api/organization/public-content/" + entry.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // b. user public.activity.manage tidak dapat publish HERO
    @Test
    @WithMockUser(authorities = {"public.activity.manage"})
    void spoof2_activityManageCannotPublishHero() throws Exception {
        PublicContentEntry entry = saveEntry(PublicContentType.HERO, PublicationStatus.DRAFT);
        
        mockMvc.perform(post("/api/organization/public-content/" + entry.getId() + "/publish"))
                .andExpect(status().isForbidden());
    }

    // c. user public.content.manage tidak dapat archive MEDIA
    @Test
    @WithMockUser(authorities = {"public.content.manage"})
    void spoof3_contentManageCannotArchiveMedia() throws Exception {
        PublicContentEntry entry = saveEntry(PublicContentType.MEDIA, PublicationStatus.PUBLISHED);
        
        mockMvc.perform(post("/api/organization/public-content/" + entry.getId() + "/archive"))
                .andExpect(status().isForbidden());
    }

    // d. user public.organization.manage tidak dapat delete ACTIVITY
    @Test
    @WithMockUser(authorities = {"public.organization.manage"})
    void spoof4_organizationManageCannotDeleteActivity() throws Exception {
        PublicContentEntry entry = saveEntry(PublicContentType.ACTIVITY, PublicationStatus.DRAFT);
        
        mockMvc.perform(delete("/api/organization/public-content/" + entry.getId()))
                .andExpect(status().isForbidden());
    }

    // 8. update PUBLISHED ditolak
    @Test
    @WithMockUser(authorities = {"public.organization.manage"})
    void shouldRejectUpdateOnPublished() throws Exception {
        PublicContentEntry entry = saveEntry(PublicContentType.ABOUT, PublicationStatus.PUBLISHED);
        PublicContentRequest request = createValidRequest(PublicContentType.ABOUT);

        mockMvc.perform(put("/api/organization/public-content/" + entry.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // 9. restore PUBLISHED ditolak
    @Test
    @WithMockUser(authorities = {"public.organization.manage"})
    void shouldRejectRestoreOnPublished() throws Exception {
        PublicContentEntry entry = saveEntry(PublicContentType.ABOUT, PublicationStatus.PUBLISHED);

        mockMvc.perform(post("/api/organization/public-content/" + entry.getId() + "/restore-draft"))
                .andExpect(status().isBadRequest());
    }

    // 10. restore ARCHIVED berhasil
    @Test
    @WithMockUser(authorities = {"public.organization.manage"})
    void shouldRestoreArchivedToDraft() throws Exception {
        PublicContentEntry entry = saveEntry(PublicContentType.ABOUT, PublicationStatus.ARCHIVED);

        mockMvc.perform(post("/api/organization/public-content/" + entry.getId() + "/restore-draft"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.publicationStatus").value("DRAFT"));
    }

    // 11. delete PUBLISHED menjadi ARCHIVED
    @Test
    @WithMockUser(authorities = {"public.organization.manage"})
    void shouldDeletePublishedToArchived() throws Exception {
        PublicContentEntry entry = saveEntry(PublicContentType.ABOUT, PublicationStatus.PUBLISHED);

        mockMvc.perform(delete("/api/organization/public-content/" + entry.getId()))
                .andExpect(status().isOk());
                
        PublicContentEntry dbEntry = repository.findById(entry.getId()).get();
        assertTrue(dbEntry.isActive());
        assertEquals(PublicationStatus.ARCHIVED, dbEntry.getPublicationStatus());
    }

    // 12. delete DRAFT soft delete
    @Test
    @WithMockUser(authorities = {"public.organization.manage"})
    void shouldSoftDeleteDraft() throws Exception {
        PublicContentEntry entry = saveEntry(PublicContentType.ABOUT, PublicationStatus.DRAFT);

        mockMvc.perform(delete("/api/organization/public-content/" + entry.getId()))
                .andExpect(status().isOk());
                
        PublicContentEntry dbEntry = repository.findById(entry.getId()).get();
        assertFalse(dbEntry.isActive());
    }

    // 13. URL ftp ditolak
    @Test
    @WithMockUser(authorities = {"public.organization.manage"})
    void shouldRejectFtpUrl() throws Exception {
        PublicContentRequest request = createValidRequest(PublicContentType.ABOUT);
        request.setMediaUrl("ftp://example.com/file");

        mockMvc.perform(post("/api/organization/public-content")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // 14. ABOUT tanpa title ditolak
    @Test
    @WithMockUser(authorities = {"public.organization.manage"})
    void shouldRejectAboutWithoutTitle() throws Exception {
        PublicContentRequest request = createValidRequest(PublicContentType.ABOUT);
        request.setTitle("");

        mockMvc.perform(post("/api/organization/public-content")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // 15. ACTIVITY tanpa category ditolak
    @Test
    @WithMockUser(authorities = {"public.activity.manage"})
    void shouldRejectActivityWithoutCategory() throws Exception {
        PublicContentRequest request = createValidRequest(PublicContentType.ACTIVITY);
        request.setCategory("");

        mockMvc.perform(post("/api/organization/public-content")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // 16. MEDIA tanpa title ditolak
    @Test
    @WithMockUser(authorities = {"public.media.manage"})
    void shouldRejectMediaWithoutTitle() throws Exception {
        PublicContentRequest request = createValidRequest(PublicContentType.MEDIA);
        request.setTitle("");

        mockMvc.perform(post("/api/organization/public-content")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // 17. HERO kedua tidak dapat dipublish
    @Test
    @WithMockUser(authorities = {"public.organization.manage"})
    void shouldRejectPublishSecondHero() throws Exception {
        saveEntry(PublicContentType.HERO, PublicationStatus.PUBLISHED);
        PublicContentEntry entry = saveEntry(PublicContentType.HERO, PublicationStatus.DRAFT);

        mockMvc.perform(post("/api/organization/public-content/" + entry.getId() + "/publish"))
                .andExpect(status().isBadRequest());
    }
}
