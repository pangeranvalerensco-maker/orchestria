package com.pangeranvalerensco.orchestria.organization_service.archive;

import com.pangeranvalerensco.orchestria.organization_service.entity.ArchiveDocument;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.DocumentCategory;
import com.pangeranvalerensco.orchestria.organization_service.repository.ArchiveDocumentRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Integration tests for the Archive module inside organization-service.
 * Uses H2 in-memory database (profile=test) and a @TempDir for file storage.
 * Does NOT touch PostgreSQL or the real storage directory.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ArchiveDocumentControllerTest {

    private static final String BASE_URL = "/api/organization/archive/documents";
    private static final String JWT_SECRET =
            "orchestria-test-jwt-secret-key-12345678901234567890";

    @TempDir
    static Path tempStorageDir;

    /**
     * Override archive.storage.path to the JUnit @TempDir so tests never
     * write to the real storage directory.
     */
    @DynamicPropertySource
    static void overrideStoragePath(DynamicPropertyRegistry registry) {
        registry.add("archive.storage.path", () -> tempStorageDir.toAbsolutePath().toString());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArchiveDocumentRepository repository;

    private String tokenWithPermission;
    private String tokenWithoutPermission;

    @BeforeEach
    void setUp() {
        tokenWithPermission = buildToken("admin@test.com", List.of("archive.manage"));
        tokenWithoutPermission = buildToken("user@test.com", List.of("organization.read"));
    }

    // ── 1. Context load (covered by OrganizationServiceApplicationTests) ────

    // ── 2. 401 — list tanpa token ────────────────────────────────────────────
    @Test
    void givenNoToken_whenListDocuments_thenReturn401() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenNoToken_whenUpload_thenReturn401() throws Exception {
        mockMvc.perform(multipart(BASE_URL)
                        .file(makePdfFile())
                        .param("title", "Test")
                        .param("category", "LAPORAN"))
                .andExpect(status().isUnauthorized());
    }

    // ── 3. 403 — token tanpa archive.manage ─────────────────────────────────
    @Test
    void givenTokenWithoutPermission_whenListDocuments_thenReturn403() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", "Bearer " + tokenWithoutPermission))
                .andExpect(status().isForbidden());
    }

    @Test
    void givenTokenWithoutPermission_whenUpload_thenReturn403() throws Exception {
        mockMvc.perform(multipart(BASE_URL)
                        .file(makePdfFile())
                        .param("title", "Test")
                        .param("category", "LAPORAN")
                        .header("Authorization", "Bearer " + tokenWithoutPermission))
                .andExpect(status().isForbidden());
    }

    // ── 4. 200 — list dengan archive.manage ─────────────────────────────────
    @Test
    void givenPermission_whenListDocuments_thenReturn200WithArray() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", "Bearer " + tokenWithPermission))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ── 5. Upload valid — menyimpan metadata dan file fisik ─────────────────
    @Test
    void givenValidPdf_whenUpload_thenDocumentSavedAndPhysicalFileExists() throws Exception {
        mockMvc.perform(multipart(BASE_URL)
                        .file(makePdfFile())
                        .param("title", "Laporan Keuangan Q1")
                        .param("description", "Laporan triwulan pertama")
                        .param("category", "LAPORAN")
                        .header("Authorization", "Bearer " + tokenWithPermission))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.title").value("Laporan Keuangan Q1"));

        List<ArchiveDocument> all = repository.findAll();
        assertFalse(all.isEmpty(), "Dokumen harus tersimpan di database");

        ArchiveDocument saved = all.getLast();
        Path storedFile = tempStorageDir.resolve(saved.getStoredFileName());
        assertTrue(storedFile.toFile().exists(), "File fisik harus ada di storage: " + storedFile);
    }

    // ── 6. File kosong ditolak ───────────────────────────────────────────────
    @Test
    void givenEmptyFile_whenUpload_thenReturn400() throws Exception {
        MockMultipartFile empty = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]);

        mockMvc.perform(multipart(BASE_URL)
                        .file(empty)
                        .param("title", "Test")
                        .param("category", "LAPORAN")
                        .header("Authorization", "Bearer " + tokenWithPermission))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── 7. Content type terlarang ditolak ────────────────────────────────────
    @Test
    void givenDisallowedContentType_whenUpload_thenReturn415() throws Exception {
        MockMultipartFile exe = new MockMultipartFile(
                "file", "virus.exe", "application/octet-stream",
                "fake-binary".getBytes());

        mockMvc.perform(multipart(BASE_URL)
                        .file(exe)
                        .param("title", "Test")
                        .param("category", "LAINNYA")
                        .header("Authorization", "Bearer " + tokenWithPermission))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── 8. File terlalu besar ditolak ────────────────────────────────────────
    @Test
    void givenOversizedFile_whenUpload_thenReturn413() throws Exception {
        // 10MB + 1 byte
        byte[] big = new byte[10 * 1024 * 1024 + 1];
        MockMultipartFile bigFile = new MockMultipartFile(
                "file", "big.pdf", "application/pdf", big);

        mockMvc.perform(multipart(BASE_URL)
                        .file(bigFile)
                        .param("title", "Test")
                        .param("category", "LAPORAN")
                        .header("Authorization", "Bearer " + tokenWithPermission))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── 9. Stored filename bukan original filename ───────────────────────────
    @Test
    void givenValidUpload_storedFilenameMustBeUuidNotOriginal() throws Exception {
        mockMvc.perform(multipart(BASE_URL)
                        .file(makePdfFile())
                        .param("title", "UUID Test")
                        .param("category", "DOKUMENTASI")
                        .header("Authorization", "Bearer " + tokenWithPermission))
                .andExpect(status().isCreated());

        ArchiveDocument saved = repository.findAll().getLast();
        assertNotEquals("laporan-test.pdf", saved.getStoredFileName(),
                "storedFileName tidak boleh sama dengan original filename");
        assertTrue(saved.getStoredFileName().endsWith(".pdf"),
                "Extension harus tetap pdf");
        // UUID format validation (UUID v4 = 8-4-4-4-12 chars tanpa extension)
        String nameWithoutExt = saved.getStoredFileName().replace(".pdf", "");
        assertEquals(36, nameWithoutExt.length(),
                "storedFileName harus berformat UUID (36 karakter tanpa extension)");
    }

    // ── 10. Filter keyword ───────────────────────────────────────────────────
    @Test
    void givenKeyword_whenListDocuments_thenOnlyMatchingReturned() throws Exception {
        saveDocument("Notulen Rapat Divisi", DocumentCategory.NOTULEN);
        saveDocument("Laporan Keuangan Tahunan", DocumentCategory.LAPORAN);

        mockMvc.perform(get(BASE_URL)
                        .param("keyword", "notulen")
                        .header("Authorization", "Bearer " + tokenWithPermission))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].title",
                        containsStringIgnoringCase("notulen")));
    }

    // ── 11. Filter category ──────────────────────────────────────────────────
    @Test
    void givenCategoryFilter_whenListDocuments_thenOnlyCategoryReturned() throws Exception {
        saveDocument("Proposal Kegiatan A", DocumentCategory.PROPOSAL);
        saveDocument("Laporan Kegiatan B", DocumentCategory.LAPORAN);

        mockMvc.perform(get(BASE_URL)
                        .param("category", "PROPOSAL")
                        .header("Authorization", "Bearer " + tokenWithPermission))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].category").value("PROPOSAL"));
    }

    // ── 12. Download — byte dan header benar ────────────────────────────────
    @Test
    void givenUploadedDocument_whenDownload_thenReturnFileWithCorrectHeaders()
            throws Exception {
        // Upload dulu
        mockMvc.perform(multipart(BASE_URL)
                        .file(makePdfFile())
                        .param("title", "Download Test")
                        .param("category", "DOKUMENTASI")
                        .header("Authorization", "Bearer " + tokenWithPermission))
                .andExpect(status().isCreated());

        Long id = repository.findAll().getLast().getId();

        mockMvc.perform(get(BASE_URL + "/" + id + "/download")
                        .header("Authorization", "Bearer " + tokenWithPermission))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        containsString("attachment")))
                .andExpect(header().string("Content-Type",
                        containsString("application/pdf")));
    }

    // ── 13. Soft delete — hilang dari list ───────────────────────────────────
    @Test
    void givenExistingDocument_whenDelete_thenRemovedFromList() throws Exception {
        ArchiveDocument doc = saveDocument("Surat Masuk Penting", DocumentCategory.SURAT_MASUK);
        Long id = doc.getId();

        mockMvc.perform(delete(BASE_URL + "/" + id)
                        .header("Authorization", "Bearer " + tokenWithPermission))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", "Bearer " + tokenWithPermission))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == " + id + ")]").isEmpty());
    }

    // ── 14. Download dokumen deleted → 404 ──────────────────────────────────
    @Test
    void givenDeletedDocument_whenDownload_thenReturn404() throws Exception {
        ArchiveDocument doc = saveDocument("Surat Keluar Lama", DocumentCategory.SURAT_KELUAR);
        Long id = doc.getId();

        // Soft delete
        mockMvc.perform(delete(BASE_URL + "/" + id)
                        .header("Authorization", "Bearer " + tokenWithPermission))
                .andExpect(status().isOk());

        // Download harus 404
        mockMvc.perform(get(BASE_URL + "/" + id + "/download")
                        .header("Authorization", "Bearer " + tokenWithPermission))
                .andExpect(status().isNotFound());
    }

    // ── 15. Optimistic locking conflict → 409 ───────────────────────────────
    // Covered by ArchiveOptimisticLockTest — requires @MockitoBean to inject
    // ObjectOptimisticLockingFailureException from the service layer without
    // needing a real concurrent transaction (impossible in @Transactional test context).



    // ── Helpers ──────────────────────────────────────────────────────────────

    private MockMultipartFile makePdfFile() {
        return new MockMultipartFile(
                "file",
                "laporan-test.pdf",
                "application/pdf",
                "%PDF-1.4 fake-content-for-test".getBytes()
        );
    }

    private ArchiveDocument saveDocument(String title, DocumentCategory category) {
        ArchiveDocument doc = ArchiveDocument.builder()
                .title(title)
                .category(category)
                .originalFileName("test-doc.pdf")
                .storedFileName("test-stored-uuid.pdf")
                .contentType("application/pdf")
                .sizeBytes(1024L)
                .storageReference("test-stored-uuid.pdf")
                .uploadedByEmail("seeder@test.com")
                .uploadedAt(LocalDateTime.now())
                .deleted(false)
                .build();
        return repository.saveAndFlush(doc);
    }

    private String buildToken(String email, List<String> permissions) {
        Key key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(email)
                .claim("roles", List.of())
                .claim("permissions", permissions)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000L))
                .signWith(key)
                .compact();
    }
}
