package com.checkpoint;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BacklogE2ETest {

    private static final long SEEDED_GAME_ID = 1L;
    private static final long SEEDED_PLATFORM_ID = 1L;

    @LocalServerPort
    private int port;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    // Inject credentials from test/application.yml
    @Value("${app.test-seed.admin.username:test_admin}")
    private String adminUsername;

    @Value("${app.test-seed.admin.password:test_admin_password}")
    private String adminPassword;

    @Value("${app.test-seed.user.username:test_user}")
    private String userUsername;

    @Value("${app.test-seed.user.password:test_user_password}")
    private String userPassword;


    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        assertTrue(datasourceUrl != null && datasourceUrl.startsWith("jdbc:h2:"));

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--headless=new",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--window-size=1400,900"
        );

        String osName = System.getProperty("os.name", "").toLowerCase();
        if (osName.contains("linux")) {
            String chromeBinary = System.getenv("CHROME_BIN");
            if (chromeBinary == null || chromeBinary.isBlank()) {
                chromeBinary = System.getenv("GOOGLE_CHROME_BIN");
            }
            if (chromeBinary == null || chromeBinary.isBlank()) {
                chromeBinary = "/usr/bin/google-chrome-beta";
            }
            if (!chromeBinary.isBlank() && Files.exists(Path.of(chromeBinary))) {
                options.setBinary(chromeBinary);
            }
        }

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Sql({"/testdata.sql"})
    @Sql(statements = "DELETE FROM user_games", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testAdminAddToBacklogSuccess() {
        adminLogin();

        wait.until(ExpectedConditions.elementToBeClickable(By.id("navLibraryBtn"))).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("libraryLoading")));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("library-dropdown-" + SEEDED_GAME_ID))).click();
        String dropdownMenuSelector = ".dropdown-menu[aria-labelledby='library-dropdown-" + SEEDED_GAME_ID + "']";
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(dropdownMenuSelector)));
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
                ".backlog-add[data-id='" + SEEDED_GAME_ID + "'][data-platform-id='" + SEEDED_PLATFORM_ID + "']"
        ))).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("libraryAlert"), "Added to your backlog!"));

        wait.until(ExpectedConditions.elementToBeClickable(By.id("navBacklogBtn"))).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("backlogLoading")));
        WebElement statusSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#backlogTableBody .backlog-status[data-id]")));
        assertEquals("WANT_TO_PLAY", new Select(statusSelect).getFirstSelectedOption().getAttribute("value"));
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("backlogCount")))
                .getText().contains("1 item"));
    }

    @Test
    @Sql({"/testdata.sql"})
    void testUserUpdateBacklogStatus() {
        userLogin();

        wait.until(ExpectedConditions.elementToBeClickable(By.id("navBacklogBtn"))).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("backlogLoading")));
        WebElement statusSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#backlogTableBody .backlog-status[data-id]")));
        new Select(statusSelect).selectByValue("COMPLETED");

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("backlogAlert"), "Status updated."));
        WebElement updatedStatusSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#backlogTableBody .backlog-status[data-id]")));
        assertEquals("COMPLETED", new Select(updatedStatusSelect).getFirstSelectedOption().getAttribute("value"));
    }

    private void userLogin() {
        driver.get("http://localhost:" + port);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginForm")));
        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginUsername")));
        WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginPassword")));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#loginForm button[type='submit']")));

        usernameInput.clear();
        usernameInput.sendKeys(userUsername);
        passwordInput.clear();
        passwordInput.sendKeys(userPassword);
        loginButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("view-app")));
    }

    private void adminLogin() {
        driver.get("http://localhost:" + port);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginForm")));
        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginUsername")));
        WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginPassword")));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#loginForm button[type='submit']")));

        usernameInput.clear();
        usernameInput.sendKeys(adminUsername);
        passwordInput.clear();
        passwordInput.sendKeys(adminPassword);
        loginButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("view-app")));
    }

}
