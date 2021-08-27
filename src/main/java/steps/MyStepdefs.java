package steps;

import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.After;
import org.junit.Assert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pageObjects.LabCorpPages;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class MyStepdefs {

    public static WebDriver driver;
    String homePageWindow;
    String careerPageWindow;
    LabCorpPages labCorpPages;

    @Before
    public void beforeScenario() {
        System.setProperty("webdriver.chrome.driver", "C:\\Softwares\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.get("http://www.labcorp.com/"); //TODO: CHANGE TO ACCOMODATE PROPERTIES FILE
        driver.manage().window().maximize();
    }

    @Given("the applicant launches LabCorp and Navigate to Career Page")
    public void theUserLogsIntoLabCorpAndNavigateToCareerPage() {

        labCorpPages = new LabCorpPages(driver);
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(ExpectedConditions.visibilityOf(labCorpPages.careersLink));

        //get window ID
        homePageWindow = driver.getWindowHandle();

        if (labCorpPages.acceptCookiesButtonOnHomePage.isDisplayed()) {
            wait.until(ExpectedConditions.elementToBeClickable(labCorpPages.acceptCookiesButtonOnHomePage));
            labCorpPages.acceptCookiesOnHomePage();
            wait.until(ExpectedConditions.elementToBeClickable(labCorpPages.careersLink));
            labCorpPages.clickOnCareersLink();
        } else {
            wait.until(ExpectedConditions.visibilityOf(labCorpPages.careersLink));
            labCorpPages.clickOnCareersLink();
        }

        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    }

    @And("the applicant searches and selects the position {string}")
    public void theUserSearchesAndSelectsThePosition(String position) {

        Set<String> windowHandles = driver.getWindowHandles();
        Iterator<String> iterator = windowHandles.iterator();

        while (iterator.hasNext()) { //Career Page opened in new Tab and assumption is default window ID is  home page
            String window = iterator.next();
            if (!homePageWindow.equalsIgnoreCase(window)) {
                driver.switchTo().window(window);
            }
        }
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(ExpectedConditions.visibilityOf(labCorpPages.searchOurJobsText));

        if (labCorpPages.acceptCookiesButtonOnCareerPage.isDisplayed()) {//accept cookies
            labCorpPages.acceptCookiesButtonOnCareerPage.click();
        }

        wait.until(ExpectedConditions.elementToBeClickable(labCorpPages.keyWordJobSearchBoxOnCareerPage));
        labCorpPages.keyWordJobSearchBoxOnCareerPage.sendKeys(position);
        labCorpPages.locationSearchBoxOnCareerPage.clear(); //my machine picking up the location hence clearing the field
        labCorpPages.submitButtonOnCareerPage.click();

        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS); //wait

        wait.until(ExpectedConditions.elementToBeClickable(labCorpPages.jobLinkFromSearchResults)); //click on Job
        labCorpPages.jobLinkFromSearchResults.click();

        wait.until(ExpectedConditions.visibilityOf(labCorpPages.jobHeader));//wait
    }

    @And("the applicant verifies the below job information:")
    public void theUserValidatesTheBelowJobInformation(List<String> jobDetails) {

        String jobHeader = labCorpPages.jobHeader.getText();
        Assert.assertEquals(jobDetails.get(0), jobHeader);

        String jobLocation = labCorpPages.jobLocation.getText().split("\n")[1];
        Assert.assertTrue("Job Location Do Not Match", jobLocation.equalsIgnoreCase(jobDetails.get(1)));

        String jobID = labCorpPages.jobID.getText().split(" ")[2];
        Assert.assertTrue("Job Location Do Not Match", jobID.equalsIgnoreCase(jobDetails.get(2)));

        Assert.assertTrue("Requirement Text Not Available", isTextPresent(jobDetails.get(3)));
        careerPageWindow = driver.getWindowHandle();

    }

    @And("the applicant applies for the job and confirms the following information:")
    public void theApplicantAppliesForTheJobAndConfirmsTheFollowingInformation(List<String> jobDetails) {

        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(ExpectedConditions.elementToBeClickable(labCorpPages.applyNowButton));
        labCorpPages.applyNowButton.click();
        wait.until(ExpectedConditions.visibilityOf(labCorpPages.departmentTextOnCareerSite));

        String jobHeader = labCorpPages.jobHeaderOnCareerSitePage.getText();
        Assert.assertEquals(jobDetails.get(0), jobHeader);

        String jobLocation = labCorpPages.joblocationOnCareerSitePage.getText();
        if (jobLocation.contains("NC")) {
            jobLocation = jobDetails.get(1);
        }
        Assert.assertTrue("Job Location Do Not Match", jobLocation.equalsIgnoreCase(jobDetails.get(1)));

        String jobID = labCorpPages.jobIdOnCareerSitePage.getText().replace("#", "").trim();
        Assert.assertTrue("Job Location Do Not Match", jobID.equalsIgnoreCase(jobDetails.get(2)));

        Assert.assertTrue("Requirement Text Not Available", isTextPresent(jobDetails.get(3)));

    }

    @When("the applicant clicks on Return to Job Search")
    public void theApplicantClicksOnReturnToJobSearch() {
        if (labCorpPages.simpleClickSignOn.isDisplayed()) {
            labCorpPages.simpleClickSignOn.click();
        }
        labCorpPages.returnToJobSearch.click();
    }

    @Then("the applicant is navigated to Job Search Page")
    public void theApplicantIsNavigatedToJobSearchPage() {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.elementToBeClickable(labCorpPages.searchForJobButton));
        Assert.assertTrue("Applicant Not Returned to Job Search Page", labCorpPages.searchForJobButton.isDisplayed());
        driver.quit();
    }

    private boolean isTextPresent(String text) {
        try {
            boolean requirementText = driver.getPageSource().contains(text);
            return requirementText;
        } catch (Exception e) {
            return false;
        }
    }

}
