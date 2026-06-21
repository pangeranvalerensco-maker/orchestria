package com.pangeranvalerensco.orchestria.organization_service.payload.response.english;

import com.pangeranvalerensco.orchestria.organization_service.entity.EnglishActivity;
import com.pangeranvalerensco.orchestria.organization_service.entity.EnglishDeposit;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.EnglishActivityStatus;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.EnglishDepositStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class EnglishResponses {

    @Data
    @Builder
    public static class ActivityResponse {
        private String id;
        private String title;
        private LocalDate activityDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String topic;
        private String description;
        private EnglishActivityStatus status;
        private String createdByEmail;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<DepositResponse> deposits;

        public static ActivityResponse fromEntity(EnglishActivity activity, List<EnglishDeposit> deposits) {
            return ActivityResponse.builder()
                    .id(activity.getId())
                    .title(activity.getTitle())
                    .activityDate(activity.getActivityDate())
                    .startTime(activity.getStartTime())
                    .endTime(activity.getEndTime())
                    .topic(activity.getTopic())
                    .description(activity.getDescription())
                    .status(activity.getStatus())
                    .createdByEmail(activity.getCreatedByEmail())
                    .createdAt(activity.getCreatedAt())
                    .updatedAt(activity.getUpdatedAt())
                    .deposits(deposits != null ? deposits.stream().map(DepositResponse::fromEntity).toList() : null)
                    .build();
        }
    }

    @Data
    @Builder
    public static class DepositResponse {
        private String id;
        private String activityId;
        private String activityTitle;
        private Long memberId;
        private String memberName;
        private String memberEmail;
        private String topic;
        private String evidenceUrl;
        private String submissionNote;
        private EnglishDepositStatus status;
        private BigDecimal score;
        private String verificationNote;
        private LocalDateTime submittedAt;
        private String verifiedByEmail;
        private LocalDateTime verifiedAt;

        public static DepositResponse fromEntity(EnglishDeposit deposit) {
            return DepositResponse.builder()
                    .id(deposit.getId())
                    .activityId(deposit.getActivity().getId())
                    .activityTitle(deposit.getActivity().getTitle())
                    .memberId(deposit.getMemberId())
                    .memberName(deposit.getMemberName())
                    .memberEmail(deposit.getMemberEmail())
                    .topic(deposit.getTopic())
                    .evidenceUrl(deposit.getEvidenceUrl())
                    .submissionNote(deposit.getSubmissionNote())
                    .status(deposit.getStatus())
                    .score(deposit.getScore())
                    .verificationNote(deposit.getVerificationNote())
                    .submittedAt(deposit.getSubmittedAt())
                    .verifiedByEmail(deposit.getVerifiedByEmail())
                    .verifiedAt(deposit.getVerifiedAt())
                    .build();
        }
    }

    @Data
    @Builder
    public static class ReportSummaryResponse {
        private long totalActivities;
        private long publishedActivities;
        private long completedActivities;
        private long totalDeposits;
        private long submittedDeposits;
        private long verifiedDeposits;
        private long rejectedDeposits;
        private long missedDeposits;
        private BigDecimal averageScore;
        private List<MemberSummary> memberSummary;
    }

    @Data
    @Builder
    public static class MemberSummary {
        private Long memberId;
        private String memberName;
        private long submittedCount;
        private long verifiedCount;
        private long rejectedCount;
        private BigDecimal averageScore;
    }
}
