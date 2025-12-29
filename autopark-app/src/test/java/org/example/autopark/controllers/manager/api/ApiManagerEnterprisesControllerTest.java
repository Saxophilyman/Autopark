//package org.example.autopark.controllers.manager.api;
//
//import org.example.autopark.controllers.managers.APIControllers.ApiManagerEnterprisesController;
//import org.example.autopark.dto.EnterpriseDTO;
//import org.example.autopark.dto.mapper.EnterpriseMapper;
//import org.example.autopark.entity.Enterprise;
//import org.example.autopark.service.EnterpriseService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.BeanPropertyBindingResult;
//import org.springframework.validation.BindingResult;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
///**
// * Юнит-тесты для ApiManagerEnterprisesController.
// * Проверяем:
// *  - вызовы сервисов,
// *  - корректный маппинг,
// *  - HTTP-статусы.
// */
//class ApiManagerEnterprisesControllerTest {
//
//    private EnterpriseService enterprisesService;
//    private EnterpriseMapper enterpriseMapper;
//    private ApiManagerEnterprisesController controller;
//
//    @BeforeEach
//    void setUp() {
//        enterprisesService = mock(EnterpriseService.class);
//        enterpriseMapper = mock(EnterpriseMapper.class);
//        controller = new ApiManagerEnterprisesController(enterprisesService, enterpriseMapper);
//    }
//
//    @Test
//    void indexEnterprises_returnsDtoListForManager() {
//        // given
//        Long managerId = 1L;
//
//        Enterprise enterprise1 = new Enterprise();
//        enterprise1.setEnterpriseId(10L);
//        enterprise1.setName("Enterprise A");
//
//        Enterprise enterprise2 = new Enterprise();
//        enterprise2.setEnterpriseId(20L);
//        enterprise2.setName("Enterprise B");
//
//        EnterpriseDTO dto1 = new EnterpriseDTO();
//        dto1.setEnterpriseId(10L);
//        dto1.setName("Enterprise A");
//
//        EnterpriseDTO dto2 = new EnterpriseDTO();
//        dto2.setEnterpriseId(20L);
//        dto2.setName("Enterprise B");
//
//        when(enterprisesService.findEnterprisesForManager(managerId))
//                .thenReturn(List.of(enterprise1, enterprise2));
//        when(enterpriseMapper.convertToDTO(enterprise1)).thenReturn(dto1);
//        when(enterpriseMapper.convertToDTO(enterprise2)).thenReturn(dto2);
//
//        // when
//        List<EnterpriseDTO> result = controller.indexEnterprises(managerId);
//
//        // then
//        assertThat(result)
//                .hasSize(2)
//                .containsExactly(dto1, dto2);
//
//        verify(enterprisesService).findEnterprisesForManager(managerId);
//        verify(enterpriseMapper).convertToDTO(enterprise1);
//        verify(enterpriseMapper).convertToDTO(enterprise2);
//        verifyNoMoreInteractions(enterprisesService, enterpriseMapper);
//    }
//
//    @Test
//    void create_validDto_callsServiceAndReturns201() {
//        // given
//        Long managerId = 1L;
//
//        EnterpriseDTO dto = new EnterpriseDTO();
//        dto.setName("New Enterprise");
//
//        // BindingResult — как если бы его создал Spring (ошибок нет)
//        BindingResult bindingResult =
//                new BeanPropertyBindingResult(dto, "enterpriseDTO");
//
//        Enterprise entity = new Enterprise();
//        entity.setName("New Enterprise");
//
//        when(enterpriseMapper.convertToEntity(dto)).thenReturn(entity);
//
//        // when
//        ResponseEntity<Void> response = controller.create(managerId, dto, bindingResult);
//
//        // then
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//        verify(enterpriseMapper).convertToEntity(dto);
//        verify(enterprisesService).save(entity, managerId);
//        verifyNoMoreInteractions(enterprisesService, enterpriseMapper);
//    }
//
//    @Test
//    void update_validDto_callsServiceAndReturns200() {
//        // given
//        Long managerId = 1L;
//        Long enterpriseId = 42L;
//
//        EnterpriseDTO dto = new EnterpriseDTO();
//        dto.setName("Updated Enterprise");
//
//        BindingResult bindingResult =
//                new BeanPropertyBindingResult(dto, "enterpriseDTO");
//
//        Enterprise mappedEntity = new Enterprise();
//        mappedEntity.setName("Updated Enterprise");
//
//        when(enterpriseMapper.convertToEntity(dto)).thenReturn(mappedEntity);
//
//        // when
//        ResponseEntity<HttpStatus> response =
//                controller.update(managerId, enterpriseId, dto, bindingResult);
//
//        // then
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//
//        verify(enterpriseMapper).convertToEntity(dto);
//        verify(enterprisesService).update(managerId, enterpriseId, mappedEntity);
//        verifyNoMoreInteractions(enterprisesService, enterpriseMapper);
//    }
//
//    @Test
//    void delete_callsServiceAndReturns204() {
//        // given
//        Long managerId = 1L;
//        Long enterpriseId = 99L;
//
//        // when
//        ResponseEntity<Void> response = controller.delete(managerId, enterpriseId);
//
//        // then
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
//        verify(enterprisesService).delete(managerId, enterpriseId);
//        verifyNoMoreInteractions(enterprisesService, enterpriseMapper);
//    }
//
//    @Test
//    void update_passesCorrectManagerIdAndEnterpriseIdToService() {
//        // Дополнительная проверка, что в update передаются ровно те id,
//        // которые приходят в контроллер.
//
//        Long managerId = 5L;
//        Long enterpriseId = 123L;
//
//        EnterpriseDTO dto = new EnterpriseDTO();
//        dto.setName("Some Name");
//
//        BindingResult bindingResult =
//                new BeanPropertyBindingResult(dto, "enterpriseDTO");
//
//        Enterprise mappedEntity = new Enterprise();
//        mappedEntity.setName("Some Name");
//
//        when(enterpriseMapper.convertToEntity(dto)).thenReturn(mappedEntity);
//
//        controller.update(managerId, enterpriseId, dto, bindingResult);
//
//        // захватываем аргументы
//        ArgumentCaptor<Long> managerCaptor = ArgumentCaptor.forClass(Long.class);
//        ArgumentCaptor<Long> enterpriseIdCaptor = ArgumentCaptor.forClass(Long.class);
//        ArgumentCaptor<Enterprise> enterpriseCaptor = ArgumentCaptor.forClass(Enterprise.class);
//
//        verify(enterprisesService).update(
//                managerCaptor.capture(),
//                enterpriseIdCaptor.capture(),
//                enterpriseCaptor.capture()
//        );
//
//        assertThat(managerCaptor.getValue()).isEqualTo(managerId);
//        assertThat(enterpriseIdCaptor.getValue()).isEqualTo(enterpriseId);
//        assertThat(enterpriseCaptor.getValue().getName()).isEqualTo("Some Name");
//    }
//}
