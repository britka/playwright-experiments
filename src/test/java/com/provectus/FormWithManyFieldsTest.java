package com.provectus;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.github.javafaker.Faker;
import com.microsoft.playwright.*;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.rushuat.ocell.document.Document;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

import static com.codeborne.selenide.Selenide.*;

@Slf4j
public class FormWithManyFieldsTest {

    private TimeStat timeStat = new TimeStat();
    // Change this to true when you want to see actions in browser
    private boolean headless = true;

    private Faker faker = new Faker();
    private List<String> cities = List.of("Edinburgh", "London", "New York", "San Francisco", "Tokyo");


    @SneakyThrows
    @AfterClass
    public void afterClass() {
        Document document = new Document();
        document.addSheet(TimeStat.SHEET_NAME, List.of(timeStat));
        document.toFile(Paths.get("stats.xlsx").toFile());
    }

    @DataProvider
    public static Object[] dataProvider() {
        return new Object[]{
                true, false
        };
    }

    @Test(priority = 30)
    public void connectSelenideBrowserToPW() {
        Playwright playwright = Playwright.create();
        Browser chrome = playwright.chromium().launch(new LaunchOptions()
                .setHeadless(headless)
                .setChannel("chrome")
                .setArgs(List.of("--remote-debugging-port=9222")));

        Page page = chrome.newPage();
        page.navigate("https://datatables.net/examples/api/form.html");

        Configuration.browser = MyWDProvider.class.getName();
        open();

        $("[name=example_length]").selectOption("100");
        sleep(1000);

        Locator tableBaseElement = page.locator("#example tbody");

        Locator rows = tableBaseElement.locator("tr");

        Long timeStart = System.currentTimeMillis();

        for (ElementHandle row : rows.elementHandles()) {
            String age = Integer.toString(faker.number().numberBetween(30, 60));
            String position = faker.job().position();
            String city = getRandomCity();

            row.querySelector("[id$=-age]").fill(age);
            row.querySelector("[id$=-position]").fill(position);
            row.querySelector("[id$=-office]").selectOption(city);
        }

        Long endTime = System.currentTimeMillis();

        log.info("tableDataTestPlaywright Time total: " + ((endTime - timeStart) / 1000));
        // closeWebDriver();
        playwright.close();
        closeWebDriver();
        timeStat.setSelenideOverDebugPort(((endTime - timeStart) / 1000));
    }

    @Test(dataProvider = "dataProvider", priority = 1)
    public void tableDataTestSelenide(boolean fastSetValue) {
        Configuration.browser = "chrome";
        Configuration.headless = headless;
        Configuration.fastSetValue = fastSetValue;
        open("https://datatables.net/examples/api/form.html");

        $("[name=example_length]").selectOption("100");
        sleep(1000);

        SelenideElement tableBaseElement = $("#example tbody");

        ElementsCollection rows = tableBaseElement.$$("tr");

        Long timeStart = System.currentTimeMillis();

        for (SelenideElement row : rows) {
            String age = Integer.toString(faker.number().numberBetween(30, 60));
            String position = faker.job().position();
            String city = getRandomCity();

            row.$("[id$=-age]").setValue(age);
            row.$("[id$=-position]").setValue(position);
            row.$("[id$=-office]").selectOptionByValue(city);
        }

        Long endTime = System.currentTimeMillis();

        log.info("tableDataTestSelenide fastSetValue= %s Time total: ".formatted(fastSetValue) + ((endTime - timeStart) / 1000));
        closeWebDriver();
        if (!fastSetValue)
            timeStat.setSelenidePure(((endTime - timeStart) / 1000));
        else
            timeStat.setSelenideJSInput(((endTime - timeStart) / 1000));
    }


    @SneakyThrows
    @Test(priority = 10)
    public void tableDataTestPlaywright() {
        Playwright playwright = Playwright.create();
        Browser chrome = playwright.chromium().launch(new LaunchOptions()
                .setHeadless(headless).setChannel("chrome"));

        Page page = chrome.newPage();
        page.navigate("https://datatables.net/examples/api/form.html");

        page.selectOption("[name=example_length]", "100");
        Thread.sleep(1000);
        Locator tableBaseElement = page.locator("#example tbody");

        Locator rows = tableBaseElement.locator("tr");

        Long timeStart = System.currentTimeMillis();

        for (ElementHandle row : rows.elementHandles()) {
            String age = Integer.toString(faker.number().numberBetween(30, 60));
            String position = faker.job().position();
            String city = getRandomCity();

            row.querySelector("[id$=-age]").fill(age);
            row.querySelector("[id$=-position]").fill(position);
            row.querySelector("[id$=-office]").selectOption(city);
        }

        Long endTime = System.currentTimeMillis();

        log.info("tableDataTestPlaywright Time total: " + ((endTime - timeStart) / 1000));
        playwright.close();
        timeStat.setPwPure(((endTime - timeStart) / 1000));
    }


    @Test(priority = 20)
    public void testCombineSelenideAndPW() {
        Configuration.browser = "chrome";
        Configuration.headless = headless;
        open("https://datatables.net/examples/api/form.html");
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().connectOverCDP(
                ((RemoteWebDriver) webdriver().object())
                        .getCapabilities()
                        .getCapability("se:cdp")
                        .toString());
        BrowserContext browserContext = browser.contexts().get(0);
        Page page = browserContext.pages().get(0);

        $("[name=example_length]").selectOption("100");
        sleep(1000);

        Locator tableBaseElement = page.locator("#example tbody");

        Locator rows = tableBaseElement.locator("tr");

        Long timeStart = System.currentTimeMillis();

        for (ElementHandle row : rows.elementHandles()) {
            String age = Integer.toString(faker.number().numberBetween(30, 60));
            String position = faker.job().position();
            String city = getRandomCity();

            row.querySelector("[id$=-age]").fill(age);
            row.querySelector("[id$=-position]").fill(position);
            row.querySelector("[id$=-office]").selectOption(city);
        }

        Long endTime = System.currentTimeMillis();

        log.info("testCombineSelenideAndPW Time total: " + ((endTime - timeStart) / 1000));


        browserContext.close();
        playwright.close();
        timeStat.setPwPlusSelenide((endTime - timeStart) / 1000);
        closeWebDriver();
    }

    private String getRandomCity() {
        Random random = new Random();
        return cities.get(random.nextInt(cities.size()));
    }

}
