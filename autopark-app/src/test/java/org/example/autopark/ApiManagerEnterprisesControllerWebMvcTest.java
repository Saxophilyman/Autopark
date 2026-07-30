package org.example.autopark;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.autopark.controllers.managers.APIControllers.ApiManagerEnterprisesController;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.example.autopark.dto.EnterpriseDTO;
import org.example.autopark.dto.mapper.EnterpriseMapper;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.monitoring.DbExceptionCountingAdvice;
import org.example.autopark.securityConfig.jwt.JwtAuthFilter;
import org.example.autopark.service.EnterpriseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ApiManagerEnterprisesController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        JwtAuthFilter.class,
                        DbExceptionCountingAdvice.class
                }
        )
)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiManagerEnterprisesControllerWebMvcTest.CurrentManagerIdResolverConfig.class)
class ApiManagerEnterprisesControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EnterpriseService enterprisesService;

    @MockBean
    private EnterpriseMapper enterpriseMapper;

    @TestConfiguration
    static class CurrentManagerIdResolverConfig implements WebMvcConfigurer {

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new HandlerMethodArgumentResolver() {

                @Override
                public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                    return parameter.hasParameterAnnotation(CurrentManagerId.class);
                }

                @Override
                public Object resolveArgument(
                        org.springframework.core.MethodParameter parameter,
                        org.springframework.web.method.support.ModelAndViewContainer mavContainer,
                        org.springframework.web.context.request.NativeWebRequest webRequest,
                        org.springframework.web.bind.support.WebDataBinderFactory binderFactory
                ) {
                    return 1L;
                }
            });
        }
    }

    @Test
    void getEnterprises_returnsListForManager() throws Exception {
        Enterprise enterprise = new Enterprise();
        enterprise.setEnterpriseId(10L);
        enterprise.setName("Test Enterprise");

        EnterpriseDTO dto = new EnterpriseDTO();
        dto.setEnterpriseId(10L);
        dto.setName("Test Enterprise");

        when(enterprisesService.findEnterprisesForManager(anyLong()))
                .thenReturn(List.of(enterprise));

        when(enterpriseMapper.convertToDTO(enterprise))
                .thenReturn(dto);

        mockMvc.perform(get("/api/managers/enterprises"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].enterpriseId").value(10L))
                .andExpect(jsonPath("$[0].name").value("Test Enterprise"));

        verify(enterprisesService).findEnterprisesForManager(1L);
        verify(enterpriseMapper).convertToDTO(enterprise);
    }

    @Test
    void createEnterprise_validRequest_returns201() throws Exception {
        EnterpriseDTO requestDto = new EnterpriseDTO();
        requestDto.setName("New Enterprise");
        requestDto.setCityOfEnterprise("Moscow");
        requestDto.setTimeZone("Europe/Moscow");

        Enterprise entity = new Enterprise();
        entity.setName("New Enterprise");

        when(enterpriseMapper.convertToEntity(any(EnterpriseDTO.class)))
                .thenReturn(entity);

        mockMvc.perform(post("/api/managers/enterprises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());

        verify(enterpriseMapper).convertToEntity(any(EnterpriseDTO.class));
    }

    @Test
    void deleteEnterprise_returns204() throws Exception {
        long enterpriseId = 42L;

        mockMvc.perform(delete(
                        "/api/managers/enterprises/{enterpriseId}",
                        enterpriseId
                ))
                .andExpect(status().isNoContent());

        verify(enterprisesService).delete(1L, enterpriseId);
    }
}