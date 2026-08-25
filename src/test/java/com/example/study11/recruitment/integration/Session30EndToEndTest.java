package com.example.study11.recruitment.integration;

import com.example.study11.controller.OnboardingController;
import com.example.study11.controller.OfferNoticeController;
import com.example.study11.controller.RecruitmentInfoController;
import com.example.study11.controller.RejectionController;
import com.example.study11.controller.RetestController;
import com.example.study11.dao.CandidateResumeDao;
import com.example.study11.dao.OfferNoticeDao;
import com.example.study11.dao.OnboardingRecordDao;
import com.example.study11.dao.RecruitmentInfoDao;
import com.example.study11.dao.RecruitmentRejectionDao;
import com.example.study11.dao.RecruitmentStatusHistoryDao;
import com.example.study11.dao.RetestApplicationDao;
import com.example.study11.dao.RetestReviewDao;
import com.example.study11.entity.dto.UserSaveDTO;
import com.example.study11.entity.po.CandidateResumePo;
import com.example.study11.entity.po.OfferNoticePo;
import com.example.study11.entity.po.OnboardingRecordPo;
import com.example.study11.entity.po.RecruitmentInfoPo;
import com.example.study11.entity.po.RecruitmentRejectionPo;
import com.example.study11.entity.po.RecruitmentStatusHistoryPo;
import com.example.study11.entity.po.RetestApplicationPo;
import com.example.study11.entity.po.RetestReviewPo;
import com.example.study11.entity.po.UserPo;
import com.example.study11.exception.GlobalExceptionHandler;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.OfferNoticeService;
import com.example.study11.service.OnboardingService;
import com.example.study11.service.RecruitmentInfoService;
import com.example.study11.service.RecruitmentStatusService;
import com.example.study11.service.RejectionService;
import com.example.study11.service.RetestService;
import com.example.study11.service.UserService;
import com.example.study11.service.impl.OfferNoticeServiceImpl;
import com.example.study11.service.impl.OnboardingServiceImpl;
import com.example.study11.service.impl.RecruitmentInfoServiceImpl;
import com.example.study11.service.impl.RecruitmentStatusServiceImpl;
import com.example.study11.service.impl.RejectionServiceImpl;
import com.example.study11.service.impl.RetestServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Session 30 端到端业务流程测试。
 *
 * <p>使用真实 Controller、Service 和状态流转实现，仅将 DAO 换成有状态的测试替身，
 * 从而覆盖 HTTP 参数映射、用户身份传递、状态历史和跨模块调用。</p>
 */
class Session30EndToEndTest {

    private static final int APPLY_OPERATOR = 101;

    private static final int REVIEW_OPERATOR = 102;

    private static final int OFFER_OPERATOR = 103;

    private static final int SEND_OPERATOR = 104;

    private static final int ONBOARDING_OPERATOR = 105;

    private static final int REJECTION_OPERATOR = 201;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private FlowFixture fixture;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        fixture = new FlowFixture();
        RecruitmentStatusService statusService = new RecruitmentStatusServiceImpl(
                fixture.recruitmentInfoDao, fixture.statusHistoryDao);
        RecruitmentInfoService recruitmentInfoService = new RecruitmentInfoServiceImpl(
                fixture.recruitmentInfoDao, fixture.statusHistoryDao, fixture.candidateResumeDao);
        RetestService retestService = new RetestServiceImpl(
                fixture.recruitmentInfoDao, fixture.retestApplicationDao,
                fixture.retestReviewDao, statusService);
        OfferNoticeService offerNoticeService = new OfferNoticeServiceImpl(
                fixture.recruitmentInfoDao, fixture.offerNoticeDao, statusService);
        OnboardingService onboardingService = new OnboardingServiceImpl(
                fixture.recruitmentInfoDao, fixture.onboardingRecordDao,
                fixture.userService, statusService);
        RejectionService rejectionService = new RejectionServiceImpl(
                fixture.recruitmentInfoDao, fixture.rejectionDao,
                fixture.candidateResumeDao, statusService);

        mockMvc = MockMvcBuilders.standaloneSetup(
                        new RecruitmentInfoController(recruitmentInfoService, statusService),
                        new RetestController(retestService),
                        new OfferNoticeController(offerNoticeService),
                        new OnboardingController(onboardingService),
                        new RejectionController(rejectionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void recruitmentToOnboardingCreatesStudy11UserAndCompleteHistory() throws Exception {
        String recordUuid = createRecruitment();

        mockMvc.perform(post("/api/retest/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordUuid\":\"" + recordUuid
                                + "\",\"applicantRemark\":\"初试通过\"}")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, APPLY_OPERATOR))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RETEST_REVIEW"));

        mockMvc.perform(post("/api/retest/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordUuid\":\"" + recordUuid
                                + "\",\"retestCompany\":\"Study11\","
                                + "\"retestContactPerson\":\"李经理\","
                                + "\"retestTime\":\"2026-09-01T10:00:00\"}")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, REVIEW_OPERATOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_RETEST"));

        mockMvc.perform(post("/api/offers/draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordUuid\":\"" + recordUuid
                                + "\",\"recipientEmail\":\"candidate@example.com\","
                                + "\"noticeContent\":\"欢迎加入\"}")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, OFFER_OPERATOR))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"));

        mockMvc.perform(post("/api/offers/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordUuid\":\"" + recordUuid + "\"}")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, SEND_OPERATOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT"));

        MvcResult onboardingResult = mockMvc.perform(post("/api/onboarding/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordUuid\":\"" + recordUuid
                                + "\",\"onboardingDate\":\"2026-09-15\","
                                + "\"onboardingNote\":\"正式入职\"}")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, ONBOARDING_OPERATOR))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ONBOARDED"))
                .andExpect(jsonPath("$.userId").value(fixture.createdUserId))
                .andExpect(jsonPath("$.username").value("13800000000"))
                .andExpect(jsonPath("$.initialPassword").isString())
                .andReturn();

        String initialPassword = objectMapper.readTree(onboardingResult.getResponse().getContentAsString())
                .get("initialPassword").asText();
        assertFalse(initialPassword.isBlank());
        assertEquals(8, initialPassword.length());
        assertNotNull(fixture.userSaveRequest);
        assertEquals("13800000000", fixture.userSaveRequest.getUsername());
        assertEquals(initialPassword, fixture.userSaveRequest.getPassword());
        assertNull(fixture.onboardingRecord.getInitialPassword(),
                "初始密码不得持久化到入职记录");

        assertEquals("ONBOARDED", fixture.recruitment.getStatus());
        assertEquals(List.of("SUBMIT_RETEST_REVIEW", "CONFIRM_RETEST",
                        "COMPLETE_RETEST", "COMPLETE_ONBOARDING"),
                fixture.statusHistory.stream()
                        .map(RecruitmentStatusHistoryPo::getAction)
                        .toList());
        assertEquals(List.of(APPLY_OPERATOR, REVIEW_OPERATOR, SEND_OPERATOR, ONBOARDING_OPERATOR),
                fixture.statusHistory.stream()
                        .map(RecruitmentStatusHistoryPo::getOperatorUserId)
                        .toList());
        assertEquals(List.of("PENDING_INITIAL", "RETEST_REVIEW", "PENDING_RETEST",
                        "PENDING_ONBOARDING"),
                fixture.statusHistory.stream()
                        .map(RecruitmentStatusHistoryPo::getFromStatus)
                        .toList());
        assertEquals(List.of("RETEST_REVIEW", "PENDING_RETEST", "PENDING_ONBOARDING", "ONBOARDED"),
                fixture.statusHistory.stream()
                        .map(RecruitmentStatusHistoryPo::getToStatus)
                        .toList());

        mockMvc.perform(get("/api/recruitment-info/{recordUuid}/status-history", recordUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[3].operatorUserId").value(ONBOARDING_OPERATOR));
    }

    @Test
    void recruitmentToRejectionAppearsInTalentPoolWithStatusHistory() throws Exception {
        String recordUuid = createRecruitment();

        mockMvc.perform(post("/api/rejections/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordUuid\":\"" + recordUuid
                                + "\",\"rejectionStage\":\"INITIAL_INTERVIEW\","
                                + "\"rejectionReason\":\"SKILL_MISMATCH\","
                                + "\"talentCategory\":\"FUTURE_CONSIDER\","
                                + "\"remark\":\"技能不匹配，后续关注\"}")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, REJECTION_OPERATOR))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.action").value("REJECT"))
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.operatorUserId").value(REJECTION_OPERATOR));

        mockMvc.perform(get("/api/rejections/talent-pool")
                        .param("talentCategory", "FUTURE_CONSIDER")
                        .param("rejectionReason", "SKILL_MISMATCH")
                        .param("rejectionStage", "INITIAL_INTERVIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].recordUuid").value(recordUuid))
                .andExpect(jsonPath("$[0].talentCategory").value("FUTURE_CONSIDER"));

        assertEquals("REJECTED", fixture.recruitment.getStatus());
        assertEquals(1, fixture.statusHistory.size());
        RecruitmentStatusHistoryPo history = fixture.statusHistory.get(0);
        assertEquals("PENDING_INITIAL", history.getFromStatus());
        assertEquals("REJECTED", history.getToStatus());
        assertEquals("REJECT", history.getAction());
        assertEquals(REJECTION_OPERATOR, history.getOperatorUserId());
        assertEquals(REJECTION_OPERATOR, fixture.rejectionRecord.getOperatorUserId());
        assertEquals("FUTURE_CONSIDER", fixture.rejectionRecord.getTalentCategory());
    }

    private String createRecruitment() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/recruitment-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"applicantName\":\"张三\",\"position\":\"Java开发工程师\","
                                + "\"phone\":\"13800000000\","
                                + "\"email\":\"candidate@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recordUuid").isString())
                .andExpect(jsonPath("$.status").value("PENDING_INITIAL"))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("recordUuid").asText();
    }

    private static final class FlowFixture {

        private final RecruitmentInfoDao recruitmentInfoDao = mock(RecruitmentInfoDao.class);

        private final RecruitmentStatusHistoryDao statusHistoryDao = mock(RecruitmentStatusHistoryDao.class);

        private final CandidateResumeDao candidateResumeDao = mock(CandidateResumeDao.class);

        private final RetestApplicationDao retestApplicationDao = mock(RetestApplicationDao.class);

        private final RetestReviewDao retestReviewDao = mock(RetestReviewDao.class);

        private final OfferNoticeDao offerNoticeDao = mock(OfferNoticeDao.class);

        private final OnboardingRecordDao onboardingRecordDao = mock(OnboardingRecordDao.class);

        private final RecruitmentRejectionDao rejectionDao = mock(RecruitmentRejectionDao.class);

        private final UserService userService = mock(UserService.class);

        private final AtomicLong ids = new AtomicLong(1000L);

        private final List<RecruitmentStatusHistoryPo> statusHistory = new ArrayList<>();

        private final Map<String, RetestApplicationPo> applications = new HashMap<>();

        private final Map<String, RetestReviewPo> reviews = new HashMap<>();

        private final Map<String, OfferNoticePo> notices = new HashMap<>();

        private final Map<String, OnboardingRecordPo> onboardingRecords = new HashMap<>();

        private final Map<String, RecruitmentRejectionPo> rejections = new HashMap<>();

        private RecruitmentInfoPo recruitment;

        private UserPo user;

        private UserSaveDTO userSaveRequest;

        private OnboardingRecordPo onboardingRecord;

        private RecruitmentRejectionPo rejectionRecord;

        private Integer createdUserId;

        private FlowFixture() {
            stubRecruitmentDao();
            stubHistoryDao();
            stubRetestDaos();
            stubOfferDao();
            stubOnboardingDao();
            stubRejectionDao();
            stubUserService();
            when(candidateResumeDao.countByRecordUuid(anyString())).thenReturn(0L);
            when(candidateResumeDao.selectById(any())).thenReturn(null);
        }

        private void stubRecruitmentDao() {
            when(recruitmentInfoDao.insert(any(RecruitmentInfoPo.class))).thenAnswer(invocation -> {
                recruitment = invocation.getArgument(0);
                recruitment.setId(ids.incrementAndGet());
                if (recruitment.getCreatedAt() == null) {
                    recruitment.setCreatedAt(LocalDateTime.now());
                }
                recruitment.setUpdatedAt(recruitment.getCreatedAt());
                return 1;
            });
            when(recruitmentInfoDao.selectByRecordUuid(anyString()))
                    .thenAnswer(invocation -> matchesRecord(invocation.getArgument(0)) ? recruitment : null);
            when(recruitmentInfoDao.selectByRecordUuidForUpdate(anyString()))
                    .thenAnswer(invocation -> matchesRecord(invocation.getArgument(0)) ? recruitment : null);
            when(recruitmentInfoDao.updateStatusIfCurrent(anyString(), anyString(), anyString()))
                    .thenAnswer(invocation -> {
                        String recordUuid = invocation.getArgument(0);
                        String from = invocation.getArgument(1);
                        String to = invocation.getArgument(2);
                        if (!matchesRecord(recordUuid) || !from.equals(recruitment.getStatus())) {
                            return 0;
                        }
                        recruitment.setStatus(to);
                        recruitment.setUpdatedAt(LocalDateTime.now());
                        return 1;
                    });
        }

        private void stubHistoryDao() {
            when(statusHistoryDao.insert(any(RecruitmentStatusHistoryPo.class))).thenAnswer(invocation -> {
                RecruitmentStatusHistoryPo history = invocation.getArgument(0);
                history.setId(ids.incrementAndGet());
                statusHistory.add(history);
                return 1;
            });
            when(statusHistoryDao.countByRecordUuid(anyString())).thenAnswer(invocation ->
                    statusHistory.stream().filter(item -> item.getRecordUuid().equals(invocation.getArgument(0))).count());
            when(statusHistoryDao.selectByRecordUuid(anyString())).thenAnswer(invocation -> statusHistory.stream()
                    .filter(item -> item.getRecordUuid().equals(invocation.getArgument(0)))
                    .sorted(Comparator.comparing(RecruitmentStatusHistoryPo::getId))
                    .toList());
        }

        private void stubRetestDaos() {
            when(retestApplicationDao.insert(any(RetestApplicationPo.class))).thenAnswer(invocation -> {
                RetestApplicationPo application = invocation.getArgument(0);
                application.setId(ids.incrementAndGet());
                applications.put(application.getRecordUuid(), application);
                return 1;
            });
            when(retestApplicationDao.selectByRecordUuid(anyString()))
                    .thenAnswer(invocation -> applications.get(invocation.getArgument(0)));
            when(retestReviewDao.insert(any(RetestReviewPo.class))).thenAnswer(invocation -> {
                RetestReviewPo review = invocation.getArgument(0);
                review.setId(ids.incrementAndGet());
                reviews.put(review.getRecordUuid(), review);
                return 1;
            });
            when(retestReviewDao.selectByRecordUuid(anyString()))
                    .thenAnswer(invocation -> reviews.get(invocation.getArgument(0)));
        }

        private void stubOfferDao() {
            when(offerNoticeDao.insert(any(OfferNoticePo.class))).thenAnswer(invocation -> {
                OfferNoticePo notice = invocation.getArgument(0);
                notice.setId(ids.incrementAndGet());
                notices.put(notice.getRecordUuid(), notice);
                return 1;
            });
            when(offerNoticeDao.selectByRecordUuid(anyString()))
                    .thenAnswer(invocation -> notices.get(invocation.getArgument(0)));
            when(offerNoticeDao.selectByRecordUuidForUpdate(anyString()))
                    .thenAnswer(invocation -> notices.get(invocation.getArgument(0)));
            when(offerNoticeDao.updateToSent(anyString(), any(), any(LocalDateTime.class)))
                    .thenAnswer(invocation -> {
                        OfferNoticePo notice = notices.get(invocation.getArgument(0));
                        if (notice == null || !"DRAFT".equals(notice.getStatus())) {
                            return 0;
                        }
                        notice.setStatus("SENT");
                        notice.setSentByUserId(invocation.getArgument(1));
                        notice.setSentAt(invocation.getArgument(2));
                        notice.setUpdatedAt(notice.getSentAt());
                        return 1;
                    });
        }

        private void stubOnboardingDao() {
            when(onboardingRecordDao.insert(any(OnboardingRecordPo.class))).thenAnswer(invocation -> {
                onboardingRecord = invocation.getArgument(0);
                onboardingRecord.setId(ids.incrementAndGet());
                onboardingRecords.put(onboardingRecord.getRecordUuid(), onboardingRecord);
                return 1;
            });
            when(onboardingRecordDao.selectByRecordUuid(anyString()))
                    .thenAnswer(invocation -> onboardingRecords.get(invocation.getArgument(0)));
        }

        private void stubRejectionDao() {
            when(rejectionDao.insert(any(RecruitmentRejectionPo.class))).thenAnswer(invocation -> {
                rejectionRecord = invocation.getArgument(0);
                rejectionRecord.setId(ids.incrementAndGet());
                rejections.put(rejectionRecord.getRecordUuid(), rejectionRecord);
                return 1;
            });
            when(rejectionDao.selectByRecordUuid(anyString()))
                    .thenAnswer(invocation -> rejections.get(invocation.getArgument(0)));
            when(rejectionDao.selectTalentPool(any(), any(), any())).thenAnswer(invocation -> rejections.values()
                    .stream()
                    .filter(item -> invocation.getArgument(0) == null
                            || invocation.getArgument(0).equals(item.getTalentCategory()))
                    .filter(item -> invocation.getArgument(1) == null
                            || invocation.getArgument(1).equals(item.getRejectionReason()))
                    .filter(item -> invocation.getArgument(2) == null
                            || invocation.getArgument(2).equals(item.getRejectionStage()))
                    .toList());
        }

        private void stubUserService() {
            when(userService.getByUserName(anyString())).thenAnswer(invocation -> {
                String username = invocation.getArgument(0);
                return user != null && username.equals(user.getUsername()) ? user : null;
            });
            when(userService.dealSave(any(UserSaveDTO.class))).thenAnswer(invocation -> {
                userSaveRequest = invocation.getArgument(0);
                createdUserId = 5001;
                user = new UserPo();
                user.setId(createdUserId);
                user.setUsername(userSaveRequest.getUsername());
                user.setPassword(userSaveRequest.getPassword());
                return createdUserId;
            });
        }

        private boolean matchesRecord(String recordUuid) {
            return recruitment != null && recordUuid.equals(recruitment.getRecordUuid());
        }
    }
}
