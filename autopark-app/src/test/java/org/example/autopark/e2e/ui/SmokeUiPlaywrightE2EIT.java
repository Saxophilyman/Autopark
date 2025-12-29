package org.example.autopark.e2e.ui;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.example.autopark.e2e.E2eTestBase;
import org.junit.jupiter.api.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Простейший smoke-тест UI через Playwright:
 *  - поднимает Spring Boot на случайном порту (через E2eTestBase)
 *  - открывает главную страницу в браузере
 *  - проверяет, что у страницы есть title.
 */
class SmokeUiPlaywrightE2EIT extends E2eTestBase {

    // Порт, на котором поднялся встроенный Tomcat (RANDOM_PORT)
    @LocalServerPort
    int port;

    // Если у тебя есть context-path в application.yaml — можно подхватить:
    @Value("${server.servlet.context-path:}")
    String contextPath;

    private static Playwright playwright;

    private Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void initPlaywright() {
        // Единственный Playwright на весь класс тестов
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
        // Запускаем Chromium в headless-режиме (без окна)
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(true);

        browser = playwright.chromium().launch(options);
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void tearDownBrowser() {
        if (context != null) {
            context.close();
        }
        if (browser != null) {
            browser.close();
        }
    }

    @Test
    void mainPageShouldOpenAndHaveTitle() {
        String baseUrl = "http://localhost:" + port + contextPath;
        // Например, на корень или страницу логина — подставь, как у тебя реально
        String url = baseUrl + "/";

        page.navigate(url);

        // Дождёмся загрузки
        page.waitForLoadState(LoadState.NETWORKIDLE);

        String title = page.title();

        Assertions.assertNotNull(title);
        Assertions.assertFalse(title.isBlank(), "Title страницы не должен быть пустым");
    }
}
