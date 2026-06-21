package com.pangeranvalerensco.orchestria.organization_service.service;

import com.pangeranvalerensco.orchestria.organization_service.entity.EnglishActivity;
import com.pangeranvalerensco.orchestria.organization_service.entity.EnglishDeposit;
import com.pangeranvalerensco.orchestria.organization_service.entity.Member;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.EnglishActivityStatus;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.EnglishDepositStatus;
import com.pangeranvalerensco.orchestria.organization_service.exception.BadRequestException;
import com.pangeranvalerensco.orchestria.organization_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.english.EnglishActivityRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.english.EnglishDepositRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.english.EnglishDepositVerificationRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.english.EnglishResponses.*;
import com.pangeranvalerensco.orchestria.organization_service.repository.EnglishActivityRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.EnglishDepositRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnglishService {

    private final EnglishActivityRepository activityRepository;
    private final EnglishDepositRepository depositRepository;
    private final MemberRepository memberRepository;

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new AccessDeniedException("User tidak terautentikasi");
        }
        return auth.getName();
    }

    private boolean canReadAllActivities() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("english.activity.manage") ||
                               a.equals("english.deposit.read.all") ||
                               a.equals("english.report.read") ||
                               a.equals("ROLE_SUPER_ADMIN"));
    }

    private boolean canVerifyDeposit() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("english.deposit.verify") || a.equals("ROLE_SUPER_ADMIN"));
    }

    private boolean canReadAllDeposits() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("english.deposit.read.all") || a.equals("ROLE_SUPER_ADMIN"));
    }

    // ==========================================
    // ACTIVITY MANAGEMENT
    // ==========================================

    @Transactional
    public ActivityResponse createActivity(EnglishActivityRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("Waktu selesai harus setelah waktu mulai.");
        }

        EnglishActivity activity = EnglishActivity.builder()
                .title(request.title())
                .activityDate(request.activityDate())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .topic(request.topic())
                .description(request.description())
                .status(request.status())
                .createdByEmail(getCurrentUserEmail())
                .active(true)
                .build();

        return ActivityResponse.fromEntity(activityRepository.save(activity), null);
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> getAllActivities() {
        if (canReadAllActivities()) {
            return activityRepository.findByActiveTrueOrderByActivityDateDescStartTimeDesc().stream()
                    .map(a -> ActivityResponse.fromEntity(a, null))
                    .toList();
        } else {
            return activityRepository.findByActiveTrueAndStatusOrderByActivityDateDescStartTimeDesc(EnglishActivityStatus.PUBLISHED).stream()
                    .map(a -> ActivityResponse.fromEntity(a, null))
                    .toList();
        }
    }

    @Transactional(readOnly = true)
    public ActivityResponse getActivity(String id) {
        EnglishActivity activity = activityRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity tidak ditemukan"));

        if (!canReadAllActivities() && activity.getStatus() != EnglishActivityStatus.PUBLISHED) {
            throw new AccessDeniedException("Anda tidak dapat melihat activity ini");
        }

        List<EnglishDeposit> deposits = new ArrayList<>();
        if (canReadAllDeposits() || canVerifyDeposit()) {
            deposits = depositRepository.findByActivityIdAndActiveTrue(id);
        } else {
            // only own deposit
            String email = getCurrentUserEmail();
            Member member = memberRepository.findByEmailAndActiveTrue(email).orElse(null);
            if (member != null) {
                depositRepository.findByActivityIdAndMemberIdAndActiveTrue(id, member.getId()).ifPresent(deposits::add);
            }
        }

        return ActivityResponse.fromEntity(activity, deposits);
    }

    @Transactional
    public ActivityResponse updateActivity(String id, EnglishActivityRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("Waktu selesai harus setelah waktu mulai.");
        }

        EnglishActivity activity = activityRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity tidak ditemukan"));

        activity.setTitle(request.title());
        activity.setActivityDate(request.activityDate());
        activity.setStartTime(request.startTime());
        activity.setEndTime(request.endTime());
        activity.setTopic(request.topic());
        activity.setDescription(request.description());
        activity.setStatus(request.status());

        return ActivityResponse.fromEntity(activityRepository.save(activity), null);
    }

    @Transactional
    public void deleteActivity(String id) {
        EnglishActivity activity = activityRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity tidak ditemukan"));

        if (activity.getStatus() == EnglishActivityStatus.DRAFT) {
            activity.setActive(false);
        } else {
            activity.setStatus(EnglishActivityStatus.CANCELLED);
        }
        activityRepository.save(activity);
    }

    // ==========================================
    // DEPOSIT MANAGEMENT
    // ==========================================

    @Transactional
    public DepositResponse createDeposit(EnglishDepositRequest request) {
        EnglishActivity activity = activityRepository.findByIdAndActiveTrue(request.activityId())
                .orElseThrow(() -> new ResourceNotFoundException("Activity tidak ditemukan"));

        if (activity.getStatus() != EnglishActivityStatus.PUBLISHED) {
            throw new BadRequestException("Hanya activity PUBLISHED yang dapat menerima setoran.");
        }

        String email = getCurrentUserEmail();
        Member member = memberRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new BadRequestException("Member tidak aktif atau tidak ditemukan."));

        if (depositRepository.existsByActivityIdAndMemberIdAndActiveTrue(activity.getId(), member.getId())) {
            throw new BadRequestException("Anda sudah membuat setoran untuk activity ini.");
        }

        EnglishDeposit deposit = EnglishDeposit.builder()
                .activity(activity)
                .memberId(member.getId())
                .memberName(member.getFullName())
                .memberEmail(member.getEmail())
                .topic(request.topic())
                .evidenceUrl(request.evidenceUrl())
                .submissionNote(request.submissionNote())
                .status(EnglishDepositStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .active(true)
                .build();

        return DepositResponse.fromEntity(depositRepository.save(deposit));
    }

    @Transactional(readOnly = true)
    public List<DepositResponse> getAllDeposits() {
        return depositRepository.findByActiveTrueOrderBySubmittedAtDesc().stream()
                .map(DepositResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DepositResponse> getMyDeposits() {
        String email = getCurrentUserEmail();
        Member member = memberRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new BadRequestException("Member tidak ditemukan."));
        return depositRepository.findByMemberIdAndActiveTrueOrderBySubmittedAtDesc(member.getId()).stream()
                .map(DepositResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepositResponse getDeposit(String id) {
        EnglishDeposit deposit = depositRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit tidak ditemukan"));

        if (!canReadAllDeposits() && !deposit.getMemberEmail().equals(getCurrentUserEmail())) {
            throw new AccessDeniedException("Anda tidak memiliki akses ke deposit ini.");
        }
        return DepositResponse.fromEntity(deposit);
    }

    @Transactional
    public DepositResponse verifyDeposit(String id, EnglishDepositVerificationRequest request) {
        EnglishDeposit deposit = depositRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit tidak ditemukan"));

        if (deposit.getStatus() == EnglishDepositStatus.VERIFIED) {
            throw new BadRequestException("Deposit VERIFIED tidak dapat diverifikasi ulang.");
        }
        
        if (deposit.getStatus() != EnglishDepositStatus.SUBMITTED) {
            throw new BadRequestException("Hanya dapat memverifikasi deposit berstatus SUBMITTED.");
        }

        if ("REJECTED".equals(request.decision())) {
            if (request.verificationNote() == null || request.verificationNote().trim().isEmpty()) {
                throw new BadRequestException("REJECTED wajib memiliki verificationNote.");
            }
            deposit.setStatus(EnglishDepositStatus.REJECTED);
            deposit.setScore(null);
        } else if ("VERIFIED".equals(request.decision())) {
            if (request.score() != null) {
                if (request.score().compareTo(BigDecimal.ZERO) < 0 || request.score().compareTo(new BigDecimal("100")) > 0) {
                    throw new BadRequestException("Score harus di antara 0 sampai 100.");
                }
            }
            deposit.setStatus(EnglishDepositStatus.VERIFIED);
            deposit.setScore(request.score());
        }

        deposit.setVerificationNote(request.verificationNote());
        deposit.setVerifiedByEmail(getCurrentUserEmail());
        deposit.setVerifiedAt(LocalDateTime.now());

        return DepositResponse.fromEntity(depositRepository.save(deposit));
    }

    // ==========================================
    // REPORTS
    // ==========================================

    @Transactional(readOnly = true)
    public ReportSummaryResponse getReportSummary() {
        long totalActivities = activityRepository.countByActiveTrue();
        long publishedActivities = activityRepository.countByActiveTrueAndStatus(EnglishActivityStatus.PUBLISHED);
        long completedActivities = activityRepository.countByActiveTrueAndStatus(EnglishActivityStatus.COMPLETED);
        
        long totalDeposits = depositRepository.countByActiveTrue();
        long submittedDeposits = depositRepository.countByActiveTrueAndStatus(EnglishDepositStatus.SUBMITTED);
        long verifiedDeposits = depositRepository.countByActiveTrueAndStatus(EnglishDepositStatus.VERIFIED);
        long rejectedDeposits = depositRepository.countByActiveTrueAndStatus(EnglishDepositStatus.REJECTED);
        long missedDeposits = depositRepository.countByActiveTrueAndStatus(EnglishDepositStatus.MISSED);

        List<EnglishDeposit> verifiedList = depositRepository.findAll().stream()
                .filter(d -> d.isActive() && d.getStatus() == EnglishDepositStatus.VERIFIED && d.getScore() != null)
                .toList();

        BigDecimal averageScore = BigDecimal.ZERO;
        if (!verifiedList.isEmpty()) {
            BigDecimal sum = verifiedList.stream().map(EnglishDeposit::getScore).reduce(BigDecimal.ZERO, BigDecimal::add);
            averageScore = sum.divide(new BigDecimal(verifiedList.size()), 2, RoundingMode.HALF_UP);
        }

        Map<Long, List<EnglishDeposit>> grouped = depositRepository.findAll().stream()
                .filter(EnglishDeposit::isActive)
                .collect(Collectors.groupingBy(EnglishDeposit::getMemberId));

        List<MemberSummary> memberSummaries = grouped.entrySet().stream()
                .map(e -> {
                    Long memId = e.getKey();
                    List<EnglishDeposit> deps = e.getValue();
                    String memName = deps.get(0).getMemberName();
                    long sCount = deps.stream().filter(d -> d.getStatus() == EnglishDepositStatus.SUBMITTED).count();
                    long vCount = deps.stream().filter(d -> d.getStatus() == EnglishDepositStatus.VERIFIED).count();
                    long rCount = deps.stream().filter(d -> d.getStatus() == EnglishDepositStatus.REJECTED).count();
                    
                    List<EnglishDeposit> verDeps = deps.stream().filter(d -> d.getStatus() == EnglishDepositStatus.VERIFIED && d.getScore() != null).toList();
                    BigDecimal memAvg = BigDecimal.ZERO;
                    if (!verDeps.isEmpty()) {
                        BigDecimal s = verDeps.stream().map(EnglishDeposit::getScore).reduce(BigDecimal.ZERO, BigDecimal::add);
                        memAvg = s.divide(new BigDecimal(verDeps.size()), 2, RoundingMode.HALF_UP);
                    }
                    
                    return MemberSummary.builder()
                            .memberId(memId)
                            .memberName(memName)
                            .submittedCount(sCount)
                            .verifiedCount(vCount)
                            .rejectedCount(rCount)
                            .averageScore(memAvg)
                            .build();
                }).toList();

        return ReportSummaryResponse.builder()
                .totalActivities(totalActivities)
                .publishedActivities(publishedActivities)
                .completedActivities(completedActivities)
                .totalDeposits(totalDeposits)
                .submittedDeposits(submittedDeposits)
                .verifiedDeposits(verifiedDeposits)
                .rejectedDeposits(rejectedDeposits)
                .missedDeposits(missedDeposits)
                .averageScore(averageScore)
                .memberSummary(memberSummaries)
                .build();
    }
}
