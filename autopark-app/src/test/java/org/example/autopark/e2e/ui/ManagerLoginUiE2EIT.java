package org.example.autopark.e2e.ui;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.SelectOption;
import org.example.autopark.e2e.E2eTestBase;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

@Sql(
        scripts = {
                "/sql/e2e/clean.sql",
                "/sql/e2e/insert_manager_user.sql",
                "/sql/e2e/insert_vehicles.sql"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class ManagerLoginUiE2EIT extends E2eTestBase {

    @LocalServerPort
    int port;

    @Value("${server.servlet.context-path:}")
    String contextPath;

    private static Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    // ─────────────────────────────────────────────────────────────
    // Общие хелперы
    // ─────────────────────────────────────────────────────────────

    private String baseUrl() {
        return "http://localhost:" + port + contextPath;
    }

    /**
     * Базовый логин без ожидания конкретного URL.
     * Используем и для позитивных, и для негативных сценариев.
     */
    private void performLogin(String username, String password) {
        String baseUrl = baseUrl();
        page.navigate(baseUrl + "/auth/login");
        page.waitForLoadState(LoadState.NETWORKIDLE);

        page.fill("input[name='username']", username);
        page.fill("input[name='password']", password);
        page.click("button[type='submit']");
    }

    /**
     * Стандартный логин менеджером m1/password.
     * Дополнительно ждём переход на страницу предприятий.
     */
    private void loginAsDefaultManager() {
        performLogin("m1", "password");
        page.waitForURL("**/managers/enterprises");
    }

    /**
     * Переход на список ТС предприятия (enterpriseId)
     * и ожидание таблицы с id="vehicles-table".
     */
    private void openEnterpriseVehiclesPage(long enterpriseId) {
        page.click("a[href$='/managers/enterprises/" + enterpriseId + "/vehicles']");
        page.waitForURL("**/managers/enterprises/" + enterpriseId + "/vehicles");
        page.waitForSelector("table#vehicles-table");
    }

    // ─────────────────────────────────────────────────────────────
    // Инициализация Playwright
    // ─────────────────────────────────────────────────────────────

    @BeforeAll
    static void initPlaywright() {
        playwright = Playwright.create();
    }

    @AfterAll
    static void closePlaywright() {
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void setUpBrowser() {
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
        );
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void tearDownBrowser() {
        context.close();
        browser.close();
    }

    // ─────────────────────────────────────────────────────────────
    // Позитивные сценарии
    // ─────────────────────────────────────────────────────────────

    @Test
    void managerCanLoginAndSeeOwnEnterpriseVehicles() {
        loginAsDefaultManager();
        openEnterpriseVehiclesPage(1L);

        int rows = page.locator("table#vehicles-table tbody tr").count();
        Assertions.assertTrue(rows > 0, "Ожидается хотя бы одна машина в списке");
    }

    @Test
    void managerCanOpenVehicleDetailsFromEnterpriseVehiclesList() {
        loginAsDefaultManager();
        openEnterpriseVehiclesPage(1L);

        // Нажимаем первую кнопку "Просмотр"
        Locator firstViewButton = page
                .locator("table#vehicles-table tbody tr td a:has-text('Просмотр')")
                .first();

        firstViewButton.click();

        // Ждём страницу просмотра ТС
        page.waitForURL("**/managers/enterprises/1/vehicles/*");

        Assertions.assertTrue(
                page.getByText("Просмотр транспортного средства").isVisible(),
                "Ожидаем увидеть страницу просмотра транспортного средства"
        );
    }

    @Test
    void managerCanCreateNewVehicle() {
        loginAsDefaultManager();
        openEnterpriseVehiclesPage(1L);

        // 5. Жмём "Добавить транспортное средство"
        page.click("text=Добавить транспортное средство");

        // 6. Ждём форму создания
        page.waitForURL("**/managers/enterprises/1/vehicles/new");
        Assertions.assertTrue(
                page.getByText("Новое транспортное средство").isVisible(),
                "Ожидаем, что открылась страница создания ТС"
        );

        // 7. Заполняем форму
        page.selectOption("#brand", new SelectOption().setIndex(1));
        page.fill("#vehicleName", "Playwright-Тестовый автомобиль");
        page.fill("#licensePlate", "У111НА");
        page.fill("#vehicleCost", "123456");
        page.fill("#vehicleYearOfRelease", "2020");

        // 8. Отправляем форму
        page.click("button[type='submit']");

        // 9. После сохранения нас должно вернуть к списку ТС предприятия
        page.waitForURL("**/managers/enterprises/1/vehicles");
        page.waitForSelector("table#vehicles-table");

        // 10. Проверяем, что в таблице появился наш номер
        Assertions.assertTrue(
                page.getByText("У111НА").isVisible(),
                "Ожидаем увидеть в таблице ТС с номером У111НА"
        );
    }

    @Test
    void managerCanEditExistingVehicle() {
        loginAsDefaultManager();
        openEnterpriseVehiclesPage(1L);

        // 5. Открываем просмотр автомобиля с ID=1
        page.click("a[href='/managers/enterprises/1/vehicles/1']");
        page.waitForURL("**/managers/enterprises/1/vehicles/1");

        // 6. Жмём "Редактировать"
        page.click("text=Редактировать");
        page.waitForURL("**/managers/enterprises/1/vehicles/1/edit");

        // 7. Меняем данные
        page.fill("#vehicleName", "Playwright-обновлённое ТС");
        page.fill("#licensePlate", "У222НА");

        // 8. Сохраняем
        page.click("button[type='submit']");

        // 9. После обновления ожидаем возврат к списку ТС предприятия
        page.waitForURL("**/managers/enterprises/1/vehicles");
        page.waitForSelector("table#vehicles-table");

        // 10. Проверяем, что новые данные отобразились
        Assertions.assertTrue(
                page.getByText("Playwright-обновлённое ТС").isVisible(),
                "Ожидаем увидеть обновлённое название ТС на странице списка"
        );
        Assertions.assertTrue(
                page.getByText("У222НА").isVisible(),
                "Ожидаем увидеть обновлённый госномер У222НА на странице списка"
        );
    }

    @Test
    void managerCanDeleteVehicleFromEnterpriseVehiclesList() {
        loginAsDefaultManager();
        openEnterpriseVehiclesPage(1L);

        // 3. Количество строк ДО удаления
        Locator rowsLocator = page.locator("table#vehicles-table tbody tr");
        int rowsBefore = rowsLocator.count();
        Assertions.assertTrue(rowsBefore > 0, "Ожидается хотя бы одна машина перед удалением");

        // 4. Переходим на страницу просмотра первой машины (кнопка "Просмотр")
        rowsLocator.first()
                .locator("a.btn.btn-sm.btn-outline-primary")
                .click();

        // ждём кнопку "Удалить"
        page.waitForSelector("form button.btn.btn-danger");

        // 5. Подготовка к confirm()
        page.onceDialog(Dialog::accept);

        // 6. Жмём "Удалить"
        page.click("form button.btn.btn-danger");

        // ожидаем редирект обратно к списку машин предприятия
        page.waitForURL("**/managers/enterprises/1/vehicles");
        page.waitForSelector("table#vehicles-table");

        // 7. Количество строк ПОСЛЕ удаления
        int rowsAfter = page.locator("table#vehicles-table tbody tr").count();

        Assertions.assertEquals(
                rowsBefore - 1,
                rowsAfter,
                "После удаления количество машин должно уменьшиться на 1"
        );
    }

    // ─────────────────────────────────────────────────────────────
    // Негативные сценарии
    // ─────────────────────────────────────────────────────────────

    /**
     * Негативный сценарий: неправильный пароль.
     * Ожидаем, что останемся (или вернёмся) на /auth/login и увидим сообщение об ошибке.
     */
    @Test
    void managerCannotLoginWithWrongPassword() {
        performLogin("m1", "wrong-password");

        // Spring Security обычно редиректит на /auth/login?error
        page.waitForURL("**/auth/login**");

        String currentUrl = page.url();
        Assertions.assertTrue(
                currentUrl.contains("/auth/login"),
                "Ожидаем остаться на странице логина при неверном пароле"
        );

        // Если у тебя другое сообщение – поменяй текст в этом ассерте
        Assertions.assertTrue(
                page.getByText("Неверный логин или пароль").isVisible(),
                "Ожидаем увидеть сообщение об ошибке авторизации"
        );
    }

    /**
     * Негативный сценарий: попытка создать ТС с некорректным госномером.
     *
     */
    @Test
    void managerCannotCreateVehicleWithInvalidLicensePlate() {
        loginAsDefaultManager();
        openEnterpriseVehiclesPage(1L);

        // Запоминаем количество машин до попытки создания
        int rowsBefore = page.locator("table#vehicles-table tbody tr").count();

        // Переход на форму создания
        page.click("text=Добавить транспортное средство");
        page.waitForURL("**/managers/enterprises/1/vehicles/new");
        Assertions.assertTrue(
                page.getByText("Новое транспортное средство").isVisible(),
                "Ожидаем, что открыта страница создания ТС"
        );

        // Заполняем форму заведомо неправильным номером
        page.selectOption("#brand", new SelectOption().setIndex(1));
        page.fill("#vehicleName", "ТС с некорректным номером");
        page.fill("#licensePlate", "123"); // неверный формат
        page.fill("#vehicleCost", "50000");
        page.fill("#vehicleYearOfRelease", "2020");

        // Отправляем форму
        page.click("button[type='submit']");
        page.waitForLoadState(LoadState.NETWORKIDLE);

        // ВАЖНО: при ошибке валидации URL остаётся /vehicles/save,
        // но шаблон "Новое транспортное средство" рендерится снова.
        Assertions.assertTrue(
                page.getByText("Новое транспортное средство").isVisible(),
                "После ошибки валидации всё ещё должна быть форма создания ТС"
        );

        // Если у тебя выводится сообщение валидации — можно добавить:
        // Assertions.assertTrue(
        //         page.getByText("Формат номера должен быть А123БВ").isVisible(),
        //         "Ожидаем увидеть сообщение о некорректном формате номера"
        // );

        // Возвращаемся к списку ТС и убеждаемся, что количество машин не изменилось
        page.navigate(baseUrl() + "/managers/enterprises/1/vehicles");
        page.waitForSelector("table#vehicles-table");

        int rowsAfter = page.locator("table#vehicles-table tbody tr").count();
        Assertions.assertEquals(
                rowsBefore,
                rowsAfter,
                "После неуспешной валидации количество машин не должно измениться"
        );
    }

    @Test
    void anonymousUserIsRedirectedToLoginWhenOpenManagerEnterprises() {
        String baseUrl = "http://localhost:" + port + contextPath;

        // 1. БЕЗ логина сразу идём на страницу менеджера
        page.navigate(baseUrl + "/managers/enterprises");

        // 2. Ожидаем, что нас перекинет на страницу логина
        page.waitForURL("**/auth/login**");

        String currentUrl = page.url();
        Assertions.assertTrue(
                currentUrl.contains("/auth/login"),
                "Анонимного пользователя должны перекинуть на /auth/login"
        );

        // Дополнительно можно проверить, что форма логина видна
        Assertions.assertTrue(
                page.locator("input[name='username']").isVisible(),
                "Ожидаем увидеть поле ввода логина"
        );
    }

}
