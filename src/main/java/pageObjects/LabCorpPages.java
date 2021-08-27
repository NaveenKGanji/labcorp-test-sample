package pageObjects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class LabCorpPages {
    WebDriver driver;

    @FindBy(xpath = "//a[contains(text(),'Careers')]")
    public WebElement careersLink;

    @FindBy(id = "onetrust-accept-btn-handler")
    public WebElement acceptCookiesButtonOnHomePage;

    @FindBy(css = "div.search-block > div > h2")
    public WebElement searchOurJobsText;

    @FindBy(id = "gdpr-button")
    public WebElement acceptCookiesButtonOnCareerPage;

    @FindBy(xpath = "//input[@class='search-keyword']")
    public WebElement keyWordJobSearchBoxOnCareerPage;

    @FindBy(xpath = "//button[contains(text(),'Submit')]")
    public WebElement submitButtonOnCareerPage;

    @FindBy(xpath = "//input[@class='search-location']")
    public WebElement locationSearchBoxOnCareerPage;

    @FindBy(xpath = "//a/h2[contains(text(),'QA Test Automation Developer')]")
    public WebElement jobLinkFromSearchResults;

    @FindBy(css = "div.jd-main-wrapper > section.job-description > h1")
    public WebElement jobHeader;

    @FindBy(xpath = "(//div[@class='job-description__info-items']/span)[1]")
    public WebElement jobLocation;

    @FindBy(xpath = "(//div[@class='job-description__info-items']/span)[2]")
    public WebElement jobID;

    @FindBy(css = "div > span > p")
    public WebElement requirementParagraph;

    @FindBy(xpath = "//a[@class='button job-apply bottom']")
    public WebElement applyNowButton;

    @FindBy(css = "span.jobTitle.job-detail-title")
    public WebElement jobHeaderOnCareerSitePage;

    @FindBy(css = "span.jobnum")
    public WebElement jobIdOnCareerSitePage;

    @FindBy(css = "span.resultfootervalue")
    public WebElement joblocationOnCareerSitePage;

    @FindBy (xpath = "//div[contains(text(),'Department')]")
    public WebElement departmentTextOnCareerSite;

    @FindBy (xpath = "//button[@class='btn btn-secondary ae-button']")
    public WebElement returnToJobSearch;

    @FindBy (xpath = "//button[@class='btn btn-primary btn-icon-right search-btn ae-button']")
    public WebElement searchForJobButton;

    @FindBy (xpath = "//button[@class='close closebutton ae-button']")
    public WebElement simpleClickSignOn;



    public LabCorpPages(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }

    public void clickOnCareersLink() {
        careersLink.click();
    }

    public void acceptCookiesOnHomePage() {
        acceptCookiesButtonOnHomePage.click();
    }


}
