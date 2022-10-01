package com.provectus;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.microsoft.playwright.*;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import static com.codeborne.selenide.Selectors.byLinkText;
import static com.codeborne.selenide.Selenide.*;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class PlayWrightFeatures {
    // Change this to true when you want to see actions in browser
    private boolean headless = true;
    @SneakyThrows
    @Test
    public void makeFullScreenScreenShot() {
        Playwright playwright = Playwright.create();
        Browser chrome = playwright.chromium()
                .launch(new BrowserType.LaunchOptions().setHeadless(headless));
        Page page = chrome.newPage();
        page.navigate("https://google.com");
        page.type("[name=q]", "Provectus");
        page.press("[name=q]", "Enter");

        sleep(5000);

        byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
        File file = new File("screenshots", "screen.png");

        FileUtils.writeByteArrayToFile(file, screenshot);

        playwright.close();

    }

    @SneakyThrows
    @Test
    public void recordVideoWithPW() {
        Playwright playwright = Playwright.create();
        Browser chrome = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
        BrowserContext video = chrome.newContext(new Browser.NewContextOptions()
                .setRecordVideoDir(Paths.get("video"))
                .setRecordHarPath(Paths.get("har/har.har")));
        video.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
        Page page = video.newPage();
        page.navigate("http://the-internet.herokuapp.com/");
        page.locator("xpath=//a[text()='Form Authentication']").click();
        page.type("#username", "tomsmith");
        page.type("#password", "SuperSecretPassword!");
        page.click(".radius");
        assertThat(page.locator(".flash.success")).isVisible();

        video.tracing().stop(new Tracing.StopOptions().setPath(Paths.get("trace/trace.zip")));
        sleep(5000);
        video.close();
        playwright.close();
    }

    @Test
    public void recordVideoWithPWConnectedSelenide() {
        Playwright playwright = Playwright.create();
        Browser chrome = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setChromiumSandbox(false)
                .setChannel("chrome")
                .setArgs(List.of("--remote-debugging-port=9222")));
        BrowserContext video = chrome.newContext(new Browser.NewContextOptions()
                .setRecordVideoDir(Paths.get("video1")));

        Page page = video.newPage();
       // page.navigate("https://provectus.com");
        Configuration.browser = MyWDProvider.class.getName();
        open("http://the-internet.herokuapp.com/");

        $(byLinkText("Form Authentication")).click();
        $("#username").setValue("tomsmith");
        $("#password").setValue("SuperSecretPassword!");
        $(".radius").click();
        $(".flash.success").shouldBe(Condition.visible);
        sleep(5000);
        video.close();
        playwright.close();

    }


}
