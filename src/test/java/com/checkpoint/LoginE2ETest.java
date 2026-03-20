package com.checkpoint;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import io.github.bonigarcia.wdm.WebDriverManager;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LoginE2ETest {

    @LocalServerPort
    private int port;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        // Explicit safety check: this E2E suite must never use the real database.
        assertTrue(datasourceUrl != null && datasourceUrl.startsWith("jdbc:h2:"),
                "E2E tests must run against H2. Current datasource: " + datasourceUrl);

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--headless=new",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--window-size=1400,900" // Set a consistent window size
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
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void registerLoginAndLogout_flowWorks() {
        String username = "u" + String.format("%09d", (System.currentTimeMillis() % 1000000000L));
        String password = "Secret1";

        openAuthPage();
        waitAndClick(By.id("registerTabBtn"));

        type(By.id("regUsername"), username);
        type(By.id("regPassword"), password);
        type(By.id("regConfirmPassword"), password);
        waitAndClick(By.cssSelector("#registerForm button[type='submit']"));

        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginForm"))).isDisplayed());
        assertEquals(username, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginUsername"))).getAttribute("value"));

        type(By.id("loginPassword"), password);
        waitAndClick(By.cssSelector("#loginForm button[type='submit']"));

        WebElement appView = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("view-app")));
        assertTrue(appView.isDisplayed());
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("backlogSection"))).isDisplayed());
        assertEquals(username, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("navUsername"))).getText());

        waitAndClick(By.xpath("//button[normalize-space()='Logout']"));
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginForm"))).isDisplayed());
    }

    @Test
    void loginWithInvalidCredentials_showsError() {
        openAuthPage();
        type(By.id("loginUsername"), "missing" + System.currentTimeMillis());
        type(By.id("loginPassword"), "wrong-password");
        waitAndClick(By.cssSelector("#loginForm button[type='submit']"));

        String errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("authAlert"))).getText();
        assertTrue(errorMsg.contains("Invalid username or password."));
    }

    @Test
    void registerWithMismatchingConfirmPassword_showsError() {
        String username = "u" + System.currentTimeMillis();
        openAuthPage();
        waitAndClick(By.id("registerTabBtn"));

        type(By.id("regUsername"), username);
        type(By.id("regPassword"), "Secret1");
        type(By.id("regConfirmPassword"), "Secret2");
        waitAndClick(By.cssSelector("#registerForm button[type='submit']"));

        String errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("authAlert"))).getText();
        assertTrue(errorMsg.contains("Passwords do not match"));
    }

    @Test
    void registerWithDuplicateUsername_showsError() {
        String username = "dup" + System.currentTimeMillis();
        String password = "Secret1";

        openAuthPage();
        waitAndClick(By.id("registerTabBtn"));

        type(By.id("regUsername"), username);
        type(By.id("regPassword"), password);
        type(By.id("regConfirmPassword"), password);
        waitAndClick(By.cssSelector("#registerForm button[type='submit']"));

        // Wait for login form to appear indicating success
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginForm")));

        // Try to register again with same username
        waitAndClick(By.id("registerTabBtn"));

        type(By.id("regUsername"), username);
        type(By.id("regPassword"), password);
        type(By.id("regConfirmPassword"), password);
        waitAndClick(By.cssSelector("#registerForm button[type='submit']"));

        String errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("authAlert"))).getText();
        assertTrue(errorMsg.contains("Username already taken"));
    }

    private void openAuthPage() {
        driver.get("http://localhost:" + port);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginForm")));
    }

    private void waitAndClick(By by) {
        wait.until(ExpectedConditions.elementToBeClickable(by)).click();
    }

    private void type(By by, String value) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        element.clear();
        element.sendKeys(value);
    }

}