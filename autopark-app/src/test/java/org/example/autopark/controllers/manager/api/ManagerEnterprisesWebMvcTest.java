package org.example.autopark.controllers.manager.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.autopark.controllers.managers.APIControllers.ApiManagerEnterprisesController;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.example.autopark.dto.EnterpriseDTO;
import org.example.autopark.dto.mapper.EnterpriseMapper;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.service.EnterpriseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiManagerEnterprisesControllerWebMvcTest.CurrentManagerIdResolverConfig.class)
class ApiManagerEnterprisesControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    EnterpriseService enterprisesService;

    @MockBean
    EnterpriseMapper enterpriseMapper;

    /**
     * Конфиг, который говорит Spring MVC:
     *  "если видишь параметр с @CurrentManagerId, подставь 1L".
     *  Подключаем его через @Import в @SpringBootTest.
     */
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
                public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                                              org.springframework.web.method.support.ModelAndViewContainer mavContainer,
                                              org.springframework.web.context.request.NativeWebRequest webRequest,
                                              org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
                    return 1L; // фиктивный managerId для всех тестов
                }
            });
        }
    }

    @Test
    void getEnterprises_returnsListForManager() throws Exception {
        // given
        Enterprise enterprise = new Enterprise();
        enterprise.setEnterpriseId(10L);
        enterprise.setName("Test Enterprise");

        EnterpriseDTO dto = new EnterpriseDTO();
        dto.setEnterpriseId(10L);
        dto.setName("Test Enterprise");

        when(enterprisesService.findEnterprisesForManager(anyLong()))
                .thenReturn(List.of(enterprise));
        when(enterpriseMapper.convertToDTO(enterprise)).thenReturn(dto);

        // when + then
        mockMvc.perform(get("/api/managers/enterprises"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // проверяем, что это массив и в нём один объект
                .andExpect(jsonPath("$.length()").value(1))
                // ВАЖНО: если в DTO поле enterpriseId, а не id
                .andExpect(jsonPath("$[0].enterpriseId").value(10L))
                .andExpect(jsonPath("$[0].name").value("Test Enterprise"));

        // проверяем, что в сервис ушёл managerId = 1L (из резолвера)
        verify(enterprisesService).findEnterprisesForManager(1L);
    }

    @Test
    void createEnterprise_validRequest_returns201() throws Exception {
        // given
        EnterpriseDTO dto = new EnterpriseDTO();
        dto.setName("New Enterprise");

        Enterprise entity = new Enterprise();
        entity.setName("New Enterprise");

        when(enterpriseMapper.convertToEntity(dto)).thenReturn(entity);

        // when + then
        mockMvc.perform(post("/api/managers/enterprises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // можно дополнительно проверить вызов save с managerId = 1L
        verify(enterprisesService).save(entity, 1L);
    }

    @Test
    void deleteEnterprise_returns204() throws Exception {
        // given
        long enterpriseId = 42L;

        // when + then
        mockMvc.perform(delete("/api/managers/enterprises/{enterpriseId}", enterpriseId))
                .andExpect(status().isNoContent());

        verify(enterprisesService).delete(1L, enterpriseId);
    }
}
