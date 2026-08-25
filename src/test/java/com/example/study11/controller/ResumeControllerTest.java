package com.example.study11.controller;

import com.example.study11.entity.vo.CandidateResumeVO;
import com.example.study11.exception.ApiException;
import com.example.study11.exception.GlobalExceptionHandler;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.ResumeStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ResumeControllerTest {

    private static final String RECORD_UUID = "550e8400-e29b-41d4-a716-446655440000";

    @Mock
    private ResumeStorageService resumeStorageService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ResumeController(resumeStorageService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void uploadReturnsCreatedSafeMetadataAndUsesTokenUserOnly() throws Exception {
        CandidateResumeVO response = resume(42L, "resume.pdf");
        when(resumeStorageService.storeFile(eq(RECORD_UUID), any(MultipartFile.class), eq(15)))
                .thenReturn(response);

        mockMvc.perform(multipart("/api/resumes/upload")
                        .file(new MockMultipartFile("file", "resume.pdf", "application/pdf",
                                "resume".getBytes(StandardCharsets.UTF_8)))
                        .param("recordUuid", RECORD_UUID)
                        .param("uploaderUserId", "999")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 15))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.recordUuid").value(RECORD_UUID))
                .andExpect(jsonPath("$.originalFilename").value("resume.pdf"))
                .andExpect(jsonPath("$.storedFilename").doesNotExist())
                .andExpect(jsonPath("$.storagePath").doesNotExist());

        verify(resumeStorageService).storeFile(eq(RECORD_UUID), any(MultipartFile.class), eq(15));
    }

    @Test
    void uploadWithoutCurrentUserIsUnauthorized() throws Exception {
        when(resumeStorageService.storeFile(eq(RECORD_UUID), any(MultipartFile.class), isNull()))
                .thenThrow(ApiException.unauthorized("用户未登录"));

        mockMvc.perform(multipart("/api/resumes/upload")
                        .file(new MockMultipartFile("file", "resume.pdf", "application/pdf", new byte[]{1}))
                        .param("recordUuid", RECORD_UUID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void uploadRejectsInvalidExtension() throws Exception {
        when(resumeStorageService.storeFile(eq(RECORD_UUID), any(MultipartFile.class), eq(15)))
                .thenThrow(ApiException.badRequest("文件名或扩展名不合法"));

        mockMvc.perform(multipart("/api/resumes/upload")
                        .file(new MockMultipartFile("file", "resume.txt", "text/plain", new byte[]{1}))
                        .param("recordUuid", RECORD_UUID)
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 15))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadMissingRecordReturnsNotFound() throws Exception {
        when(resumeStorageService.storeFile(eq(RECORD_UUID), any(MultipartFile.class), eq(15)))
                .thenThrow(ApiException.notFound("招聘信息不存在"));

        mockMvc.perform(multipart("/api/resumes/upload")
                        .file(new MockMultipartFile("file", "resume.pdf", "application/pdf", new byte[]{1}))
                        .param("recordUuid", RECORD_UUID)
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 15))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingMultipartPartMapsToBadRequest() throws Exception {
        mockMvc.perform(multipart("/api/resumes/upload")
                        .param("recordUuid", RECORD_UUID)
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 15))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findByIdReturnsMetadataWithoutStorageFields() throws Exception {
        when(resumeStorageService.findById(42L)).thenReturn(resume(42L, "resume.pdf"));

        mockMvc.perform(get("/api/resumes/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.originalFilename").value("resume.pdf"))
                .andExpect(jsonPath("$.storedFilename").doesNotExist())
                .andExpect(jsonPath("$.storagePath").doesNotExist());
    }

    @Test
    void findByIdMissingReturnsNotFound() throws Exception {
        when(resumeStorageService.findById(404L))
                .thenThrow(ApiException.notFound("简历附件不存在"));

        mockMvc.perform(get("/api/resumes/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listReturnsMetadataForRecord() throws Exception {
        when(resumeStorageService.findByRecordUuid(RECORD_UUID))
                .thenReturn(List.of(resume(42L, "resume.pdf")));

        mockMvc.perform(get("/api/resumes/list/{recordUuid}", RECORD_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(42))
                .andExpect(jsonPath("$[0].storedFilename").doesNotExist())
                .andExpect(jsonPath("$[0].storagePath").doesNotExist());
    }

    @Test
    void listMissingRecordReturnsNotFound() throws Exception {
        when(resumeStorageService.findByRecordUuid(RECORD_UUID))
                .thenThrow(ApiException.notFound("招聘信息不存在"));

        mockMvc.perform(get("/api/resumes/list/{recordUuid}", RECORD_UUID))
                .andExpect(status().isNotFound());
    }

    @Test
    void downloadStreamsControlledFileWithCanonicalTypeAndSafeAttachmentName() throws Exception {
        CandidateResumeVO metadata = resume(42L, "résumé.pdf");
        when(resumeStorageService.findById(42L)).thenReturn(metadata);
        when(resumeStorageService.loadFile(42L))
                .thenReturn(new ByteArrayResource("resume-bytes".getBytes(StandardCharsets.UTF_8)));

        mockMvc.perform(get("/api/resumes/download/42"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes("resume-bytes".getBytes(StandardCharsets.UTF_8)))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, not(containsString("stored"))))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, not(containsString("storagePath"))));
    }

    @Test
    void downloadMissingMetadataReturnsNotFound() throws Exception {
        when(resumeStorageService.findById(404L))
                .thenThrow(ApiException.notFound("简历附件不存在"));

        mockMvc.perform(get("/api/resumes/download/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    void downloadMissingPhysicalFileReturnsNotFound() throws Exception {
        when(resumeStorageService.findById(42L)).thenReturn(resume(42L, "resume.pdf"));
        when(resumeStorageService.loadFile(42L))
                .thenThrow(ApiException.notFound("简历附件不存在"));

        mockMvc.perform(get("/api/resumes/download/42"))
                .andExpect(status().isNotFound());
    }

    @Test
    void downloadSanitizesLegacyControlCharacterAndTraversalFilename() throws Exception {
        CandidateResumeVO metadata = resume(42L, "..\\..\\evil\r\nX-Injected: yes.pdf");
        when(resumeStorageService.findById(42L)).thenReturn(metadata);
        when(resumeStorageService.loadFile(42L))
                .thenReturn(new ByteArrayResource(new byte[]{1, 2, 3}));

        mockMvc.perform(get("/api/resumes/download/42"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, not(containsString("evil"))))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, not(containsString("\r"))))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, not(containsString("\n"))));
    }

    @Test
    void downloadUsesWhitelistedTypeAndFilenameForLegacyMetadata() throws Exception {
        CandidateResumeVO metadata = resume(43L, "resume.exe");
        metadata.setContentType("text/html");
        when(resumeStorageService.findById(43L)).thenReturn(metadata);
        when(resumeStorageService.loadFile(43L))
                .thenReturn(new ByteArrayResource(new byte[]{1, 2, 3}));

        mockMvc.perform(get("/api/resumes/download/43"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("resume-43.bin")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        not(containsString("resume.exe"))));
    }

    @Test
    void downloadUsesGenericFilenameWhenUnknownMimeHasSafeBinName() throws Exception {
        CandidateResumeVO metadata = resume(44L, "legacy.bin");
        metadata.setContentType("text/html");
        when(resumeStorageService.findById(44L)).thenReturn(metadata);
        when(resumeStorageService.loadFile(44L))
                .thenReturn(new ByteArrayResource(new byte[]{1, 2, 3}));

        mockMvc.perform(get("/api/resumes/download/44"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("resume-44.bin")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        not(containsString("legacy.bin"))));
    }

    private CandidateResumeVO resume(Long id, String originalFilename) {
        CandidateResumeVO response = new CandidateResumeVO();
        response.setId(id);
        response.setRecordUuid(RECORD_UUID);
        response.setOriginalFilename(originalFilename);
        response.setFileSize(6L);
        response.setContentType("application/pdf");
        response.setUploaderUserId(15);
        response.setUploadedAt(LocalDateTime.of(2026, 8, 24, 12, 30));
        return response;
    }
}
