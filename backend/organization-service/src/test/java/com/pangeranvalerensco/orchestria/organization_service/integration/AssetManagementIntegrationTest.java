package com.pangeranvalerensco.orchestria.organization_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pangeranvalerensco.orchestria.organization_service.entity.Asset;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetCondition;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetStatus;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.asset.AssetRequest;
import com.pangeranvalerensco.orchestria.organization_service.repository.AssetRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AssetManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AssetRepository assetRepository;

    private ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @AfterEach
    void tearDown() {
        assetRepository.deleteAll();
    }

    @Test
    @WithMockUser(authorities = {"asset.manage"})
    void createAsset_success() throws Exception {
        AssetRequest request = new AssetRequest(
                "A001", "MacBook Pro M3", "Elektronik", "Laptop",
                AssetCondition.GOOD, "Ruang IT", null, "https://example.com/img.png"
        );

        mockMvc.perform(post("/api/organization/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assetCode").value("A001"));

        assertThat(assetRepository.findAll()).hasSize(1);
    }

    @Test
    @WithMockUser(authorities = {"asset.manage"})
    void createAsset_duplicateCode_throwsException() throws Exception {
        Asset asset = Asset.builder()
                .assetCode("A001")
                .assetName("Laptop A")
                .category("Elektronik")
                .build();
        assetRepository.save(asset);

        AssetRequest request = new AssetRequest(
                "A001", "MacBook Pro M3", "Elektronik", "Laptop",
                AssetCondition.GOOD, "Ruang IT", null, "https://example.com/img.png"
        );

        mockMvc.perform(post("/api/organization/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Asset code sudah digunakan."));
    }

    @Test
    @WithMockUser(authorities = {"asset.manage"})
    void deleteAsset_success() throws Exception {
        Asset asset = Asset.builder()
                .assetCode("A001")
                .assetName("Laptop A")
                .category("Elektronik")
                .active(true)
                .build();
        asset = assetRepository.save(asset);

        mockMvc.perform(delete("/api/organization/assets/" + asset.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Asset updated = assetRepository.findById(asset.getId()).orElseThrow();
        assertThat(updated.isActive()).isFalse();
        assertThat(updated.getCurrentStatus()).isEqualTo(AssetStatus.INACTIVE);
    }
}
