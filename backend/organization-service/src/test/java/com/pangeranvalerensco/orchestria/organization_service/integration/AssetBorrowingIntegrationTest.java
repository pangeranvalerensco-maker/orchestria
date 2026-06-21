package com.pangeranvalerensco.orchestria.organization_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pangeranvalerensco.orchestria.organization_service.entity.Asset;
import com.pangeranvalerensco.orchestria.organization_service.entity.AssetBorrowing;
import com.pangeranvalerensco.orchestria.organization_service.entity.Member;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetStatus;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.BorrowingStatus;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.MemberStatus;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.asset.BorrowingCreateRequest;
import com.pangeranvalerensco.orchestria.organization_service.repository.AssetBorrowingRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.AssetRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AssetBorrowingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AssetBorrowingRepository assetBorrowingRepository;

    @AfterEach
    void tearDown() {
        assetBorrowingRepository.deleteAll();
        assetRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @WithMockUser(authorities = {"asset.borrow.create"}, username = "user@test.com")
    void createBorrowing_success() throws Exception {
        // Prepare member matching the mocked user (we assume test auth filter sets auth_user_id or email)
        // Wait, WithMockUser sets the username, the AssetAccessService expects to find Member by email or authUserId.
        // Let's create a Member with this email.
        Member member = Member.builder()
                .email("user@test.com")
                .fullName("Test User")
                .active(true)
                .status(MemberStatus.ACTIVE)
                .build();
        member = memberRepository.save(member);

        Asset asset = Asset.builder()
                .assetCode("A002")
                .assetName("Proyektor")
                .category("Elektronik")
                .currentStatus(AssetStatus.AVAILABLE)
                .active(true)
                .build();
        asset = assetRepository.save(asset);

        String requestJson = String.format("""
            {
                "assetId": "%s",
                "purpose": "Event PUB",
                "borrowDate": "%s",
                "expectedReturnDate": "%s"
            }
            """, asset.getId(), LocalDate.now(), LocalDate.now().plusDays(3));

        mockMvc.perform(post("/api/organization/asset-borrowings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.purpose").value("Event PUB"))
                .andExpect(jsonPath("$.data.borrowerMemberId").value(member.getId()));

        assertThat(assetBorrowingRepository.findAll()).hasSize(1);
    }
}
