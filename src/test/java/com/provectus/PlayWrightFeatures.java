package com.provectus;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.RecordVideoSize;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static com.codeborne.selenide.Selenide.*;
import static java.lang.Thread.sleep;

public class PlayWrightFeatures {

    @SneakyThrows
    @Test
    public void makeFullScreenScreenShot() {
        Playwright playwright = Playwright.create();
        Browser chrome = playwright.chromium().launch(new BrowserType.LaunchOptions().setChannel("chrome").setHeadless(true));
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
        if (new File("video").exists())
            FileUtils.cleanDirectory(Paths.get("video").toFile());
        Playwright playwright = Playwright.create();
        Browser chrome = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true).setChannel("chrome"));
        BrowserContext video = chrome.newContext(new Browser.NewContextOptions()
                .setRecordVideoDir(Paths.get("video"))
                .setRecordHarPath(Paths.get("har/har.har")));
        video.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
        Page page = video.newPage();
        page.navigate("https://provectus.com");
        page.click("a[title=Insights]");
        page.locator("#breadcrumbs a", new Page.LocatorOptions().setHasText("Home")).click();
        page.click("a[title='AI  Solutions']");
        page.locator("#breadcrumbs a", new Page.LocatorOptions().setHasText("Home")).click();

        video.tracing().stop(new Tracing.StopOptions().setPath(Paths.get("trace/trace.zip")));

        video.close();
        playwright.close();
    }

    @Test
    public void recordVideoWithPWConnectedSelenide() throws IOException {
        FileUtils.cleanDirectory(Paths.get("video").toFile());
        Playwright playwright = Playwright.create();
        Browser chrome = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setChannel("chrome")
                .setArgs(List.of("--remote-debugging-port=9222")));
        BrowserContext video = chrome.newContext(new Browser.NewContextOptions()
                .setRecordVideoDir(Paths.get("video")));

        Page page = video.newPage();
        // page.navigate("https://provectus.com");
        Configuration.browser = MyWDProvider.class.getName();
        open("https://provectus.com");

        $("a[title=Insights]").click();
        $$("#breadcrumbs a").find(Condition.text("Home")).click();
        $("a[title='AI  Solutions']").click();
        $$("#breadcrumbs a").find(Condition.text("Home")).click();

        video.close();
        playwright.close();

    }


}
