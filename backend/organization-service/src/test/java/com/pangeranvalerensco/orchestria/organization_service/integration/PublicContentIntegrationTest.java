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

    // --- Scenario 1 to 4: Create HERO ---
    @Test
    @WithMockUser(authorities = {"public.organization.manage"}, username = "admin@test.com")
    void shouldCreateHeroContent() throws Exception {
        PublicContentRequest request = createValidRequest(PublicContentType.HERO);
        
        mockMvc.perform(post("/api/organization/public-content")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contentType").value("HERO"))
                .andExpect(jsonPath("$.data.publicationStatus").value("DRAFT"));
    }

    @Test
    @WithMockUser(authorities = {"public.organization.manage"})
    void shouldRejectHeroWithoutTitle() throws Exception {
        PublicContentRequest request = createValidRequest(PublicContentType.HERO);
        request.setTitle("");
        
        mockMvc.perform(post("/api/organization/public-content")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"public.organization.manage"})
    void shouldRejectHeroWithoutMediaUrl() throws Exception {
        PublicContentRequest request = createValidRequest(PublicContentType.HERO);
        request.setMediaUrl("");
        
        mockMvc.perform(post("/api/organization/public-content")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"public.content.manage"})
    void shouldDenyHeroCreateWithoutCorrectPermission() throws Exception {
        PublicContentRequest request = createValidRequest(PublicContentType.HERO);
        
        mockMvc.perform(post("/api/organization/public-content")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // --- Scenario 5: Create PROGRAM ---
    @Test
    @WithMockUser(authorities = {"public.content.manage"})
    void shouldCreateProgramContent() throws Exception {
        PublicContentRequest request = createValidRequest(PublicContentType.PROGRAM);
        
        mockMvc.perform(post("/api/organization/public-content")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // --- Scenario 6: Create TESTIMONIAL ---
    @Test
    @WithMockUser(authorities = {"public.content.manage"})
    void shouldCreateTestimonial() throws Exception {
        PublicContentRequest request = createValidRequest(PublicContentType.TESTIMONIAL);
        
        mockMvc.perform(post("/api/organization/public-content")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // --- Scenario 7: Create ACTIVITY ---
    @Test
    @WithMockUser(authorities = {"public.activity.manage"})
    void shouldCreateActivity() throws Exception {
        PublicContentRequest request = createValidRequest(PublicContentType.ACTIVITY);
        
        mockMvc.perform(post("/api/organization/public-content")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // --- Scenario 8: Update Content ---
    @Test
    @WithMockUser(authorities = {"public.organization.manage"})
    void shouldUpdateContent() throws Exception {
        PublicContentEntry entry = saveEntry(PublicContentType.ABOUT, PublicationStatus.DRAFT);
        PublicContentRequest request = createValidRequest(PublicContentType.ABOUT);
        request.setTitle("Updated Title");

        mockMvc.perform(put("/api/organization/public-content/" + entry.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Title"));
    }

    // --- Scenario 9: Publish Content ---
    @Test
    @WithMockUser(authorities = {"public.organization.manage"})
    void shouldPublishContent() throws Exception {
        PublicContentEntry entry = saveEntry(PublicContentType.ABOUT, PublicationStatus.DRAFT);

        mockMvc.perform(put("/api/organization/public-content/" + entry.getId() + "/publish")
                .param("type", "ABOUT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.publicationStatus").value("PUBLISHED"));
    }

    // --- Scenario 10: Reject Publish Already Published ---
    @Test
    @WithMockUser(authorities = {"public.organization.manage"})
    void shouldRejectPublishIfAlreadyPublished() throws Exception {
        PublicContentEntry entry = saveEntry(PublicContentType.ABOUT, PublicationStatus.PUBLISHED);

        mockMvc.perform(put("/api/organization/public-content/" + entry.getId() + "/publish")
                .param("type", "ABOUT"))
                .andExpect(status().isBadRequest());
    }

    // --- Scenario 11: Reject Publish HERO > 1 ---
    @Test
    @WithMockUser(authorities = {"public.organization.manage"})
    void shouldRejectPublishHeroIfLimitReached() throws Exception {
        saveEntry(PublicContentType.HERO, PublicationStatus.PUBLISHED);
        PublicContentEntry entry = saveEntry(PublicContentType.HERO, PublicationStatus.DRAFT);

        mockMvc.perform(put("/api/organization/public-content/" + entry.getId() + "/publish")
                .param("type", "HERO"))
                .andExpect(status().isBadRequest());
    }

    // --- Scenario 12: Archive Content ---
    @Test
    @WithMockUser(authorities = {"public.organization.manage"})
    void shouldArchiveContent() throws Exception {
        PublicContentEntry entry = saveEntry(PublicContentType.ABOUT, PublicationStatus.PUBLISHED);

        mockMvc.perform(put("/api/organization/public-content/" + entry.getId() + "/archive")
                .param("type", "ABOUT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.publicationStatus").value("ARCHIVED"));
    }

    // --- Scenario 13: Reject Archive Already Archived ---
    @Test
    @WithMockUser(authorities = {"public.organization.manage"})
    void shouldRejectArchiveAlreadyArchived() throws Exception {
        PublicContentEntry entry = saveEntry(PublicContentType.ABOUT, PublicationStatus.ARCHIVED);

        mockMvc.perform(put("/api/organization/public-content/" + entry.getId() + "/archive")
                .param("type", "ABOUT"))
                .andExpect(status().isBadRequest());
    }

    // --- Scenario 14: Restore Draft ---
    @Test
    @WithMockUser(authorities = {"public.organization.manage"})
    void shouldRestoreDraft() throws Exception {
        PublicContentEntry entry = saveEntry(PublicContentType.ABOUT, PublicationStatus.ARCHIVED);

        mockMvc.perform(put("/api/organization/public-content/" + entry.getId() + "/restore")
                .param("type", "ABOUT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.publicationStatus").value("DRAFT"));
    }

    // --- Scenario 15: Delete Content ---
    @Test
    @WithMockUser(authorities = {"public.organization.manage"})
    void shouldDeleteDraftContent() throws Exception {
        PublicContentEntry entry = saveEntry(PublicContentType.ABOUT, PublicationStatus.DRAFT);

        mockMvc.perform(delete("/api/organization/public-content/" + entry.getId())
                .param("type", "ABOUT"))
                .andExpect(status().isOk());
                
        assertFalse(repository.findById(entry.getId()).get().isActive());
    }

    // --- Scenario 16: Reject Delete Published ---
    @Test
    @WithMockUser(authorities = {"public.organization.manage"})
    void shouldRejectDeletePublishedContent() throws Exception {
        PublicContentEntry entry = saveEntry(PublicContentType.ABOUT, PublicationStatus.PUBLISHED);

        mockMvc.perform(delete("/api/organization/public-content/" + entry.getId())
                .param("type", "ABOUT"))
                .andExpect(status().isBadRequest());
    }

    // --- Scenario 17: Negative Display Order ---
    @Test
    @WithMockUser(authorities = {"public.organization.manage"})
    void shouldRejectNegativeDisplayOrder() throws Exception {
        PublicContentRequest request = createValidRequest(PublicContentType.ABOUT);
        request.setDisplayOrder(-1);

        mockMvc.perform(post("/api/organization/public-content")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // --- Scenario 18: Invalid URL ---
    @Test
    @WithMockUser(authorities = {"public.organization.manage"})
    void shouldRejectInvalidMediaUrl() throws Exception {
        PublicContentRequest request = createValidRequest(PublicContentType.HERO);
        request.setMediaUrl("invalid-url");

        mockMvc.perform(post("/api/organization/public-content")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // --- Scenario 19: Get All Contents Admin ---
    @Test
    @WithMockUser(authorities = {"public.content.read"})
    void shouldGetAllContentsAdmin() throws Exception {
        saveEntry(PublicContentType.ABOUT, PublicationStatus.DRAFT);
        saveEntry(PublicContentType.HERO, PublicationStatus.PUBLISHED);

        mockMvc.perform(get("/api/organization/public-content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    // --- Scenario 20: Get Contents By Type Admin ---
    @Test
    @WithMockUser(authorities = {"public.content.read"})
    void shouldGetContentsByTypeAdmin() throws Exception {
        saveEntry(PublicContentType.ABOUT, PublicationStatus.DRAFT);
        saveEntry(PublicContentType.ABOUT, PublicationStatus.PUBLISHED);
        saveEntry(PublicContentType.HERO, PublicationStatus.PUBLISHED);

        mockMvc.perform(get("/api/organization/public-content/type/ABOUT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    // --- Scenario 21: Get Published By Type Public ---
    @Test
    void shouldGetPublishedContentsByTypePublic() throws Exception {
        saveEntry(PublicContentType.ABOUT, PublicationStatus.DRAFT);
        saveEntry(PublicContentType.ABOUT, PublicationStatus.PUBLISHED);
        saveEntry(PublicContentType.HERO, PublicationStatus.PUBLISHED);

        mockMvc.perform(get("/api/organization/public/content/type/ABOUT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    // --- Scenario 22: Reject Create Media Without Url ---
    @Test
    @WithMockUser(authorities = {"public.media.manage"})
    void shouldRejectMediaWithoutUrl() throws Exception {
        PublicContentRequest request = createValidRequest(PublicContentType.MEDIA);
        request.setMediaUrl("");

        mockMvc.perform(post("/api/organization/public-content")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // --- Scenario 23: Get Activities By Category Public ---
    @Test
    void shouldGetPublishedActivitiesByCategoryPublic() throws Exception {
        PublicContentEntry entry1 = saveEntry(PublicContentType.ACTIVITY, PublicationStatus.PUBLISHED);
        entry1.setCategory("WORKSHOP");
        repository.save(entry1);
        
        PublicContentEntry entry2 = saveEntry(PublicContentType.ACTIVITY, PublicationStatus.DRAFT);
        entry2.setCategory("WORKSHOP");
        repository.save(entry2);
        
        PublicContentEntry entry3 = saveEntry(PublicContentType.ACTIVITY, PublicationStatus.PUBLISHED);
        entry3.setCategory("SEMINAR");
        repository.save(entry3);

        mockMvc.perform(get("/api/organization/public/content/activities")
                .param("category", "WORKSHOP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].category").value("WORKSHOP"));
    }
}
