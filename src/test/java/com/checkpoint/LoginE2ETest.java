package com.checkpoint;

import java.io.IOException;
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
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import java.io.File;
import org.apache.commons.io.FileUtils;


import io.github.bonigarcia.wdm.WebDriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// End-to-end test for user creation through the UI, including JWT authentication and database verification
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SuppressWarnings("unused") // Prevents SonarQube false positives for "unassigned" private fields injected with @Value
class LoginE2ETest {

    @LocalServerPort
    private int port;

//    // Inject admin credentials from application.dev.properties
//    @Value("${app.admin.email}")
//    private String adminEmail;
//
//    @Value("${app.admin.password}")
//    private String adminPassword;
//
    private WebDriver driver;
    private WebDriverWait wait;
//
//    @Autowired
//    private TestRestTemplate restTemplate;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private UserService userService;

    private By inContentById(String id) {
        return By.cssSelector("#content #" + id);
    }

    @BeforeEach
    void setUp() {
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
    void testSomething() throws IOException {
        String baseUrl = "http://localhost:" + port;

        driver.get(baseUrl);
        assertTrue(driver.getTitle().contains("Checkpoint"));

        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(screenshot, new File("target/screenshots/failure.png"));

        driver.quit();
    }
}