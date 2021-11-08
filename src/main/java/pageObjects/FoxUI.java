package util.foxUiUtils;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import cucumber.api.java.sl.In;
import org.fluentlenium.core.action.KeyboardElementActions;
import org.jetbrains.annotations.*;
import com.uhc.aarp.fox.common.utils.DateUtils;
import cucumber.api.Scenario;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.math.NumberUtils;
import org.fluentlenium.core.FluentPage;
import org.fluentlenium.core.domain.FluentList;
import org.fluentlenium.core.domain.FluentWebElement;
import org.junit.Assert;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui.foxPages.common.UIUtil;
import util.endToEnd.SharedMemory;
import util.endToEnd.helpers.E2EUtils;
import util.integration.ReportUtil;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by schinnag on 4/10/2018.
 * Class will contain base methods to manipulate the Fox-UI application
 */
public final class FoxUI extends FluentPage {
    //Variables
    private static boolean isMatTypeTbl = false;
    private static String headerRowTag = "";
    private static String headerColTag = "";
    private static String rowTag = "";
    private static String colTag = "";

    public static boolean ignoreSyncError = false;
    public static String loginRole = "";

    @FindBy(css = "fox-ui-app")
    public static FluentWebElement foxUiApp;
    private static Logger logger = LoggerFactory.getLogger(FoxUI.class);

    public FoxUI() {
    }

    public static void setIgnoreSyncError(boolean ignoreError) {
        ignoreSyncError = ignoreError;
    }

    //------------------- DROPDOWN -----------------------

    /**
     * This will select a value from a drop down list in Fox-Ui application
     *
     * @param dropDownList       - Dropdown element
     * @param listOptionToSelect - which option/item to select on the dropdown
     */
    public static void selectValueFromDropDown(FluentWebElement dropDownList, String listOptionToSelect) throws Throwable {

        if (listOptionToSelect.trim().length() == 0)
            return;

        String dropDownCurrentText = dropDownList.text().trim();
        if (dropDownCurrentText.contains(listOptionToSelect)) {
            return;
        } else if (!(dropDownCurrentText.isEmpty()))
            SharedMemory.getInstance().scenario.write("updated from: " + dropDownCurrentText + " TO: " + listOptionToSelect);

        //Wait for the Element
        try {
            dropDownList.await().atMost(30, TimeUnit.SECONDS).until(dropDownList).clickable();
        } catch (Exception e1) {
            String errors = getErrorMessages();
            if (errors.isEmpty())
                throw new Exception("Element not available/enabled to enter_temp value:" + dropDownList.html());
            else throw new Exception(errors.toUpperCase());
        }

        //if normal HTML element
        if (dropDownList.tagName().equalsIgnoreCase("select")) {
            dropDownList.fillSelect().withValue(listOptionToSelect);
            return;
        }
        dropDownList.await().atMost(30, TimeUnit.SECONDS).until(dropDownList).present();

        //wait for popup
        int counter1 = 0, counter2 = 0;
        WebElement lstOptionsModal = null;
        String modalXpath;
        if (dropDownList.attribute("class").contains("ng-select")) {
            modalXpath = "ng-dropdown-panel";
        } else {
            modalXpath = "div.cdk-overlay-pane div.mat-select-panel";
        }

        do {
            //Click the list box to open the options popup
            counter1++;
            dropDownList.scrollIntoView();
            dropDownList.await().explicitlyFor(2, TimeUnit.SECONDS);
            dropDownList.click();

            try {
                do {
                    counter2++;
                    dropDownList.await().explicitlyFor(2, TimeUnit.SECONDS);
                    lstOptionsModal = dropDownList.getDriver().findElement(By.cssSelector(modalXpath));
                } while (!lstOptionsModal.isDisplayed() && counter2 < 5);
            } catch (Exception e) {
            }
        } while (!lstOptionsModal.isDisplayed() && counter1 < 3);

        if (!lstOptionsModal.isDisplayed())
            throw new Exception("Problem in selecting drop-down value. Items list pop-up not displayed.");

        //Find toQueue option from the list and select
        WebElement targetOption = null;
        String caseInsensitive = "normalize-space(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))"; //lower-case(text()) supports only xpath 2.0
        listOptionToSelect = listOptionToSelect.toLowerCase(); //make case-insensitive for finding item
        try {
            dropDownList.await().explicitlyFor(2, TimeUnit.SECONDS);
            dropDownList.scrollIntoView();
            lstOptionsModal = dropDownList.el(lstOptionsModal).reset().getElement();
            targetOption = lstOptionsModal.findElement(By.xpath("//span[" + caseInsensitive + "='" + listOptionToSelect + "']")); ////mat-option
            ((JavascriptExecutor) dropDownList.getDriver()).executeScript("arguments[0].scrollIntoView();", targetOption);
        } catch (Exception e) {
            try {
                dropDownList.await().explicitlyFor(1, TimeUnit.SECONDS);
                dropDownList.scrollIntoView();
                lstOptionsModal = dropDownList.getDriver().findElement(By.cssSelector(modalXpath));
                targetOption = lstOptionsModal.findElement(By.xpath("//span[contains(" + caseInsensitive + ",'" + listOptionToSelect + "')]"));
                ((JavascriptExecutor) dropDownList.getDriver()).executeScript("arguments[0].scrollIntoView();", targetOption);
            } catch (Exception e1) {
                dropDownList.await().explicitlyFor(1, TimeUnit.SECONDS);
                dropDownList.scrollIntoView();
                lstOptionsModal = dropDownList.getDriver().findElement(By.cssSelector(modalXpath));
                targetOption = lstOptionsModal.findElement(By.xpath("//mat-option/span[contains(" + caseInsensitive + ",'" + listOptionToSelect + "')]"));
                ((JavascriptExecutor) dropDownList.getDriver()).executeScript("arguments[0].scrollIntoView();", targetOption);
            }

        }

        ((JavascriptExecutor) dropDownList.getDriver()).executeScript("arguments[0].scrollIntoView();", targetOption);
        targetOption.click();
    }

    /**
     * This will select a value from a drop down list in Fox-Ui application
     *
     * @param dropDownList       - Dropdown element
     * @param listOptionToSelect - which option/item to select on the dropdown
     */
    public static void selectValueFromDropDown_temp(FluentWebElement dropDownList, String listOptionToSelect) throws Throwable {

        if (listOptionToSelect.trim().length() == 0)
            return;

        dropDownList.await().atMost(15, TimeUnit.SECONDS).until(dropDownList).present();
        FoxUI.click(dropDownList);

        //wait for popup
        int counter1 = 0, counter2 = 0;
        WebElement lstOptionsModal = null;
        String modalXpath;
        if (dropDownList.attribute("class").contains("ng-select")) {
            modalXpath = "ng-dropdown-panel";
        } else {
            modalXpath = "div.cdk-overlay-pane div.mat-select-panel";
        }

        do {
            //Click the list box to open the options popup
            counter1++;
            dropDownList.scrollIntoView();
            dropDownList.await().explicitlyFor(2, TimeUnit.SECONDS);
            FoxUI.click(dropDownList);

            try {
                do {
                    counter2++;
                    dropDownList.await().explicitlyFor(2, TimeUnit.SECONDS);
                    lstOptionsModal = dropDownList.getDriver().findElement(By.cssSelector(modalXpath));
                } while (!lstOptionsModal.isDisplayed() && counter2 < 5);
            } catch (Exception e) {
                logger.info("Failed to detect dropdownlist");
            }

        } while (!lstOptionsModal.isDisplayed() && counter1 < 2);

        if (!lstOptionsModal.isDisplayed())
            throw new Exception("Problem in selecting drop-down value. Items list pop-up not displayed.");

        //Find option from the list and select
        WebElement targetOption = null;
        try {
            dropDownList.await().explicitlyFor(2, TimeUnit.SECONDS);
            lstOptionsModal = dropDownList.el(lstOptionsModal).reset().getElement();
            targetOption = lstOptionsModal.findElement(By.xpath("//span[normalize-space(text())='" + listOptionToSelect + "']"));
        } catch (Exception e) {
            try {
                dropDownList.await().explicitlyFor(1, TimeUnit.SECONDS);
                lstOptionsModal = dropDownList.getDriver().findElement(By.cssSelector(modalXpath));
                targetOption = lstOptionsModal.findElement(By.xpath("//span[contains(normalize-space(text()),'" + listOptionToSelect + "')]"));
            } catch (Exception e1) {
                dropDownList.await().explicitlyFor(1, TimeUnit.SECONDS);
                lstOptionsModal = dropDownList.getDriver().findElement(By.cssSelector(modalXpath));
                targetOption = lstOptionsModal.findElement(By.xpath("//mat-option/span[contains(normalize-space(text()),'" + listOptionToSelect + "')]"));
            }
        }

        targetOption.click();
    }

    /**
     * This will get the current selected option from the drop down list in Fox-Ui application
     *
     * @param dropDownList - Dropdown element
     */
    public static String getSelectedValueFromDropDown(FluentWebElement dropDownList) throws Throwable {
        //Get the text
        WebElement temp = getDropDownTextElement(dropDownList);
        return temp.getText().trim();
    }

    /**
     * Reworked selectValueFromDropdown due to conflicts with some scripts
     *
     * @author ksadullo
     */
    public static void selectValueFromDropdownRewrk(FluentWebElement dropdown, String listOptionToSelect) throws Throwable {
        String listElement = "//mat-option//*[text()='%s']";

        dropdown.await().atMost(10, TimeUnit.SECONDS).until(dropdown).present();
        FoxUI.click(dropdown);

        listElement = String.format(listElement, listOptionToSelect);
        FluentWebElement optionToSelect = dropdown.el(By.xpath(listElement));
        dropdown.await().atMost(10, TimeUnit.SECONDS).until(optionToSelect).present();
        FoxUI.click(optionToSelect);
    }

    /**
     * This will get the text element present inside the drop down list in Fox-Ui application
     *
     * @param dropDownList - Dropdown element
     */
    private static WebElement getDropDownTextElement(FluentWebElement dropDownList) throws Throwable {
        //Wait for the Element
        dropDownList.await().atMost(15, TimeUnit.SECONDS).until(dropDownList).clickable();

        //Get Element
        WebElement temp = null;

        String valueLocator = "//div/div[@class='mat-select-value']|//*[@role='listbox']";
        try {
            temp = dropDownList.getElement().findElement(By.xpath(valueLocator));
        } catch (Exception e) {
            try {
                temp = dropDownList.getElement().findElement(By.xpath(valueLocator));
            } catch (Exception x) {
                temp = dropDownList.getElement().findElement(By.xpath(valueLocator));
            }
        }
        if (temp == null) throw new Exception("Mentioned dropdown not available in the UI.");

        return temp;
    }

    /**
     * This will get all the options/items in the drop down list of Fox-Ui application
     *
     * @param dropDownList - Dropdown element
     */
    public static List<String> getAllOptionsFromDropDown(FluentWebElement dropDownList) throws Throwable {
        List<String> options = new ArrayList<String>();

        //Wait for the Element
        dropDownList.await().atMost(15, TimeUnit.SECONDS).until(dropDownList).clickable();


        //wait for popup
        int counter1 = 0, counter2 = 0;
        WebElement lstOptionsModal = null;
        String modalXpath = "//div[@class='cdk-overlay-pane']/div[contains(@class,'mat-select-panel')]";
        do {
            //Click the list box to open the options popup
            counter1++;
            new FoxUI().focusElement(dropDownList);
            dropDownList.click();

            try {
                do {
                    counter2++;
                    dropDownList.await().explicitlyFor(2, TimeUnit.SECONDS);
                    lstOptionsModal = dropDownList.getDriver().findElement(By.xpath(modalXpath));
                } while (!lstOptionsModal.isDisplayed() && counter2 < 5);
            } catch (Exception e) {
            }
        } while (!lstOptionsModal.isDisplayed() && counter1 < 3);

        //Find toQueue option from the list and select
        List<WebElement> targetOptions = null;
        try {
            targetOptions = lstOptionsModal.findElements(By.xpath("//mat-option/span"));
        } catch (Exception e) {
            lstOptionsModal = dropDownList.getDriver().findElement(By.xpath(modalXpath));
            targetOptions = lstOptionsModal.findElements(By.xpath("//mat-option/span"));
        }

        for (WebElement tempE : targetOptions) {
            options.add(tempE.getText().trim());
        }


        return options;
    }


    //-------------------- TEXT BOX-----------------------

    /**
     * This will get column indexes for a table element of Fox-Ui application
     *
     * @param element - textBox element
     * @param txt     - value to enter_temp in textbox
     */
    public static FluentWebElement sendKeys(FluentWebElement element, String txt) throws Throwable {
        int delayInMS = 0;
        if (element.attribute("placeholder") != null) {
            if (element.attribute("placeholder").matches("\\w{2}\\/\\w{2}\\/{0,1}\\w{0,4}"))
                delayInMS = 200;
        }

        return sendKeys(element, txt, delayInMS);
    }

    public static FluentWebElement sendKeys(FluentWebElement elementSection, String fieldLabel, String txt) throws Throwable {
        FluentWebElement rootEle, targetEle;

        if (txt.trim().isEmpty())
            return elementSection;

        //Get parent element
        if (elementSection == null)
            rootEle = foxUiApp;
        else
            rootEle = elementSection;

        //Find field label/element
        try {
            targetEle = rootEle.el(By.cssSelector("fox-input[labelText='" + fieldLabel + "']"));
            targetEle.displayed();

            targetEle = targetEle.el(By.cssSelector("input"));
        } catch (NoSuchElementException e1) {
            try {
                targetEle = rootEle.el(By.xpath("//label[text()='" + fieldLabel + "']/../..//input"));
                targetEle.displayed();
            } catch (Exception e2) {
                targetEle = rootEle.el(By.xpath("//mat-label[text()='" + fieldLabel + "']/../../..//input"));
                targetEle.displayed();
            }
        } catch (Exception e) {
            throw new Exception("Field not found to edit: " + fieldLabel);
        }

        //edit
        return sendKeys(targetEle, txt, 0);
    }

    public static FluentWebElement sendKeys(FluentWebElement element, String txt, int delayInMS) throws Throwable {

        if (txt.trim().isEmpty())
            return element;

        try {
            element.scrollIntoView();
            element.await().atMost(15, TimeUnit.SECONDS).until(element).displayed();
            element.await().atMost(1, TimeUnit.SECONDS).until(element).enabled();
        } catch (Exception e1) {
            String errors = getErrorMessages();
            if (errors.isEmpty())
                throw new Exception("Element not available/enabled to enter the value:" + element.html());
            else throw new Exception(errors.toUpperCase());
        }

        //Each method call on a webElement cause an I/O to saucelabs so use local variables
        String elementText = element.text().trim();
        String elementValue = element.value().trim();


        //if select element
        if (element.tagName().equalsIgnoreCase("select")) {
            if (elementText.equals(txt) || elementValue.equals(txt)) {
                return element;
            } else if (!(elementText.isEmpty() && elementValue.isEmpty()))
                SharedMemory.getInstance().scenario.write("updated from: " + (elementText.isEmpty() ? elementValue : elementText) + " TO: " + txt);

            element.fillSelect().withValue(txt);
            return element;
        }

        try {
            String elementAttribute = element.attribute("placeholder").trim();
            if (!elementAttribute.isEmpty()) {
                if (elementAttribute.equalsIgnoreCase("MM/DD/YYYY")) {
                    txt = txt.substring(0, 2) + "/" + txt.substring(2, 4) + "/" + txt.substring(4, 8);
                } else if (elementAttribute.equalsIgnoreCase("MM/DD/YY")) {
                    txt = txt.substring(0, 2) + "/" + txt.substring(2, 4) + "/" + txt.substring(6, 8);
                } else if (elementAttribute.equalsIgnoreCase("MM/YY")) {
                    txt = txt.substring(0, 2) + "/" + txt.substring(6, 8);
                } else if (elementAttribute.equalsIgnoreCase("MM/DD")) {
                    txt = txt.substring(0, 2) + "/" + txt.substring(2, 4);
                }
            }
            if (elementText.equals(txt) || elementValue.equals(txt)) {
                return element;
            } else if (!(elementText.isEmpty() && elementValue.isEmpty()))
                SharedMemory.getInstance().scenario.write("updated from: " + (elementText.isEmpty() ? elementValue : elementText) + " TO: " + txt);


            element.clear();

            //TRY-1
            for (int i = 0; i < txt.length(); i++) {
                element.getWrappedElement().sendKeys(txt.charAt(i) + "");
                if (delayInMS > 0) element.await().explicitlyFor(delayInMS, TimeUnit.MILLISECONDS);
            }

            //TRY-2
            if (element.text().isEmpty() && element.textContent().isEmpty() && element.attribute("value").isEmpty()) {
                element.scrollIntoView();
                element.mouse().moveToElement().click();
                element.keyboard().sendKeys(txt);
            }

            //TRY-3
            if (element.text().isEmpty() && element.textContent().isEmpty() && element.attribute("value").isEmpty())
                ((JavascriptExecutor) element.getDriver()).executeScript("arguments[0].value='" + txt + "'", element);
        } catch (Exception e) {
            throw new Exception("Problem while entering value in text field:" + e.getMessage());
        }
        return element;
    }

    public String getTextFromTextField(FluentWebElement textBox) throws Exception {

        if (textBox == null)
            throw new Exception("Expected Text Field is not present/populated on Page.");
        else
            return textBox.value();

    }

    public void enterSeriesDetails(LinkedList<String> values, FluentWebElement rootElement, String locator,
                                   int rowIdx, int startColIdx) throws Throwable {
        int colIdx = startColIdx - 1;
        FluentWebElement currEle;
        for (String value : values) {
            colIdx++;

            if (value.isEmpty()) {
                continue;
            }

            if (rootElement == null) {
                currEle = foxUiApp.el(By.cssSelector(locator + colIdx));
                if (!currEle.present())
                    currEle = foxUiApp.el(By.xpath("//table/tbody/tr[" + (rowIdx + 1) + "]/td[" + (colIdx + 1) + "]//input"));
                if (!currEle.present())
                    currEle = foxUiApp.el(By.xpath("//table/tbody/tr[" + (rowIdx + 1) + "]/td[" + (colIdx + 1) + "]//ng-select"));
            } else {
                currEle = rootElement.el(By.cssSelector(locator + colIdx));
                if (!currEle.present())
                    currEle = rootElement.el(By.xpath("//tr[" + (rowIdx + 1) + "]/td[" + (colIdx + 1) + "]//input"));
            }


            sendKeys(currEle, value);
        }
    }

    public void removeSeriesDetails(FluentWebElement rootElement, String locator,
                                    int rowIdx, int startColIdx, int endColIdx) throws Throwable {
        FluentWebElement currEle;
        for (int colIdx = startColIdx - 1; colIdx < endColIdx; colIdx++) {
            if (rootElement == null) {
                currEle = foxUiApp.el(By.cssSelector(locator + colIdx));
                if (!currEle.present())
                    currEle = foxUiApp.el(By.xpath("//table/tbody/tr[" + (rowIdx + 1) + "]/td[" + (colIdx + 1) + "]//input"));
                if (!currEle.present())
                    currEle = foxUiApp.el(By.xpath("//table/tbody/tr[" + (rowIdx + 1) + "]/td[" + (colIdx + 1) + "]//ng-select"));
            } else {
                currEle = rootElement.el(By.cssSelector(locator + colIdx));
                if (!currEle.present())
                    currEle = rootElement.el(By.xpath("//tr[" + (rowIdx + 1) + "]/td[" + (colIdx + 1) + "]//input"));
            }
            currEle.write(" ");
            currEle.getElement().sendKeys(Keys.BACK_SPACE);
        }
    }

    /**
     * This will get column indexes for a table element of Fox-Ui application
     *
     * @param textBox     - textBox element
     * @param textToEnter - value to enter_temp in textbox
     */

    public static void enterTextInTextField_temp(FluentWebElement textBox, String textToEnter) throws Exception {
        if (textBox == null)
            throw new Exception("Expected Text Field is not present/populated on Page.");
        else {
            if (textBox.enabled()) {
                if (!textToEnter.equalsIgnoreCase("")) {
                    textBox.scrollIntoView();
                    textBox.click();
                    textBox.fill().with(textToEnter);
                    Assert.assertEquals("Text was not successfully entered into text box.",
                            textToEnter.replaceAll(" ", ""), textBox.value().replaceAll(" ", "").replace("-", "").replaceAll(",", ""));
                    Thread.sleep(1000);
                }
            } else {
                logger.info("Expected Text is not enabled on Page.");
                throw new RuntimeException();
            }
        }
    }

    public static FluentWebElement sendKeys_temp(FluentWebElement element, String txt) throws Throwable {
        if (!(element.present() && element.enabled()))
            throw new Exception("Element not available to enter_temp value.");

        try {
            element.mouse().moveToElement().click();
            element.keyboard().sendKeys(txt);
            if (element.text().isEmpty() && element.textContent().isEmpty() && element.attribute("value").isEmpty())
                ((JavascriptExecutor) element.getDriver()).executeScript("arguments[0].value='" + txt + "'", element);
        } catch (Exception e) {
            try {
                for (int i = 0; i < txt.length(); i++) element.getWrappedElement().sendKeys(txt.charAt(i) + "");
            } catch (Exception e1) {
                throw new Exception("Problem while entering value in text field.");
            }
        }
        return element;
    }

    public String getTextFromTextField_temp(FluentWebElement textBox) throws Exception {

        if (textBox == null)
            throw new Exception("Expected Text Field is not present/populated on Page.");
        else
            return textBox.value();

    }

    /**
     * Get text from element that is not reached
     *
     * @param element
     * @return
     * @auth ksadullo
     */
    public static String getTextFromField(FluentWebElement element) {
        element.await().atMost(10, TimeUnit.SECONDS).until(element).present();
        JavascriptExecutor js = (JavascriptExecutor) element.getDriver();
        String text = js.executeScript("return arguments[0].innerHTML;", element.getWrappedElement()).toString();
        return text;
    }

    public FluentWebElement getInputElement(FluentWebElement rootEle, String fieldLabel) throws Throwable {
        //Find field label/element
        FluentWebElement targetEle;
        try {
            if (rootEle == null)
                rootEle = foxUiApp;

            targetEle = rootEle.el(By.cssSelector("fox-input[labelText='" + fieldLabel + "']"));
            targetEle = targetEle.el(By.cssSelector("input"));
        } catch (Exception e) {
            throw new Exception("Field not found to edit: " + fieldLabel);
        }
        return targetEle;
    }


    //---------------------- TABLE ----------------------------

    /**
     * @param table - required table FluentWebElement type
     * @return actual table web element
     * @throws Throwable
     */
    private static WebElement getTable(FluentWebElement table) throws Throwable {
        //Wait for the element
        table.await().atMost(60, TimeUnit.SECONDS).until(table).clickable();

        //Get the table element
        WebElement eTable = null;
        if (table.tagName().equalsIgnoreCase("table")) {
            setTable();
            eTable = table.getElement();
        } else if (table.tagName().equalsIgnoreCase("mat-table")) {
            setMatTable();
            eTable = table.getElement();
        } else if (table.tagName().equalsIgnoreCase("fox-table")) {
            setTable();
            eTable = table.getElement();
        } else {
            try {
                eTable = table.getElement().findElement(By.xpath("table"));
                setTable();
            } catch (Exception e) {
                try {
                    eTable = table.getElement().findElement(By.xpath("mat-table"));
                    setMatTable();
                } catch (Exception f) {
                    throw new Exception("Mentioned table not present in current page.");
                }
            }
        }

        return eTable;
    }

    private static void setTable() {
        isMatTypeTbl = false;
        headerRowTag = "thead/tr";
        headerColTag = "th";
        rowTag = "tbody/tr";
        colTag = "td";
    }

    private static void setMatTable() {
        isMatTypeTbl = true;
        headerRowTag = "mat-header-row";
        headerColTag = "mat-header-cell";
        rowTag = "mat-row";
        colTag = "mat-cell";
    }

    /**
     * This will get column indexes for a table element of Fox-Ui application
     *
     * @param table - table element
     */
    public static Map<String, Integer> getTableColumnIndexes(FluentWebElement table) throws Throwable {

        Map<String, Integer> indexes = new HashMap<>();

        //Get the table element
        WebElement eTable = getTable(table);

        //Get the table headers based on type
        List<WebElement> headers = null;
        headers = eTable.findElements(By.xpath(headerRowTag + "/" + headerColTag));

        //Get column header row
        String columnName = "";
        ListIterator<WebElement> it = headers.listIterator();
        while (it.hasNext()) {
            WebElement tableHeader = it.next();
            columnName = tableHeader.getText().trim().toUpperCase().replace("\n", " ");
            if (columnName.trim().isEmpty()) {
                columnName = tableHeader.getAttribute("textContent").toUpperCase();
            }
            if (columnName.contains("SORTED BY"))
                columnName = columnName.substring(0, columnName.indexOf("SORTED BY")).trim();

            indexes.put(columnName, it.nextIndex() - 1);
        }
        //Return
        return indexes;
    }

    private static String getColumnValue(WebElement eleColumn) throws Throwable {
        String colValue = eleColumn.getText().trim().isEmpty() ?
                (eleColumn.getAttribute("innerText").trim().isEmpty() ?
                        (eleColumn.getAttribute("value") == null ? "" : eleColumn.getAttribute("value").trim())
                        : eleColumn.getAttribute("innerText").trim())
                : eleColumn.getText().trim();

        if (colValue.isEmpty()) {
            try {
                WebElement tempInputEle = eleColumn.findElement(By.tagName("input"));
                if (tempInputEle.isDisplayed())
                    colValue = tempInputEle.getText().trim().isEmpty() ?
                            (tempInputEle.getAttribute("innerText").trim().isEmpty() ?
                                    (tempInputEle.getAttribute("value") == null ? "" : tempInputEle.getAttribute("value").trim())
                                    : tempInputEle.getAttribute("innerText").trim())
                            : tempInputEle.getText().trim();
            } catch (Exception e) {
            }
        }

        colValue = colValue.contains("$") ? colValue.replace("$", "") : colValue;
        return colValue;
    }

    /**
     * This will find a row in the table element of Fox-Ui application
     *
     * @param table                 - table element
     * @param columnValuesToFindRow - column values to match the row ex. COL1=VAL1;COL2=VAL2
     */
    public static int findTableRow(FluentWebElement table, String columnValuesToFindRow) throws Throwable {
        Map<String, Integer> indexes = new HashMap<>();
        String[] columns, values;

        //Split the columns and values
        String[] temp = columnValuesToFindRow.split(";");
        columns = new String[temp.length];
        values = new String[temp.length];
        for (int t = 0; t < temp.length; t++) {
            columns[t] = temp[t].split("=")[0].trim();
            values[t] = temp[t].split("=")[1].trim();
        }

        //Get the table element
        WebElement eTable = getTable(table);

        //Get the indexes
        indexes = getTableColumnIndexes(table);  //TODO - pass onlyRequiredColumns

        //Get each row and search
        List<WebElement> rows = null;
        rows = eTable.findElements(By.xpath(rowTag));
        ListIterator<WebElement> it = rows.listIterator();
        List<WebElement> cols = null;
        WebElement currCol = null;
        String currColValue = "";
        int match = 0;
        while (it.hasNext()) {
            //Reset match
            match = 0;

            //Get all columns for current row
            cols = it.next().findElements(By.xpath(colTag));

            //For each column, check the value
            for (int c = 0; c < columns.length; c++) {
                //Get current column UI value
                currColValue = cols.get(indexes.get(columns[c].toUpperCase())).getAttribute("innerText").trim();

                if (NumberUtils.isNumber(currColValue) && NumberUtils.isNumber(values[c].trim())) { //if number
                    try {
                        if (Integer.parseInt(currColValue) == Integer.parseInt(values[c].trim())) {
                            match += 1;
                        }
                    } catch (Exception e) {
                        if (Long.parseLong(currColValue) == Long.parseLong(values[c].trim())) {
                            match += 1;
                        }
                    }

                } else {
                    if (values[c].trim().endsWith(".*")) {
                        values[c] = values[c].substring(0, values[c].indexOf(".*"));
                        if (currColValue.trim().toUpperCase().contains(values[c].trim().toUpperCase())) {
                            match += 1;
                        }
                    } else if (currColValue.trim().equalsIgnoreCase(values[c].trim())) {
                        match += 1;
                    }
                }
            }

            //See if we got the matching row
            if (match == columns.length)
                return it.nextIndex() - 1;
        }

        //Return
        return -1;
    }

    private static ArrayList<String> collectARowValues(FluentWebElement table, String columnsToFetch, List<WebElement> allColumns) throws Throwable {
        ArrayList<String> columnValuesFetched = new ArrayList<String>();

        //Get the indexes
        Map<String, Integer> indexes = getTableColumnIndexes(table);

        //Collect values
        if (columnsToFetch.isEmpty()) { //Fetch all columns
            for (int c = 0; c < allColumns.size(); c++) {
                columnValuesFetched.add(getColumnValue(allColumns.get(c)));
            }

        } else { //Fetch for only required columns
            String[] colsToFetch = columnsToFetch.trim().split(";");
            WebElement el;
            for (int c = 0; c < colsToFetch.length; c++) {
                el = allColumns.get(indexes.get(colsToFetch[c].toUpperCase()));
                columnValuesFetched.add(getColumnValue(el));
            }
        }

        return columnValuesFetched;
    }

    /**
     * This will get the mentioned column values for all the rows in the table element of Fox-Ui application
     *
     * @param table                 - table element
     * @param columnValuesToFindRow - column values to match the row ex. COL1=VAL1;COL2=VAL2
     */
    public static List<List<String>> getTableRows(FluentWebElement table, String columnValuesToFindRow, String columnsToFetch) throws Throwable {
        Map<String, Integer> indexes = new HashMap<String, Integer>();
        String[] temp = {}, columns = {}, values = {};
        List<List<String>> rowsFetched = new ArrayList<>();

        //Split the columns and values
        if (!columnValuesToFindRow.trim().isEmpty()) {
            temp = columnValuesToFindRow.split(";");
            columns = new String[temp.length];
            values = new String[temp.length];
            for (int t = 0; t < temp.length; t++) {
                columns[t] = temp[t].split("=")[0].trim();
                values[t] = temp[t].split("=")[1].trim();
            }
        }

        //Get the table element
        WebElement eTable = getTable(table);

        //Get the indexes
        indexes = getTableColumnIndexes(table);  //TODO - pass onlyRequiredColumns

        //Get each row and search
        List<WebElement> rows = eTable.findElements(By.xpath(rowTag));
        ListIterator<WebElement> it = rows.listIterator();
        List<WebElement> cols = null;
        WebElement currCol = null;
        String currColValue = "";
        int match = 0;
        while (it.hasNext()) {
            //Reset match
            ArrayList<String> columnValuesFetched = new ArrayList<String>();
            match = 0;

            //Get all columns for current row
            cols = it.next().findElements(By.xpath(colTag));

            //if need to find a particular row
            if (!columnValuesToFindRow.isEmpty()) {
                //For each column, check the value
                for (int c = 0; c < columns.length; c++) {
                    //Get current column UI value
                    currColValue = cols.get(indexes.get(columns[c].toUpperCase())).getText().trim();

                    if (NumberUtils.isNumber(currColValue) && NumberUtils.isNumber(values[c].trim())) { //if number
                        if (Long.parseLong(currColValue) == Long.parseLong(values[c].trim())) {
                            match += 1;
                        }
                    } else {
                        if (currColValue.trim().equalsIgnoreCase(values[c].trim())) {
                            match += 1;
                        }
                    }
                }
            }

            //See if we got the matching row
            if (!columnValuesToFindRow.trim().isEmpty()) {
                if (match == columns.length)
                    columnValuesFetched = collectARowValues(table, columnsToFetch, cols);
            } else
                columnValuesFetched = collectARowValues(table, columnsToFetch, cols);

            //Add to master collection
            if (!columnValuesFetched.isEmpty())
                rowsFetched.add(columnValuesFetched);

        }

        //Return
        return rowsFetched;
    }

    /**
     * This will get the mentioned column values for all the rows in the table element of Fox-Ui application
     *
     * @param table                 - table element
     * @param columnValuesToFindRow - column values to match the row ex. COL1=VAL1;COL2=VAL2
     */
    public static ArrayList<ArrayList<String>> getTableRows_temp(FluentWebElement table, String columnValuesToFindRow, String columnsToFetch) throws Throwable {
        Map<String, Integer> indexes = new HashMap<>();
        String[] temp = {}, columns = {}, values = {};
        ArrayList<ArrayList<String>> rowsFetched = new ArrayList<ArrayList<String>>();

        //Split the columns and values
        if (columnValuesToFindRow != null && columnValuesToFindRow.trim().length() > 0) {
            temp = columnValuesToFindRow.split(";");
            columns = new String[temp.length];
            values = new String[temp.length];
            for (int t = 0; t < temp.length; t++) {
                columns[t] = temp[t].split("=")[0].trim();
                values[t] = temp[t].split("=")[1].trim();
            }
        }

        //Get the table element
        WebElement eTable = getTable(table);

        //Get the indexes
        indexes = getTableColumnIndexes_temp(table);  //TODO - pass onlyRequiredColumns

        //Get each row and search
        List<WebElement> rows = eTable.findElements(By.xpath(rowTag));
        ListIterator<WebElement> it = rows.listIterator();
        List<WebElement> cols = null;
        WebElement currCol = null;
        String currColValue = "";
        int match = 0;
        while (it.hasNext()) {
            //Reset match
            ArrayList<String> columnValuesFetched = new ArrayList<String>();
            match = 0;

            //Get all columns for current row
            cols = it.next().findElements(By.xpath(colTag));

            //if need to find a particular row
            if (columnValuesToFindRow != null) {
                //For each column, check the value
                for (int c = 0; c < columns.length; c++) {
                    //Get current column UI value
                    currColValue = cols.get(indexes.get(columns[c].toUpperCase())).getText().trim();

                    if (NumberUtils.isNumber(currColValue) && NumberUtils.isNumber(values[c].trim())) { //if number
                        if (Long.parseLong(currColValue) == Long.parseLong(values[c].trim())) {
                            match += 1;
                        }
                    } else {
                        if (currColValue.trim().equalsIgnoreCase(values[c].trim())) {
                            match += 1;
                        }
                    }
                }
            } else {
                //if need to collect for all rows
                match = columns.length;
            }

            //See if we got the matching row
            if (match == columns.length) {
                columnValuesFetched = collectARowValues_temp(table, columnsToFetch, cols);
            }

            //Add to master collection
            if (!columnValuesFetched.isEmpty())
                rowsFetched.add(columnValuesFetched);

        }

        //Return
        return rowsFetched;
    }

    /**
     * This will get all the column values for all the rows in the table element of Fox-Ui application
     *
     * @param table                 - table element
     * @param columnValuesToFindRow - column values to match the row ex. COL1=VAL1;COL2=VAL2
     */
    public static List<List<String>> getTableRows(FluentWebElement table, String columnValuesToFindRow) throws Throwable {
        return getTableRows(table, columnValuesToFindRow, null);
    }

    /**
     * @param table - table element
     * @return list of rows with list of column values
     * @throws Throwable
     */
    public static List<List<String>> getTableRows(FluentWebElement table) throws Throwable {
        return getTableRows(table, "", "");
    }

    /**
     * @param table            - FluentWebElement table element to consider
     * @param targetElementRow - required row number starting from 0
     * @param elementColumn    - column name where target element is available
     * @param typeOfElement    - type of element looking for ex: DROPDOWN, LINK
     * @return requiredElement present inside the table to operate
     * @throws Throwable
     */
    private static FluentWebElement getElementInsideTable(FluentWebElement table, int targetElementRow,
                                                          String elementColumn, String typeOfElement, int elementIndex) throws Throwable {
        //Get the required row number
        targetElementRow = targetElementRow + 1;

        //Get the target element column index
        elementColumn = elementColumn.trim().toUpperCase();
        if ("space".equalsIgnoreCase(elementColumn))
            elementColumn = "";
        int targetElementCol = getTableColumnIndexes(table).get(elementColumn);
        targetElementCol = targetElementCol + 1;

        //Find the target element cell
        WebElement eleTable = getTable(table);
        String xpath = rowTag + "[" + targetElementRow + "]/" + colTag + "[" + targetElementCol + "]";
        WebElement targetElementCell = eleTable.findElement(By.xpath(xpath));

        //Find the element based on type
        WebElement targetElement = null;
        switch (typeOfElement.trim().toUpperCase()) {
            case "DROPDOWN":
            case "LISTBOX":
                targetElement = targetElementCell.findElements(By.tagName("MAT-SELECT")).get(elementIndex).findElement(By.xpath(".."));
                break;
            case "LINK":
                targetElement = targetElementCell.findElements(By.tagName("A")).get(elementIndex);
                break;
            case "BUTTON":
                targetElement = targetElementCell.findElements(By.tagName("BUTTON")).get(elementIndex);
                break;
            case "SPAN":
                targetElement = targetElementCell.findElements(By.tagName("SPAN")).get(elementIndex);
                break;
            case "CHECKBOX":
            case "RADIO":
                targetElement = targetElementCell.findElements(By.tagName("INPUT")).get(elementIndex);
                break;
            case "MAT-ICON":
                targetElement = targetElementCell.findElements(By.tagName("MAT-ICON")).get(elementIndex);
            case "IMAGE":
                targetElement = targetElementCell.findElements(By.cssSelector("fox-member-demographics > fox-member-demographics-designees > fox-section > div > div.member-designee-table-main > div > mat-table > mat-row > mat-cell.mat-cell.cdk-column-roles.mat-column-roles.ng-star-inserted > div > svg")).get(elementIndex);
        }

        //return
        return table.newFluent(targetElement);
    }

    /**
     * @param table                 - FluentWebElement table element to consider
     * @param columnValuesToFindRow - column name-value pairs to match a required row
     * @param elementColumn         - column name where target element is available
     * @param typeOfElement         - type of element looking for ex: DROPDOWN, LINK
     * @return requiredElement present inside the table to operate
     * @throws Throwable
     */
    public static FluentWebElement getElementInTable(FluentWebElement table, String columnValuesToFindRow,
                                                     String elementColumn, String typeOfElement) throws Throwable {
        //Get the matching row
        int targetElementRow = findTableRow(table, columnValuesToFindRow);

        //Get the element
        return getElementInsideTable(table, targetElementRow, elementColumn, typeOfElement, 0);
    }

    public static FluentWebElement getElementInTable(FluentWebElement table, String columnValuesToFindRow,
                                                     String elementColumn, String typeOfElement, int elementIndex) throws Throwable {
        //Get the matching row
        int targetElementRow = findTableRow(table, columnValuesToFindRow);

        //Get the element
        return getElementInsideTable(table, targetElementRow, elementColumn, typeOfElement, elementIndex);
    }

    /**
     * @param table         - FluentWebElement table element to consider
     * @param rowIndex      - required row number starting from 0
     * @param elementColumn - column name where target element is available
     * @param typeOfElement - type of element looking for ex: DROPDOWN, LINK
     * @return requiredElement present inside the table to operate
     * @throws Throwable
     */
    public static FluentWebElement getElementInTable(FluentWebElement table, int rowIndex,
                                                     String elementColumn, String typeOfElement) throws Throwable {
        //Get the element
        return getElementInsideTable(table, rowIndex, elementColumn, typeOfElement, 0);
    }

    public static FluentWebElement getElementInTable(FluentWebElement table, int rowIndex,
                                                     String elementColumn, String typeOfElement, int elementIndex) throws Throwable {
        //Get the element
        return getElementInsideTable(table, rowIndex, elementColumn, typeOfElement, elementIndex);
    }

    /**
     * @param table
     * @param valueToFind - identifier
     * @param colToFind   -columen where you want to search for your identifier (separated by coma)
     * @param colsToGet   - columns to get per specified identifier  (separated coma)
     * @param elementType - specific element types (separated by coma)
     */
    public Map<String, Map<String, WebElement>> getElementOnATable(FluentWebElement table, String valueToFind, int colToFind, Map<Integer, String> colsToGet, String elementType) throws Throwable {

        WebElement eTable = getTable(table);
        List<WebElement> trList = eTable.findElements(By.xpath(rowTag));
        Map<String, Map<String, WebElement>> requiredRows = new TreeMap<>();
        String[] elType = null;
        if (elementType != null) {
            elType = elementType.split(",");
        } else {
            elementType = "any";
            elType = elementType.split(",");
        }

        Map<String, WebElement> requiredTdList = new TreeMap<>();
        for (WebElement tr : trList) {
            List<WebElement> tdList = tr.findElements(By.xpath("td"));
            int cnt = 0;
            for (int c : colsToGet.keySet()) {
                if (c >= 0 && c < tdList.size()) {
                    if (elType.length > 0) {
                        WebElement tdEl = null;
                        if (elType[cnt].equals("checkbox")) {
                            tdEl = tdList.get(c).findElement(By.xpath("//input[@type='checkbox']"));
                        } else if (elType[cnt].equals("link")) {
                            tdEl = tdList.get(c).findElement(By.xpath("a"));
                        } else if (elType[cnt].equals("button")) {
                            tdEl = tdList.get(c).findElement(By.xpath("//button"));
                        } else if (elType[cnt].equals("radiobutton")) {
                            tdEl = tdList.get(c).findElement(By.xpath("//input[@type='radio']"));
                        } else {
                            tdEl = tdList.get(c);
                        }

                        String k = colsToGet.get(c);
                        requiredTdList.put(k.trim(), tdEl);
                    }
                }
                String keyValue = tdList.get(colToFind).getText();
                String keyValueNoSpace = keyValue.replaceAll("\\s+", ""); //Removes the spaces in between the String
                if (keyValueNoSpace.equals("")) {
                    keyValueNoSpace = tdList.get(colToFind).getAttribute("value");
                }
                requiredRows.put(keyValueNoSpace.trim(), requiredTdList);
            }

        }


        String[] key = valueToFind.split(",");
        Map<String, Map<String, WebElement>> selectedList = new TreeMap<>();
        for (String c : key) {
            Map<String, WebElement> webElements = requiredRows.get(c);
            if (webElements != null) {
                selectedList.put(c, webElements);
            } else continue;
        }

        return selectedList;
    }

    public Map<String, WebElement> getElementsInTable(FluentWebElement table, String tagToGet, int columnIndexToGet, String tagToFind, int columnIndexToFind, String valueToFind) throws Throwable {
        WebElement eTable = getTable(table);
        List<WebElement> trList = eTable.findElements(By.xpath(rowTag));
        String[] valArr = valueToFind.trim().split(",");
        Map<String, WebElement> requiredElements = new TreeMap<>();
        for (String val : valArr) {
            for (WebElement tr : trList) {
                List<WebElement> tdList = tr.findElements(By.xpath("td"));
                WebElement tdToFind = tdList.get(columnIndexToFind);
                WebElement targetToFind;
                if (tagToFind == null) {
                    targetToFind = tdToFind;
                } else {
                    targetToFind = tdToFind.findElement(By.tagName(tagToFind));
                }

                if (val.equals(targetToFind.getText())) {
                    WebElement tdToGet = tdList.get(columnIndexToGet);
                    if (tagToGet == null) {
                        requiredElements.put(val, tdToGet);
                    } else {
                        requiredElements.put(val, tdToGet.findElement(By.tagName(tagToGet)));
                    }
                    break;
                }
            }
        }
        return requiredElements;
    }

    public static Map<String, Integer> getTableColumnIndexesUpdate(FluentWebElement table) throws Throwable {
        Map<String, Integer> indexes = new HashMap<>();

        //Wait for the element
        table.await().atMost(15, TimeUnit.SECONDS).until(table).clickable();

        //Get the table element
        WebElement eTable = null;

        eTable = table.getElement();


        //Get column header row
        String columnName = "";
        List<WebElement> headers = eTable.findElements(By.xpath("thead/tr/th"));
        ListIterator<WebElement> it = headers.listIterator();
        while (it.hasNext()) {
            columnName = it.next().getText().trim().toUpperCase();
            indexes.put(columnName, it.nextIndex() - 1);
        }

        //Return
        return indexes;
    }

    /**
     * This will get the mentioned column values for all the rows in the table element of Fox-Ui application
     *
     * @param table          - table element
     * @param columnsToFetch - to fetch the values of that colum for all rows
     */
    public static ArrayList<ArrayList<String>> getTableColumnValuesUpdated(FluentWebElement table, String columnsToFetch) throws Throwable {
        Map<String, Integer> indexes = new HashMap<>();
        String[] columns, values;
        ArrayList<ArrayList<String>> rowsFetched = new ArrayList<ArrayList<String>>();

        //Get the table element
        WebElement eTable = null;
        if (table.tagName().trim().equalsIgnoreCase("table")) {
            eTable = table.getElement().findElement(By.xpath("."));
        } else {
            eTable = table.getElement();
        }

        //Get the indexes
        indexes = getTableColumnIndexesUpdate(table);

        //Get each row and search
        List<WebElement> rows = eTable.findElements(By.xpath("tbody/tr"));
        ListIterator<WebElement> it = rows.listIterator();
        List<WebElement> cols = null;
        while (it.hasNext()) {
            //Reset match
            ArrayList<String> columnValuesFetched = new ArrayList<String>();

            //Get all columns for all rows
            cols = it.next().findElements(By.xpath("td"));

            //See if we got the matching row
            if (columnsToFetch == null) { //Fetch all columns
                for (int c = 0; c < cols.size(); c++) {
                    columnValuesFetched.add(cols.get(c).getText().trim());
                }
            } else { //Fetch for only required columns
                String[] colsToFetch = columnsToFetch.trim().split(";");
                for (int c = 0; c < colsToFetch.length; c++) {
                    columnValuesFetched.add(cols.get(indexes.get(colsToFetch[c].toUpperCase())).getText().trim());
                }
            }

            //Add to master collection
            if (!columnValuesFetched.isEmpty())
                rowsFetched.add(columnValuesFetched);

        }

        //Return
        return rowsFetched;
    }

    /**
     * This will get the mentioned column header values for all the rows in the table element of Fox-Ui application
     *
     * @param table Mat Table Headers
     */
    public static Map<String, Integer> getMatTableColumnIndexesUpdated(FluentWebElement table) throws Throwable {
        Map<String, Integer> indexes = new HashMap<>();

        //Wait for the element
        table.await().atMost(15, TimeUnit.SECONDS).until(table).clickable();

        //Get the table element
        WebElement eTable = null;

        eTable = table.getElement();


        //Get column header row
        String columnName = "";
        List<WebElement> headers = eTable.findElements(By.xpath("mat-header-row/mat-header-cell"));
        ListIterator<WebElement> it = headers.listIterator();
        while (it.hasNext()) {
            columnName = it.next().getText().trim().toUpperCase();
            indexes.put(columnName, it.nextIndex() - 1);
        }

        //Return
        return indexes;
    }

    public static int getTableRowsCount(FluentWebElement table) throws Throwable {
        //Get the table element
        WebElement eTable = getTable(table);

        //Return
        return eTable.findElements(By.xpath(rowTag)).size();
    }

    //-----DUPLICATES----

    /**
     * This will get column indexes for a table element of Fox-Ui application
     *
     * @param table - table element
     */
    public static Map<String, Integer> getTableColumnIndexes_temp(FluentWebElement table) throws Throwable {

        Map<String, Integer> indexes = new HashMap<>();

        //Get the table element
        WebElement eTable = getTable(table);

        //Get the table headers based on type
        List<WebElement> headers = null;
        headers = eTable.findElements(By.xpath(headerRowTag + "/" + headerColTag));

        //Get column header row
        String columnName = "";
        ListIterator<WebElement> it = headers.listIterator();
        while (it.hasNext()) {
            WebElement tableHeader = it.next();
            columnName = tableHeader.getText().trim().toUpperCase().replace("\n", " ");
            if (columnName.trim().isEmpty()) {
                columnName = tableHeader.getAttribute("textContent").toUpperCase();
            }
            if (columnName.contains("SORTED BY"))
                columnName = columnName.substring(0, columnName.indexOf("SORTED BY")).trim();

            indexes.put(columnName, it.nextIndex() - 1);
        }
        //Return
        return indexes;
    }

    /**
     * This will find a row in the table element of Fox-Ui application
     *
     * @param table                 - table element
     * @param columnValuesToFindRow - column values to match the row ex. COL1=VAL1;COL2=VAL2
     */
    public static int findTableRow_temp(FluentWebElement table, String columnValuesToFindRow) throws Throwable {
        Map<String, Integer> indexes = new HashMap<>();
        String[] columns, values;

        //Split the columns and values
        String[] temp = columnValuesToFindRow.split(";");
        columns = new String[temp.length];
        values = new String[temp.length];
        for (int t = 0; t < temp.length; t++) {
            columns[t] = temp[t].split("=")[0].trim();
            values[t] = temp[t].split("=")[1].trim();
        }

        //Get the table element
        WebElement eTable = getTable(table);

        //Get the indexes
        indexes = getTableColumnIndexes_temp(table);  //TODO - pass onlyRequiredColumns

        //Get each row and search
        List<WebElement> rows = null;
        rows = eTable.findElements(By.xpath(rowTag));
        ListIterator<WebElement> it = rows.listIterator();
        List<WebElement> cols = null;
        WebElement currCol = null;
        String currColValue = "";
        int match = 0;
        while (it.hasNext()) {
            //Reset match
            match = 0;

            //Get all columns for current row
            cols = it.next().findElements(By.xpath(colTag));

            //For each column, check the value
            for (int c = 0; c < columns.length; c++) {
                //Get current column UI value
                currColValue = cols.get(indexes.get(columns[c].toUpperCase())).getText().trim();

                if (NumberUtils.isNumber(currColValue) && NumberUtils.isNumber(values[c].trim())) { //if number
                    try {
                        if (Integer.parseInt(currColValue) == Integer.parseInt(values[c].trim())) {
                            match += 1;
                        }
                    } catch (Exception e) {
                        if (Long.parseLong(currColValue) == Long.parseLong(values[c].trim())) {
                            match += 1;
                        }
                    }

                } else {
                    if (values[c].trim().endsWith(".*")) {
                        values[c] = values[c].substring(0, values[c].indexOf(".*"));
                        if (currColValue.trim().toUpperCase().contains(values[c].trim().toUpperCase())) {
                            match += 1;
                        }
                    } else if (currColValue.trim().equalsIgnoreCase(values[c].trim())) {
                        match += 1;
                    }
                }
            }

            //See if we got the matching row
            if (match == columns.length)
                return it.nextIndex() - 1;
        }

        //Return
        return -1;
    }

    private static ArrayList<String> collectARowValues_temp(FluentWebElement table, String columnsToFetch, List<WebElement> allColumns) throws Throwable {
        ArrayList<String> columnValuesFetched = new ArrayList<String>();

        //Get the indexes
        Map<String, Integer> indexes = getTableColumnIndexes_temp(table);

        //Collect values
        if (columnsToFetch == null) { //Fetch all columns
            for (int c = 0; c < allColumns.size(); c++) {
                String colValue = allColumns.get(c).getText().trim();
                if (colValue.trim().equals("")) {
                    colValue = allColumns.get(c).getAttribute("innerText").trim();
                }
                if (colValue.contains("$")) {
                    colValue = colValue.replace("$", "");
                }
                columnValuesFetched.add(colValue);

            }

        } else { //Fetch for only required columns
            String[] colsToFetch = columnsToFetch.trim().split(";");
            WebElement el = null;
            for (int c = 0; c < colsToFetch.length; c++) {
                el = allColumns.get(indexes.get(colsToFetch[c].toUpperCase()));
                String colValue = el.getText().trim();
                if (colValue.trim().equals("")) {
                    colValue = el.getAttribute("innerText").trim();
                }
                if (colValue.contains("$")) {
                    colValue = colValue.replace("$", "");
                }
                columnValuesFetched.add(colValue);
            }
        }

        return columnValuesFetched;
    }

    /**
     * This will get the mentioned column values for the required row in the table element of Fox-Ui application
     *
     * @param table                 - table element
     * @param columnValuesToFindRow - column values to match the row ex. COL1=VAL1;COL2=VAL2
     * @param columnsToFetch        - column names to fetch the values ex. COL3;COL4 or COL5
     */
    public static List<String> getTableRow(FluentWebElement table, String columnValuesToFindRow, String columnsToFetch) throws Throwable {
        Map<String, Integer> indexes = new HashMap<>();
        String[] columns, values;
        List<String> columnValuesFetched = new ArrayList<String>();
        //Split the columns and values
        String[] temp = columnValuesToFindRow.split(";");
        columns = new String[temp.length];
        values = new String[temp.length];

        for (int t = 0; t < temp.length; t++) {
            columns[t] = temp[t].split("=")[0].trim();
            values[t] = temp[t].split("=")[1].trim();
        }


        //Get the table element
        WebElement eTable = getTable(table);

        //Get the indexes

        indexes = getTableColumnIndexes(table);
        if (indexes.size() < 1) {
            indexes = getTableColumnIndexes_temp(table);
            if (indexes.size() < 1) {
                throw new Exception("Cannot fetch column index from table.");
            }
        }


        List<WebElement> rows = eTable.findElements(By.xpath(rowTag));
        ListIterator<WebElement> it = rows.listIterator();
        List<WebElement> cols = null;
        WebElement currCol = null;
        String currColValue = "";
        int match = 0;
        while (it.hasNext()) {
            //Reset match
            match = 0;

            //Get all columns for current row
            cols = it.next().findElements(By.xpath(colTag));

            //For each column, check the value
            for (int c = 0; c < columns.length; c++) {
                //Get current column UI value
                currColValue = cols.get(indexes.get(columns[c].toUpperCase())).getAttribute("innerText").trim();

                if (NumberUtils.isNumber(currColValue) && NumberUtils.isNumber(values[c].trim())) { //if number

                    try {
                        int tempColValue = 0, tempValueActual = 0;
                        tempColValue = Integer.parseInt(currColValue);
                        tempValueActual = Integer.parseInt(values[c].trim());

                        if (tempColValue == tempValueActual) {
                            match += 1;
                        }
                    } catch (NumberFormatException e) {
                        long tempColValue1 = 0, tempValueActual1 = 0;
                        tempColValue1 = Long.parseLong(currColValue);
                        tempValueActual1 = Long.parseLong(values[c].trim());

                        if (tempColValue1 == tempValueActual1) {
                            match += 1;
                        }
                    }

                } else {
                    if (values[c].substring(0, 1).equalsIgnoreCase("*")) {
                        values[c] = values[c].substring(1);
                        if (currColValue.toLowerCase().contains(values[c].trim().toLowerCase())) {
                            match += 1;
                        }
                    } else {
                        if (currColValue.trim().equalsIgnoreCase(values[c].trim())) {
                            match += 1;
                        }
                    }
                }
            }

            //See if we got the matching row
            if (match == columns.length) {
                columnValuesFetched = collectARowValues_temp(table, columnsToFetch, cols);
                return columnValuesFetched;
            }
        }

        //Return
        return columnValuesFetched;
    }

    public static List<String> getTableRow(FluentWebElement table, int rowInstance, String columnsToFetch) throws Throwable {

        String[] columns, values;
        List<String> columnValuesFetched = new ArrayList<String>();

        //Get the table element
        WebElement eTable = getTable(table);

        //Get required row
        List<WebElement> rows = eTable.findElements(By.xpath(rowTag));

        //Get all columns for current row
        List<WebElement> cols = rows.get(rowInstance).findElements(By.xpath(colTag));

        //Collect values
        columnValuesFetched = collectARowValues_temp(table, columnsToFetch, cols);

        //Return
        return columnValuesFetched;
    }

    /**
     * This will get all the column values for all the rows in the table element of Fox-Ui application
     *
     * @param table                 - table element
     * @param columnValuesToFindRow - column values to match the row ex. COL1=VAL1;COL2=VAL2
     */
    public static ArrayList<ArrayList<String>> getTableRows_temp(FluentWebElement table, String columnValuesToFindRow) throws Throwable {
        return getTableRows_temp(table, columnValuesToFindRow, null);
    }

    /**
     * @param table - table element
     * @return list of rows with list of column values
     * @throws Throwable
     */
    public static ArrayList<ArrayList<String>> getTableRows_temp(FluentWebElement table) throws Throwable {
        return getTableRows_temp(table, null, null);
    }

    /**
     * @param table            - FluentWebElement table element to consider
     * @param targetElementRow - required row number starting from 0
     * @param elementColumn    - column name where target element is available
     * @param typeOfElement    - type of element looking for ex: DROPDOWN, LINK
     * @return requiredElement present inside the table to operate
     * @throws Throwable
     */
    private static FluentWebElement getElementInsideTable_temp(FluentWebElement table, int targetElementRow,
                                                               String elementColumn, String typeOfElement, int elementIndex) throws Throwable {
        //Get the required row number
        targetElementRow = targetElementRow + 1;

        //Get the target element column index
        elementColumn = elementColumn.trim().toUpperCase();
        if ("space".equalsIgnoreCase(elementColumn))
            elementColumn = "";
        int targetElementCol = getTableColumnIndexes_temp(table).get(elementColumn);
        targetElementCol = targetElementCol + 1;

        //Find the target element cell
        WebElement eleTable = getTable(table);
        String xpath = rowTag + "[" + targetElementRow + "]/" + colTag + "[" + targetElementCol + "]";
        WebElement targetElementCell = eleTable.findElement(By.xpath(xpath));

        //Find the element based on type
        WebElement targetElement = null;
        switch (typeOfElement.trim().toUpperCase()) {
            case "DROPDOWN":
            case "LISTBOX":
                targetElement = targetElementCell.findElements(By.tagName("MAT-SELECT")).get(elementIndex).findElement(By.xpath(".."));
                break;
            case "LINK":
                targetElement = targetElementCell.findElements(By.tagName("A")).get(elementIndex);
                break;
            case "BUTTON":
                targetElement = targetElementCell.findElements(By.tagName("BUTTON")).get(elementIndex);
                break;
            case "SPAN":
                targetElement = targetElementCell.findElements(By.tagName("SPAN")).get(elementIndex);
                break;
            case "CHECKBOX":
                targetElement = targetElementCell.findElements(By.tagName("INPUT")).get(elementIndex);
                break;
            case "MAT-ICON":
                targetElement = targetElementCell.findElements(By.tagName("MAT-ICON")).get(elementIndex);
            case "IMAGE":
                targetElement = targetElementCell.findElements(By.tagName("IMG")).get(elementIndex);
        }

        //return
        return table.newFluent(targetElement);
    }

    private static List<WebElement> getElementsInsideTable(FluentWebElement table, int targetElementRow,
                                                           String elementColumn, String typeOfElement, int elementIndex) throws Throwable {
        //Get the required row number
        targetElementRow = targetElementRow + 1;

        //Get the target element column index
        elementColumn = elementColumn.trim().toUpperCase();
        if ("space".equalsIgnoreCase(elementColumn))
            elementColumn = "";
        int targetElementCol = getTableColumnIndexes_temp(table).get(elementColumn);
        targetElementCol = targetElementCol + 1;

        //Find the target element cell
        WebElement eleTable = getTable(table);
        String xpath = rowTag + "[" + targetElementRow + "]/" + colTag + "[" + targetElementCol + "]";
        WebElement targetElementCell = eleTable.findElement(By.xpath(xpath));

        //Find the element based on type
        List<WebElement> targetElement = null;
        switch (typeOfElement.trim().toUpperCase()) {
            case "DROPDOWN":
            case "LISTBOX":
                targetElement = targetElementCell.findElements(By.tagName("MAT-SELECT"));
                break;
            case "LINK":
                targetElement = targetElementCell.findElements(By.tagName("A"));
                break;
            case "BUTTON":
                targetElement = targetElementCell.findElements(By.tagName("BUTTON"));
                break;
            case "SPAN":
                targetElement = targetElementCell.findElements(By.tagName("SPAN"));
                break;
            case "CHECKBOX":
                targetElement = targetElementCell.findElements(By.tagName("INPUT"));
                break;
            case "MAT-ICON":
                targetElement = targetElementCell.findElements(By.tagName("MAT-ICON"));
        }

        //return
        return targetElement;
    }

    /**
     * @param table                 - FluentWebElement table element to consider
     * @param columnValuesToFindRow - column name-value pairs to match a required row
     * @param elementColumn         - column name where target element is available
     * @param typeOfElement         - type of element looking for ex: DROPDOWN, LINK
     * @return requiredElement present inside the table to operate
     * @throws Throwable
     */
    public static FluentWebElement getElementInTable_temp(FluentWebElement table, String columnValuesToFindRow,
                                                          String elementColumn, String typeOfElement) throws Throwable {
        //Get the matching row
        int targetElementRow = findTableRow_temp(table, columnValuesToFindRow);

        //Get the element
        return getElementInsideTable_temp(table, targetElementRow, elementColumn, typeOfElement, 0);
    }

    public static FluentWebElement getElementInTable_temp(FluentWebElement table, String columnValuesToFindRow,
                                                          String elementColumn, String typeOfElement, int elementIndex) throws Throwable {
        //Get the matching row
        int targetElementRow = findTableRow_temp(table, columnValuesToFindRow);

        //Get the element
        return getElementInsideTable_temp(table, targetElementRow, elementColumn, typeOfElement, elementIndex);
    }

    /**
     * @param table         - FluentWebElement table element to consider
     * @param rowIndex      - required row number starting from 0
     * @param elementColumn - column name where target element is available
     * @param typeOfElement - type of element looking for ex: DROPDOWN, LINK
     * @return requiredElement present inside the table to operate
     * @throws Throwable
     */
    public static FluentWebElement getElementInTable_temp(FluentWebElement table, int rowIndex,
                                                          String elementColumn, String typeOfElement) throws Throwable {
        //Get the element
        return getElementInsideTable_temp(table, rowIndex, elementColumn, typeOfElement, 0);
    }

    public static List<WebElement> getElementsInTable(FluentWebElement table, int rowIndex,
                                                      String elementColumn, String typeOfElement) throws Throwable {
        //Get the element
        return getElementsInsideTable(table, rowIndex, elementColumn, typeOfElement, 0);
    }

    public static FluentWebElement getElementInTable_temp(FluentWebElement table, int rowIndex,
                                                          String elementColumn, String typeOfElement, int elementIndex) throws Throwable {
        //Get the element
        return getElementInsideTable_temp(table, rowIndex, elementColumn, typeOfElement, elementIndex);
    }

    public ArrayList<ArrayList<String>> returnEobDrugSummaryTableResults(FluentWebElement sortedTableName, String columnToFetch) throws Throwable {

        UIUtil UIUtil = new UIUtil();
        return ui.foxPages.common.UIUtil.getTableColumnValues(sortedTableName, columnToFetch);
    }

    /**
     * @param table
     * @param valueToFind - identifier
     * @param colToFind   -columen where you want to search for your identifier (separated by coma)
     * @param colsToGet   - columns to get per specified identifier  (separated coma)
     * @param elementType - specific element types (separated by coma)
     */
    public Map<String, List<WebElement>> getElementOnRefTable(FluentWebElement table, String valueToFind, int colToFind, int[] colsToGet, String elementType) throws Throwable {

        WebElement eTable = getTable(table);
        List<WebElement> trList = eTable.findElements(By.xpath(rowTag));
        Map<String, List<WebElement>> requiredRows = new TreeMap<>();
        String[] elType = null;
        if (elementType != null) {
            elType = elementType.split(",");
        } else {
            elementType = "any";
            elType = elementType.split(",");
        }

        for (WebElement tr : trList) {
            List<WebElement> tdList = tr.findElements(By.xpath("td"));
            List<WebElement> requiredTdList = new ArrayList<>();
            int cnt = 0;
            for (int c : colsToGet) {
                if (c >= 0 && c < tdList.size()) {
                    if (elType.length > 0) {
                        if (elType[cnt].equals("checkbox")) {
                            WebElement tdEl = tdList.get(c).findElement(By.xpath("//input[@type='checkbox']"));
                            requiredTdList.add(tdEl);
                        } else if (elType[cnt].equals("link")) {
                            WebElement tdEl = tdList.get(c).findElement(By.xpath("a"));
                            requiredTdList.add(tdEl);
                        } else if (elType[cnt].equals("button")) {
                            WebElement tdEl = tdList.get(c).findElement(By.xpath("//button"));
                            requiredTdList.add(tdEl);
                        } else if (elType[cnt].equals("radiobutton")) {
                            WebElement tdEl = tdList.get(c).findElement(By.xpath("//input[@type='radio']"));
                            requiredTdList.add(tdEl);
                        } else if (elType[cnt].equals("icon")) {
                            WebElement tdEl = tdList.get(c).findElement(By.xpath("//span[contains(.,'" + valueToFind + "')]/preceding-sibling::mat-icon[@role='img']"));
                            requiredTdList.add(tdEl);
                        } else {
                            WebElement tdEl = tdList.get(c);
                            requiredTdList.add(tdEl);
                        }
                        if (elType.length > 0) {
                            c++;
                        }
                    }
                }
                String keyValue = tdList.get(colToFind).getText();
                if (keyValue.equals("")) {
                    keyValue = tdList.get(colToFind).getAttribute("value");
                }
                requiredRows.put(keyValue.trim(), requiredTdList);
            }

        }

        String[] key = valueToFind.split(",");
        Map<String, List<WebElement>> selectedList = new TreeMap<>();
        for (String c : key) {
            List<WebElement> webElements = requiredRows.get(c);
            if (webElements != null) {
                selectedList.put(c, webElements);
            } else continue;
        }

        return selectedList;
    }

    /**
     * @param table
     * @param valueToFind - identifier
     * @param colToFind   -columen where you want to search for your identifier (separated by coma)
     * @param colsToGet   - columns to get per specified identifier  (separated coma)
     * @param elementType - specific element types (separated by coma)
     */
    public Map<String, Map<String, WebElement>> getElementOnATable_temp(FluentWebElement table, String valueToFind, int colToFind, Map<Integer, String> colsToGet, String elementType) throws Throwable {

        WebElement eTable = getTable(table);
        List<WebElement> trList = eTable.findElements(By.xpath(rowTag));
        Map<String, Map<String, WebElement>> requiredRows = new TreeMap<>();
        String[] elType = null;
        if (elementType != null) {
            elType = elementType.split(",");
        } else {
            elementType = "any";
            elType = elementType.split(",");
        }

        Map<String, WebElement> requiredTdList = new TreeMap<>();
        for (WebElement tr : trList) {
            List<WebElement> tdList = tr.findElements(By.xpath("td"));
            int cnt = 0;
            for (int c : colsToGet.keySet()) {
                if (c >= 0 && c < tdList.size()) {
                    if (elType.length > 0) {
                        WebElement tdEl = null;
                        if (elType[cnt].equals("checkbox")) {
                            tdEl = tdList.get(c).findElement(By.xpath("//input[@type='checkbox']"));
                        } else if (elType[cnt].equals("link")) {
                            tdEl = tdList.get(c).findElement(By.xpath("a"));
                        } else if (elType[cnt].equals("button")) {
                            tdEl = tdList.get(c).findElement(By.xpath("//button"));
                        } else if (elType[cnt].equals("radiobutton")) {
                            tdEl = tdList.get(c).findElement(By.xpath("//input[@type='radio']"));
                        } else {
                            tdEl = tdList.get(c);
                        }

                        String k = colsToGet.get(c);
                        requiredTdList.put(k.trim(), tdEl);
                    }
                }
                String keyValue = tdList.get(colToFind).getText();
                String keyValueNoSpace = keyValue.replaceAll("\\s+", ""); //Removes the spaces in between the String
                if (keyValueNoSpace.equals("")) {
                    keyValueNoSpace = tdList.get(colToFind).getAttribute("value");
                }
                requiredRows.put(keyValueNoSpace.trim(), requiredTdList);
            }

        }


        String[] key = valueToFind.split(",");
        Map<String, Map<String, WebElement>> selectedList = new TreeMap<>();
        for (String c : key) {
            Map<String, WebElement> webElements = requiredRows.get(c);
            if (webElements != null) {
                selectedList.put(c, webElements);
            } else continue;
        }

        return selectedList;
    }

    public static ArrayList<ArrayList<String>> getMatTableColumnValuesUpdatedForSingleRow(FluentWebElement table, String columnsToFetch, String rowToFetch) throws Throwable {
        Map<String, Integer> indexes = new HashMap<>();
        String[] columns, values;
        ArrayList<ArrayList<String>> rowsFetched = new ArrayList<ArrayList<String>>();

        //Get the table element
        WebElement eTable = null;
        if (table.tagName().trim().equalsIgnoreCase("mat-table")) {
            eTable = table.getElement().findElement(By.xpath("."));
        } else {
            eTable = table.getElement();
        }

        //Get the indexes
        indexes = getMatTableColumnIndexesUpdated(table);

        //Get each row and search
        List<WebElement> rows = eTable.findElements(By.xpath("mat-row"));
        ListIterator<WebElement> it = rows.listIterator();
        List<WebElement> cols = null;
        while (it.hasNext()) {
            //Reset match
            ArrayList<String> columnValueFetched = new ArrayList<String>();

            //Get all columns for all rows
            cols = it.next().findElements(By.xpath("mat-cell"));

            //See if we got the matching row
            if (columnsToFetch == null) { //Fetch all columns
                for (int c = 0; c < cols.size(); c++) {
                    columnValueFetched.add(cols.get(c).getText().trim());
                }
            } else { //Fetch for only required columns
                String[] colsToFetch = columnsToFetch.trim().split(";");
                for (int c = 0; c < colsToFetch.length; c++) {

                    int x = indexes.get(colsToFetch[c].toUpperCase());
                    String val = cols.get(x).getText().trim();
                    columnValueFetched.add(val);

                }
            }

            //Add to master collection
            if (!columnValueFetched.isEmpty()) {
                rowsFetched.add(columnValueFetched);
            }

        }

        //Return
        return rowsFetched;
    }

    /**
     * @param table
     * @param valueToFind - values you need to find on specified column(delimited by ; for multiple criteria)
     * @param colToFind   - single column where you want to find values specified
     * @param colsToGet   -   all will be pull else only specific column will be selected
     * @param elementType - <optional> if null, all will be pull else only specific element will be selected
     * @return
     * @throws Throwable
     */
    public Map<String, Map<String, List<WebElement>>> getTableElements(FluentWebElement table, String colToFind, String valueToFind, String colsToGet, String elementType) throws Throwable {

        WebElement eTable = getTable(table);
        List<WebElement> trList = eTable.findElements(By.xpath(rowTag));
        Map<String, Map<String, List<WebElement>>> requiredRows = new TreeMap<>();
        String[] elType = null;
        if (elementType != null) {
            elType = elementType.split(";");
        } else {
            elementType = "any";
            elType = elementType.split(";");
        }
        Map<String, Integer> columnIndex = getTableColumnIndexes_temp(table);

        Map<Integer, String> FinalColsToget = new TreeMap<>();

        //getting columns to get (can be index already or column name)
        String[] columns = null;
        if (colsToGet != null) {
            columns = colsToGet.split(";");
            for (String col : columns) {
                if (col.matches("\\d"))
                    FinalColsToget.put(Integer.valueOf(col), col);
                else
                    FinalColsToget.put(columnIndex.get(col.trim().toUpperCase()), col);
            }
        } else {
            for (String col : columnIndex.keySet()) {
                if (col.matches("\\d"))
                    FinalColsToget.put(Integer.valueOf(col), col);
                else
                    FinalColsToget.put(columnIndex.get(col.trim().toUpperCase()), col);
            }
        }
        //search criteria on the table (values to find on the identified column
        String[] values = valueToFind.split(";");
        Map<String, String> valuesToFind = new TreeMap<>();

        for (int c = 0; c < values.length; c++) {
            valuesToFind.put(values[c], values[c]);
        }
        //getting index of the column to be used fo find the criteria values
        Integer coltoFindIndex = null;

        if (colToFind.matches("\\d"))
            coltoFindIndex = Integer.valueOf(colToFind);
        else
            coltoFindIndex = columnIndex.get(colToFind.trim().toUpperCase());

        int trCnt = 0;
        //start code to get elements in the table
        for (WebElement tr : trList) {
            Map<String, List<WebElement>> requiredTdList = new TreeMap<>();
            List<WebElement> tdList = tr.findElements(By.xpath("td"));
            List<WebElement> tdL = trList.get(0).findElements(By.xpath("td"));
            int cnt = 0;
            int tdCnt = 0;
            //getting actual value on the column to find
            String actualColValue = tdList.get(coltoFindIndex).getText();
            if (actualColValue.equals("")) {
                actualColValue = tdList.get(coltoFindIndex).getAttribute("value");
            }
            //validate if actual value is existing on list of values to find, if existing then will get values from the row on selected values
            if (valuesToFind.containsKey(actualColValue) || valuesToFind.containsKey("NA")) {
                for (int c : FinalColsToget.keySet()) {
                    List<WebElement> tdEl = null;
                    if (c >= 0 && c < tdList.size()) {
                        if (elType.length > 0) {
                            trCnt++;
                            tdCnt = c + 1;
                            if (elType[cnt].equals("checkbox")) {
                                tdEl = eTable.findElements(By.xpath("//tr[" + trCnt + "]//td[" + tdCnt + "]//input[@type='checkbox']"));
                            } else if (elType[cnt].equals("link")) {
                                tdEl = eTable.findElements(By.xpath("//tr[" + trCnt + "]//td[" + tdCnt + "]//a"));
                            } else if (elType[cnt].equals("button")) {
                                tdEl = eTable.findElements(By.xpath("//tr[" + trCnt + "]//td[" + tdCnt + "]//button"));
                            } else if (elType[cnt].equals("radiobutton")) {
                                tdEl = eTable.findElements(By.xpath("//tr[" + trCnt + "]//td[" + tdCnt + "]//input[@type='radio']"));
                            } else if (elType[cnt].equals("text")) {
                                tdEl = eTable.findElements(By.xpath("//tr[" + trCnt + "]//td[" + tdCnt + "]"));
                            } else {
                                tdEl = eTable.findElements(By.xpath("//tr[" + trCnt + "]//td[" + tdCnt + "]//."));
                            }
                            //adding column name and element

                            requiredTdList.put(FinalColsToget.get(c).trim(), tdEl);
                        }
                    }
                }
                requiredRows.put(actualColValue, requiredTdList);

            }
        }

        return requiredRows;
    }


    //------------------------ LABELS  -----------------------------------

    /**
     * @param detailsSection           fluent web element for the page section where all the label fields are displayed
     * @param fieldNamesToGetValuesFor labels as in the UI separated by ;
     * @return list with values in the same order of field names passed
     * @throws Throwable
     */
    private static List<String> getUIFieldValues(FluentWebElement detailsSection, String fieldNamesToGetValuesFor, boolean isHeader) throws Throwable {
        List<String> fieldValues = new ArrayList<String>();

        //Wait for the element
        detailsSection.await().atMost(10, TimeUnit.SECONDS).until(detailsSection).clickable();

        //Get the fields/labels to read values for
        String[] fieldNames = fieldNamesToGetValuesFor.trim().split(";");

        //Find each element and read the values
        WebElement currField = null;
        String currFieldValue = null;
        List<WebElement> textFields = null;
        for (int field = 0; field < fieldNames.length; field++) {

            //Get the element
            String currFieldLabel = (fieldNames[field].trim().contains(":")) ? fieldNames[field].trim() + " " : fieldNames[field].trim() + ": ";
            try {
                String className = "";
                if (isHeader) {
                    className = "span-header-label";
                    currFieldLabel = currFieldLabel.trim();

                } else {
                    className = "label-detail-item";
                }
                currField =
                        detailsSection.getElement().findElement(By.xpath("//span[contains(@class, '" + className + "') and text()='" + currFieldLabel + "']"));

                //Get the text elements as additional text wrapped in next line
                textFields = currField.findElements(By.xpath("../span"));

                currFieldValue = textFields.get(1).getText();
                if (textFields.size() > 2)
                    currFieldValue = currFieldValue + " " + textFields.get(2).getText();
            } catch (Exception e) {
                currField = detailsSection.getElement().findElement(By.xpath("//div[contains(@class,'container-info-label') and text()='" + currFieldLabel.trim() + "']"));
                //Get the text elements as additional text wrapped in next line
                textFields = currField.findElements(By.xpath("../div[contains(@class,'container-info-value')]"));
                currFieldValue = textFields.get(0).getText();
            }

            //Add to list
            fieldValues.add(currFieldValue);
        }

        //Return
        return fieldValues;
    }

    public static List<String> getUIFieldValues(FluentWebElement detailsSection, String fieldNamesToGetValuesFor) throws Throwable {
        return getUIFieldValues(detailsSection, fieldNamesToGetValuesFor, false);
    }

    public static List<String> getUIHeaderValues(FluentWebElement headerSection, String fieldNamesToGetValuesFor) throws Throwable {
        return getUIFieldValues(headerSection, fieldNamesToGetValuesFor, true);
    }

    /**
     * @param elements
     * @param
     * @return
     * @throws Throwable
     */
    public static List<String> getUIValues(FluentList<FluentWebElement> elements) throws Throwable {
        List<String> fieldValues = new ArrayList<String>();

        //Wait for the element
        elements.await().atMost(30, TimeUnit.SECONDS).until(elements.first()).present();

        //Find each element and read the values
        String currFieldValue = null;

        for (FluentWebElement element : elements) {
            try {
                currFieldValue = element.getElement().getText();
                if (currFieldValue.trim().length() == 0) //if value has the text
                    currFieldValue = element.getElement().getAttribute("Value");
                if (currFieldValue.trim().length() == 0) //if value has the text
                    currFieldValue = element.getElement().getAttribute("innerText");
            } catch (Exception e) {
                currFieldValue = "";
            }
            fieldValues.add(currFieldValue);
        }

        return fieldValues;
    }

    /**
     * @param detailsSection           fluent web element for the page section where all the label fields are displayed
     * @param fieldNamesToGetValuesFor labels as in the UI separated by ;
     * @return list with values in the same order of field names passed
     * @throws Throwable
     */
    public static List<String> getUIFieldValues_temp(FluentWebElement detailsSection, String fieldNamesToGetValuesFor) throws Throwable {
        List<String> fieldValues = new ArrayList<String>();

        //Wait for the element
        detailsSection.await().atMost(10, TimeUnit.SECONDS).until(detailsSection).clickable();

        //Get the fields/labels to read values for
        String[] fieldNames = fieldNamesToGetValuesFor.trim().split(";");

        //Find each element and read the values
        WebElement currField = null;
        String currFieldValue = null;
        List<WebElement> textFields = null;
        for (int field = 0; field < fieldNames.length; field++) {
            //Get the element
            try {
                String currFieldLabel = (fieldNames[field].trim().contains(":")) ? fieldNames[field].trim() + " " : fieldNames[field].trim() + ": ";
                currField =
                        detailsSection.getElement().findElement(By.xpath("//span[contains(@class,'label-detail-item') and text()='" + currFieldLabel + "']"));

                //Get the text elements as additional text wrapped in next line
                textFields = currField.findElements(By.xpath("../span"));

                currFieldValue = textFields.get(1).getText();
                if (textFields.size() > 2)
                    currFieldValue = currFieldValue + " " + textFields.get(2).getText();
            } catch (Exception e) {
                currFieldValue = "";
            }

            //Add to list
            fieldValues.add(currFieldValue);
        }

        //Return
        return fieldValues;
    }

    /**
     * @param detailsSection           fluent web element for the page section where all the input fields are displayed
     * @param fieldNamesToGetValuesFor labels as in the UI separated by ;
     * @return list with values in the same order of field names passed
     * @throws Throwable
     */
    public static List<String> getUIInputFieldValues(FluentWebElement detailsSection, String fieldNamesToGetValuesFor) throws Throwable {
        List<String> fieldValues = new ArrayList<String>();

        //Wait for the element
        detailsSection.await().atMost(10, TimeUnit.SECONDS).until(detailsSection).clickable();

        //Get the fields/labels to read values for
        String[] fieldNames = fieldNamesToGetValuesFor.trim().split(";");

        //Find each element and read the values
        WebElement currField = null;
        String currFieldValue = null;
        for (int field = 0; field < fieldNames.length; field++) {
            //Get the element
            try {
                String currFieldLabel = fieldNames[field].trim();
                try {
                    currField =
                            detailsSection.getElement().findElement(By.xpath("//mat-label[text()='" + currFieldLabel + "']"));
                    currField.isDisplayed();
                } catch (Exception e1) {
                    try {
                        currField =
                                detailsSection.getElement().findElement(By.xpath("//label[text()='" + currFieldLabel + "']"));
                        currField.isDisplayed();
                    } catch (Exception e2) {
                        String[] temp = currFieldLabel.split("–");
                        String fxpath = "";
                        for (int i = 0; i < temp.length; i++) {
                            if (!fxpath.isEmpty())
                                fxpath += " and ";

                            fxpath += "contains(text(),'" + temp[i].trim() + "')";
                        }
                        currField =
                                detailsSection.getElement().findElement(By.xpath("//label[" + fxpath + "]"));
                    }
                }

                try {
                    currField = currField.findElement(By.xpath("../../..//input"));
                    currFieldValue = currField.getText();
                } catch (NoSuchElementException ne) { //for few drop down fields values stored in mat-select
                    currField = currField.findElement(By.xpath("../../../mat-select//span/span"));
                    currFieldValue = currField.getText();
                }

                if (currFieldValue.trim().length() == 0) //if value has the text
                    currFieldValue = currField.getAttribute("value");
            } catch (Exception e) {
                currFieldValue = "";
            }

            //Add to list
            fieldValues.add(currFieldValue);
        }

        //Return
        return fieldValues;
    }

    public static LinkedHashMap<String, String> getUIInputFieldValuesInMap(FluentWebElement detailsSection,
                                                                           String fieldNamesToGetValuesFor,
                                                                           String fieldNamesForMap) throws Throwable {
        //Get the values
        List<String> fieldValues = getUIInputFieldValues(detailsSection, fieldNamesToGetValuesFor);

        //Convert to map
        LinkedHashMap<String, String> values = new LinkedHashMap<>();

        if (fieldNamesForMap.isEmpty())
            fieldNamesForMap = fieldNamesToGetValuesFor;

        int index = -1;
        for (String field : fieldNamesForMap.split(";")) {
            values.put(field.toUpperCase().trim(), fieldValues.get(++index));
        }
        return values;
    }


    //----------------------- GENERAL ELEMENTS --------------------------------

    /**
     * generic ispresent/isenabled/isclicable
     *
     * @param element               -FluentWebElement
     * @param scenarioForScreenshot - enter_temp scenario if you need screenshot
     * @throws Throwable
     * @author: janine
     */
    public boolean isPresent(FluentWebElement element, Scenario scenarioForScreenshot) throws Throwable {
        boolean ispresent = true;
        try {
            ispresent = await().atMost(30, TimeUnit.SECONDS).until(element).present();
        } catch (Exception e) {
            ispresent = false;
        }

        if (scenarioForScreenshot != null)
            ReportUtil.getIntance().takeScreenshot(getDriver(), scenarioForScreenshot);
        return ispresent;
    }

    /**
     * @param element               -FluentWebElement
     * @param scenarioForScreenshot - enter_temp scenario if you need screenshot
     * @throws Throwable
     * @author: janine
     * generic action click
     */
    public void clickElement(Object element, Scenario scenarioForScreenshot) throws Throwable {

        try {
            if (element instanceof WebElement) {
                WebDriverWait wait = new WebDriverWait(getDriver(), 20);
                wait.until(ExpectedConditions.visibilityOf((WebElement) element));
                JavascriptExecutor executor = (JavascriptExecutor) getDriver();
                executor.executeScript("arguments[0].click();", (WebElement) element);
            } else {
                await().atMost(20, TimeUnit.SECONDS).until((FluentWebElement) element).present();
                ((FluentWebElement) element).scrollIntoView();
                ((FluentWebElement) element).click();
            }
            //for screenshot
            if (scenarioForScreenshot != null)
                ReportUtil.getIntance().takeScreenshot(getDriver(), scenarioForScreenshot);
        } catch (Exception e) {
            if (scenarioForScreenshot != null)
                ReportUtil.getIntance().takeScreenshot(getDriver(), scenarioForScreenshot);
        }
    }

    public static void click(FluentWebElement element) throws Throwable {
        try {
            element.await().atMost(10, TimeUnit.SECONDS).until(element).present();
            element.scrollIntoView();
            if (element.present() && element.enabled()) {
                JavascriptExecutor executor = (JavascriptExecutor) element.getDriver();
                executor.executeScript("arguments[0].click(arguments[0].offsetWidth/2, arguments[0].offsetHeight/2);", element.getWrappedElement());
            }
        } catch (Exception e) {
            String errors = getErrorMessages();
            if (errors.isEmpty()) throw e;
            else throw new Exception(errors.toUpperCase());
        }
    }

    public boolean isEnabled(FluentWebElement element, Scenario scenarioForScreenshot) throws Throwable {

        await().atMost(20, TimeUnit.SECONDS).until(element).present();
        element.scrollIntoView();
        if (scenarioForScreenshot != null)
            ReportUtil.getIntance().takeScreenshot(getDriver(), scenarioForScreenshot);
        return await().atMost(1, TimeUnit.SECONDS).until(element).enabled();
    }

    public boolean isClickable(FluentWebElement element, Scenario scenarioForScreenshot) throws Throwable {
        await().atMost(20, TimeUnit.SECONDS).until(element).present();
        element.scrollIntoView();
        if (scenarioForScreenshot != null)
            ReportUtil.getIntance().takeScreenshot(getDriver(), scenarioForScreenshot);
        return await().atMost(1, TimeUnit.SECONDS).until(element).clickable();
    }

    /**
     * @param element               -FluentWebElement
     * @param scenarioForScreenshot - enter_temp scenario if you need screenshot
     * @throws Throwable
     * @author: janine
     * generic enter_temp
     */
    public void enter(FluentWebElement element, String enterValue, Scenario scenarioForScreenshot) throws Throwable {

        await().atMost(20, TimeUnit.SECONDS).until(element).present();
        focusElement(element);
        await().atMost(20, TimeUnit.SECONDS).until(element).enabled();
        element.fill().with(enterValue);
        if (scenarioForScreenshot != null)
            ReportUtil.getIntance().takeScreenshot(getDriver(), scenarioForScreenshot);
    }

    /**
     * @param fileName
     * @return
     * @throws Throwable
     * @Author nganji
     */
    public static String readFile(File fileName) throws Throwable {

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }

    }

    /**
     * @param element
     * @throws Throwable
     * @Author: janine
     */
    public void focusElement(Object element) throws Throwable {
        try {

            if (element instanceof WebElement) {
                WebDriverWait wait = new WebDriverWait(getDriver(), 5);
                wait.until(ExpectedConditions.visibilityOf((WebElement) element));
                JavascriptExecutor executor = (JavascriptExecutor) getDriver();
                executor.executeScript("arguments[0].scrollIntoView();", (WebElement) element);
                executor.executeScript("arguments[0].focus();", (FluentWebElement) element);
            } else {
                JavascriptExecutor executor = (JavascriptExecutor) getDriver();
                await().explicitlyFor(5, TimeUnit.SECONDS).until(((FluentWebElement) element)).present();
                executor.executeScript("arguments[0].scrollIntoView();", (FluentWebElement) element);
                executor.executeScript("arguments[0].focus();", (FluentWebElement) element);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void waitUntilFoxRotatingIconDisappear() {
        try {
            await().atMost(20, TimeUnit.SECONDS).until(el("img.img-load")).not().present();
        } catch (Exception e) {
        }
    }

    /**
     * This will get column indexes for a table element of Fox-Ui application
     *
     * @param buttonToClick - button element
     */
    public void clickButton(FluentWebElement buttonToClick) throws Exception {

        if (buttonToClick == null)
            throw new Exception("Expected Button is not present/populated on Page.");
        else {
            if (buttonToClick.enabled()) {
                buttonToClick.click();
            } else {
                logger.info("Expected Button is not enabled on Page.");
                throw new RuntimeException();
            }
        }
    }

    /**
     * @param messageTitle
     * @return
     * @throws Throwable
     * @author TCoE/nganji
     */
    public static boolean isMessageExists(String messageTitle) throws Throwable {
        List<FluentWebElement> msgElements = foxUiApp.$(By.xpath("//fox-message-box"));
        for (FluentWebElement ele : msgElements) {
            try {
                ele.$(By.tagName("svg"));
                if (ele.attribute("messageboxtitle").toLowerCase().contains(messageTitle.trim().toLowerCase()))
                    return true;
            } catch (Exception e) {
            }
        }

        msgElements = foxUiApp.$(By.cssSelector("fox-error-message ul.fox-error-messages-list li span"));
        msgElements.addAll(foxUiApp.$(By.cssSelector("fox-error-message h4")));
        for (FluentWebElement ele : msgElements) {
            if (ele.textContent().toLowerCase().contains(messageTitle.toLowerCase()))
                return true;
        }

        return false;
    }

    public static String getPageTitle() throws Throwable {
        try {
            return foxUiApp.getDriver().findElement(By.cssSelector("fox-page-header span.page-title")).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public static String getErrorMessages() throws Throwable {
        String error = "", hdr = "";
        try {
            FluentWebElement errorBox = foxUiApp.el(By.cssSelector("fox-error-message div.fox-error"));
            if (errorBox.displayed()) {
                //Header
                hdr = errorBox.el(By.cssSelector("h4")).textContent().trim().replace("\r\n", "").replace("\n", "");

                //Errors
                FluentList<FluentWebElement> errors = errorBox.$(By.cssSelector("ul li span"));
                for (FluentWebElement err : errors) {
                    error += err.textContent().trim().
                            replace("\r\n", "").replace("\n", "")
//                            .replace(".?", " | ")
//                            .replace(".", " | ")
                            + "\n";
                }
            }
        } catch (NoSuchElementException | TimeoutException e) {
            logger.debug("No error message found");
        }

        try {
            List<WebElement> msgElements = foxUiApp.getDriver().findElements(By.tagName("fox-message-box"));
            for (WebElement ele : msgElements) {
                if (ele.isDisplayed()) {
                    hdr = ele.findElement(By.cssSelector("p.fox-message__title")).getText().trim();
                    if (hdr.contains("Error")) {
                        error += ele.findElement(By.cssSelector("div.container-message")).getText().trim().replace("\n", ": ");
                        error += "[Page : " + foxUiApp.el(By.cssSelector("fox-page-header span.page-title")).text().trim() + "]";
                    }
                }
            }
        } catch (Exception e) {
        }

        error = error.toLowerCase()
                .replace("highlighted fields are different from defaults", "") //TODO temp workaround for OF screen
                .replace("\n", "")
                .replace("\r\n", "").replace(".", " ").trim();
        return error.isEmpty() ? "" : hdr + ":\n" + error;
    }

    public void waitFor(int sec) {

        try {
            Thread.sleep(sec * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param elemParam
     * @return
     * @throws Throwable
     * @DESC wait method to add more time waiting when the element is still not yet found
     */
    public boolean smartWaitElement(FluentWebElement elemParam) throws Throwable {
        Integer tries = 4, seconds = 5; //DEFAULT

        Boolean isDisplayed = false;
        try {
            isDisplayed = elemParam.await().atMost(seconds, TimeUnit.SECONDS).until(elemParam).present();
            elemParam.scrollIntoView();
            logger.info("Succeeded on first await try of element: " + elemParam.getElement().toString());
        } catch (NoSuchElementException | TimeoutException e) {
            for (int i = 1; i <= tries; i++) {
                FluentWebElement element = elemParam;
                try {
                    isDisplayed = element.await().atMost(seconds, TimeUnit.SECONDS).until(element.reset()).present();
                    element.scrollIntoView(false);
                    logger.info("Succeeded on " + i + " retries for element " + element.getElement().toString());
                    break;
                } catch (Exception ee) {
                    logger.info("Failed on " + i + " attempt/s will try again of element: " + element.getElement().toString());
                }
            }
        }
        return isDisplayed;
    }

    /**
     * generic ispresent/isenabled/isclicable
     *
     * @param element               -FluentWebElement
     * @param scenarioForScreenshot - enter_temp scenario if you need screenshot
     * @throws Throwable
     * @author: janine
     */
    public boolean isPresent_temp(FluentWebElement element, Scenario scenarioForScreenshot) throws Throwable {
        boolean ispresent = true;
        try {
            ispresent = await().atMost(20, TimeUnit.SECONDS).until(element).present();
        } catch (Exception e) {
            ispresent = false;
        }

        if (scenarioForScreenshot != null)
            ReportUtil.getIntance().takeScreenshot(getDriver(), scenarioForScreenshot);
        return ispresent;
    }

    /**
     * @param element               -FluentWebElement
     * @param scenarioForScreenshot - enter_temp scenario if you need screenshot
     * @throws Throwable
     * @author: janine
     * generic action click
     */
    public void clickElement_temp(Object element, Scenario scenarioForScreenshot) throws Throwable {

        try {
            if (element instanceof WebElement) {
                executeScript("arguments[0].click()", (WebElement) element);
            } else {
                await().atMost(20, TimeUnit.SECONDS).until((FluentWebElement) element).present();
                ((FluentWebElement) element).scrollIntoView();
                ((FluentWebElement) element).click();
            }
            //for screenshot
            if (scenarioForScreenshot != null)
                ReportUtil.getIntance().takeScreenshot(getDriver(), scenarioForScreenshot);

        } catch (Exception e) {
            if (scenarioForScreenshot != null)
                ReportUtil.getIntance().takeScreenshot(getDriver(), scenarioForScreenshot);
        }
    }

    public boolean isEnabled_temp(FluentWebElement element, Scenario scenarioForScreenshot) throws Throwable {

        await().atMost(20, TimeUnit.SECONDS).until(element).present();
        element.scrollIntoView();
        if (scenarioForScreenshot != null)
            ReportUtil.getIntance().takeScreenshot(getDriver(), scenarioForScreenshot);
        return await().atMost(1, TimeUnit.SECONDS).until(element).enabled();
    }

    public boolean isClickable_temp(FluentWebElement element, Scenario scenarioForScreenshot) throws Throwable {
        await().atMost(20, TimeUnit.SECONDS).until(element).present();
        element.scrollIntoView();
        if (scenarioForScreenshot != null)
            ReportUtil.getIntance().takeScreenshot(getDriver(), scenarioForScreenshot);
        return await().atMost(1, TimeUnit.SECONDS).until(element).clickable();
    }

    /**
     * @param element               -FluentWebElement
     * @param scenarioForScreenshot - enter_temp scenario if you need screenshot
     * @throws Throwable
     * @author: janine
     * generic enter_temp
     */
    public void enter_temp(FluentWebElement element, String enterValue, Scenario scenarioForScreenshot) throws Throwable {
        element.scrollIntoView();
        foxUiApp.await().atMost(20, TimeUnit.SECONDS).until(element).present();
        foxUiApp.await().atMost(20, TimeUnit.SECONDS).until(element).enabled();
        element.scrollIntoView(false).fill().with(enterValue);
        if (scenarioForScreenshot != null)
            ReportUtil.getIntance().takeScreenshot(getDriver(), scenarioForScreenshot);
    }

    //Sync
    public static void syncFox_temp() throws Throwable {
        foxUiApp.await().atMost(15, TimeUnit.SECONDS).until(foxUiApp).present();

        List<FluentWebElement> imgs;
        try {
            imgs = foxUiApp.$(By.xpath("../../../..//fox-loading-overlay"));
            if (imgs.size() <= 0)
                return;
        } catch (Exception e) {
            return;
        }

        for (FluentWebElement loadingImg : imgs) {
            try {
                FluentWebElement temp = loadingImg.$(By.xpath("(\"//mat-option/span[normalize-space(text())=' + listOptionToSelect + ']")).first();
                temp.await().atMost(10, TimeUnit.SECONDS).until(temp).displayed();
            } catch (Exception e) {
                return;
            }

        }
    }

    /**
     * @param element
     * @throws Throwable
     * @Author: janine
     */
    public void focusElement_temp(Object element) throws Throwable {
        try {

            if (element instanceof WebElement) {
                WebDriverWait wait = new WebDriverWait(getDriver(), 5);
                wait.until(ExpectedConditions.visibilityOf((WebElement) element));
                JavascriptExecutor executor = (JavascriptExecutor) getDriver();
                executor.executeScript("arguments[0].scrollIntoView();", (WebElement) element);
                executor.executeScript("arguments[0].focus();", (FluentWebElement) element);
            } else {
                JavascriptExecutor executor = (JavascriptExecutor) getDriver();
                await().explicitlyFor(5, TimeUnit.SECONDS).until(((FluentWebElement) element)).present();
                executor.executeScript("arguments[0].scrollIntoView();", (FluentWebElement) element);
                executor.executeScript("arguments[0].focus();", (FluentWebElement) element);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This will get column indexes for a table element of Fox-Ui application
     *
     * @param buttonToClick - button element
     */
    public void clickButton_temp(FluentWebElement buttonToClick) throws Exception {

        if (buttonToClick == null)
            throw new Exception("Expected Button is not present/populated on Page.");
        else {
            if (buttonToClick.enabled()) {
                buttonToClick.click();
            } else {
                logger.info("Expected Button is not enabled on Page.");
                throw new RuntimeException();
            }
        }
    }

    /**
     * @param elemParam
     * @return
     * @throws Throwable
     * @DESC wait method to add more time waiting when the element is still not yet found
     */
    public boolean smartWaitElement_temp(FluentWebElement elemParam) throws Throwable {
        Integer tries = 4, seconds = 5; //DEFAULT
        Boolean isDisplayed = false;
        try {

            isDisplayed = elemParam.await().atMost(seconds, TimeUnit.SECONDS).until(elemParam).displayed();
            elemParam.scrollIntoView(false);
            logger.info("Succeeded on first await try of element: " + elemParam.getElement().toString());

        } catch (org.openqa.selenium.NoSuchElementException | TimeoutException e) {

            for (int i = 1; i <= tries; i++) {

                FluentWebElement element = elemParam;
                try {
                    isDisplayed = element.await().atMost(seconds, TimeUnit.SECONDS).until(element.reset()).present();
                    element.scrollIntoView(false);
                    logger.info("Succeeded on " + i + " retries for element " + element.getElement().toString());
                    break;
                } catch (Exception exc) {
                    logger.info("Failed on " + i + " attempt/s will try again of element: " + element.getElement().toString());
                }

            }
        }
        return isDisplayed;
    }

    //Sync
    public static void syncFox() throws Throwable {
        foxUiApp.await().atMost(10, TimeUnit.SECONDS).until(foxUiApp).present();

        List<WebElement> imgs;
        int timeOut = 0;
        boolean stillLoading = true;
        do {
            timeOut += 2;

            //Loading images - traditional
            try {
                foxUiApp.await().explicitlyFor(1, TimeUnit.SECONDS);
                imgs = foxUiApp.getDriver().findElements(By.cssSelector("img.img-load"));//div.loading-overlay div.loading-indicator img.img-load
                if (imgs.size() <= 0) {
                    foxUiApp.await().explicitlyFor(1, TimeUnit.SECONDS);
                    imgs = foxUiApp.getDriver().findElements(By.cssSelector("div.loading-overlay"));
                }

                if (imgs.size() <= 0)
                    stillLoading = false;
                else logger.info("Page is still loading...");
            } catch (Exception e) {
            }

            //Loading buttons
            try {
                foxUiApp.await().explicitlyFor(1, TimeUnit.SECONDS);
                imgs = foxUiApp.getDriver().findElements(By.cssSelector("fox-loading-button button"));//button.btn-primary-loading
                if (imgs.size() <= 0)
                    stillLoading = false;
                else {
                    for (WebElement ele : imgs) {
                        if (ele.getAttribute("class").contains("loading")) {
                            stillLoading = true;
                            logger.info("Application is still processing...");
                            break;
                        }
                    }
                }
            } catch (Exception e) {
            }
        } while (stillLoading && timeOut < 75);
        foxUiApp.await().explicitlyFor(2, TimeUnit.SECONDS);

        String txtError = getErrorMessages();
        if (!ignoreSyncError) {
            if (!txtError.toLowerCase().contains("highlighted fields are different from defaults")) //TODO temp workaround for OF screen
                Assert.assertTrue("\n" + txtError, txtError.isEmpty());
            if (!txtError.contains("Highlighted fields are different from defaults")) {
            } //TODO temp workaround for OF scressn
            Assert.assertTrue("\n" + txtError, txtError.isEmpty());
        } else if (!txtError.isEmpty()) {
            logger.error("\n================================================================");
            logger.error(txtError);
            logger.error("\n================================================================");
        }

        //Multiple sessions check
        try {
            FluentWebElement maa_errHdr = foxUiApp.el(By.cssSelector("fox-adj-acc-denied div.txt-error-header"));
            foxUiApp.await().atMost(2, TimeUnit.SECONDS).until(maa_errHdr).present();
            Assert.assertTrue("\nUser is trying to open multiple adjudication sessions, which is not allowed.", true);
        } catch (Exception e2) {
        }

        if (stillLoading)
            throw new Exception("Application didn't respond or move to next screen");
    }

    public static boolean isPageReady(FluentWebElement pageElementToBePresent) throws Throwable {
        try {
            foxUiApp.await().atMost(60, TimeUnit.SECONDS).until(pageElementToBePresent).displayed();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isPageReadyLong(FluentWebElement pageElementToBePresent) throws Throwable {
        try {
            foxUiApp.await().atMost(90, TimeUnit.SECONDS).until(pageElementToBePresent).displayed();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isPageReady(FluentWebElement pageElementToBePresent, String pageTitle) throws Throwable {
        try {
            syncFox();
            foxUiApp.await().atMost(10, TimeUnit.SECONDS).until(pageElementToBePresent).displayed();
            if (!FoxUI.getPageTitle().toUpperCase().contains(pageTitle.trim().toUpperCase()))
                return false;

            return true;
        } catch (Exception e) {
            syncFox();
            return false;
        }
    }

    public static void syncFoxIgnoreError() throws Throwable {
        ignoreSyncError = true;
        syncFox();
        ignoreSyncError = false;
    }


    //---------------------------- MENUS -------------------------------------------------
    public static void goToFoxPage(String whichPage) throws Throwable {
        //Ensure you're in homepage
        foxUiApp.el(By.cssSelector("img.fox-icon")).click();
        syncFoxIgnoreError(); //bring back to the orig syncFox barom
        //temp workaround for current loading errors on foxtusr0
        foxUiApp.await().explicitlyFor(2, TimeUnit.SECONDS); //TODO remove later

        if (!whichPage.contains(">")) {
            openFoxPageUsingCmd(whichPage);
            return;
        }

        //Get the pages
        String[] pages = whichPage.split(">");

        //Get the latest element
        FluentWebElement menuHamburgarBtn = foxUiApp.el(By.xpath("//div[contains(text(),'Menu')]/../*[@role='img']"));
        menuHamburgarBtn.await().atMost(10, TimeUnit.SECONDS).until(menuHamburgarBtn).present();

        //Open the nav bar
        FluentWebElement navBar;
        Actions actions = new Actions(menuHamburgarBtn.getDriver());
        try {
            menuHamburgarBtn.reset();
            actions.moveToElement(menuHamburgarBtn.getElement()).click().build().perform();

            navBar = foxUiApp.el(By.xpath("//mat-sidenav-container/mat-sidenav"));
            menuHamburgarBtn.await().atMost(3, TimeUnit.SECONDS).until(navBar.reset()).displayed();
        } catch (Exception e1) {
            try {
                menuHamburgarBtn.reset();
                actions.moveToElement(menuHamburgarBtn.getElement()).click().build().perform();

                navBar = foxUiApp.el(By.xpath("//mat-sidenav-container/mat-sidenav"));
                menuHamburgarBtn.await().atMost(3, TimeUnit.SECONDS).until(navBar.reset()).displayed();
            } catch (Exception e2) {
                try { //in-case actions doesn't support
                    menuHamburgarBtn.reset();
                    menuHamburgarBtn.click();

                    navBar = foxUiApp.el(By.xpath("//mat-sidenav-container/mat-sidenav"));
                    menuHamburgarBtn.await().atMost(3, TimeUnit.SECONDS).until(navBar.reset()).displayed();
                } catch (Exception e4) {
                    throw new Exception("Error while opening navigation bar. Couldn't open the page: " + whichPage);
                }
            }
        }

        //Open the section
        try {
            FluentWebElement sectionBtn = navBar.reset().$(By.xpath("div[@class='static-sidenav']")).first();
            sectionBtn = sectionBtn.$(By.xpath("//p[text()='" + pages[0].trim() + "']/..")).first();
            sectionBtn.click();
        } catch (Exception e) {
            throw new Exception("Mentioned menu is not available: " + whichPage);
        }

        //Open the page
        FluentWebElement pageLnk = navBar.reset().$(By.xpath("div[@class='sidenav-items']")).first();
        try {
            pageLnk.$(By.xpath("//div[text()='" + pages[1].trim() + "']/..")).first().click();
        } catch (Exception e) {
            try {
                pageLnk.$(By.xpath("div/div/div[text()='" + pages[1].trim() + "']/..")).first().click();
            } catch (Exception e3) {
                throw new Exception("Mentioned menu is not available: " + whichPage);
            }
        }

        if (!menuHamburgarBtn.await().atMost(2, TimeUnit.SECONDS).until(navBar.reset()).not().displayed())
            throw new Exception("Error while opening: " + whichPage);

        menuHamburgarBtn.await().atMost(5, TimeUnit.SECONDS).until(navBar).not().displayed();
    }

    public static void goToFoxRecord(String whichPage, String whichType, String whichRecord) throws Throwable {
        WebDriver driver = foxUiApp.getDriver();
        foxUiApp.await().explicitlyFor(3, TimeUnit.SECONDS);

        if (whichRecord.isEmpty()) {
            driver.findElement(By.id("com1")).sendKeys(whichPage + Keys.ENTER);
            return;
        } else
            driver.findElement(By.id("com1")).sendKeys(whichPage);

        foxUiApp.await().explicitlyFor(1, TimeUnit.SECONDS);
        if (whichType.equalsIgnoreCase("mem"))
            driver.findElement(By.id("memid")).sendKeys(whichRecord + Keys.ENTER);
        else if (whichType.equalsIgnoreCase("Comm"))
            driver.findElement(By.id("commid")).sendKeys(whichRecord + Keys.ENTER);
        else
            driver.findElement(By.id("claimid")).sendKeys(whichRecord + Keys.ENTER);
    }

    private static void openFoxPageUsingCmd(String cmdKey) throws Throwable {
        goToFoxRecord(cmdKey, "", "");
    }


    //---------------------------- CHECK BOX -------------------------------------------------

    /**
     * This will check first Available check box in FOX-UI, This will not select selectAll check box
     *
     * @param unlockItem - FluentListForFirstAvaliableCheckBox
     */
    public static void clickFirstAvailableCheckBox(FluentList<FluentWebElement> unlockItem) throws Exception {
        int firstAvailableCheckbox = getFirstUnlockedWqItem(unlockItem);
        if (firstAvailableCheckbox > 0) {
            unlockItem.get(firstAvailableCheckbox).scrollIntoView();
            new FoxUI().clickButton(unlockItem.get(firstAvailableCheckbox));
            Thread.sleep(3000);
        }
    }

    public static int getFirstUnlockedWqItem(FluentList<FluentWebElement> unlockItem) {

        int unlockedItemIndex = -1;
        int i = 0;

        if (unlockItem.present()) {

            for (i = 1; i < unlockItem.size(); i++) {
                boolean checkBoxStatusEnabled = unlockItem.get(i).enabled();
                if (checkBoxStatusEnabled) {
                    unlockedItemIndex = i;
                    break;
                }
            }
        }
        return i;
    }

    /**
     * This will check All check box in FOX-UI
     *
     * @param unlockItem - FluentListForFirstAvaliableCheckBox
     */
    public void clickSelectAllCheckBox(FluentList<FluentWebElement> unlockItem) throws Exception {
        clickButton_temp(unlockItem.get(0));
        Thread.sleep(3000);
    }

    public static void selectCheckBox(FluentWebElement element, boolean yesOrNo) throws Throwable {
        if ((!element.selected() && yesOrNo) || (element.selected() && !yesOrNo))
            FoxUI.click(element);
    }


    //  -----------------------  UTILS -----------------------------------
    public static String compareLists(List<String> leftList, List<String> rightList, boolean isReportOnlyDiff) throws Throwable {
        String errorText = "", errTextExp = "", errTextAct = "";

        //Get the iterators
        ListIterator itL = leftList.listIterator();
        ListIterator itR = rightList.listIterator();

        //Loop and compare
        while (itL.hasNext()) {
            String leftValue = itL.next().toString().trim();
            String rightValue = itR.next().toString().trim();

            if (leftValue.contains("$")) {
                leftValue = leftValue.replace("$", "");
                if (rightValue.contains("$")) {
                    rightValue = rightValue.replace("$", "");
                }
            }

            if (NumberUtils.isNumber(leftValue) && !(rightValue.length() == 0 || rightValue.substring(0, 1).equalsIgnoreCase("*"))) {
                if (rightValue.length() == 0) {
                    errTextAct += leftValue + ", ";
                    errTextExp += rightValue + ", ";

                } else if (!NumberUtils.createNumber(leftValue).equals(NumberUtils.createNumber(rightValue))) {
                    errTextAct += leftValue + ", ";
                    errTextExp += rightValue + ", ";

                }
            } else {
                if (rightValue.length() != 0 && rightValue.substring(0, 1).equalsIgnoreCase("*")) {
                    rightValue = rightValue.substring(1);
                    if (!rightValue.toLowerCase().contains(leftValue.trim().toLowerCase())) {
                        errTextAct += leftValue + ", ";
                        errTextExp += rightValue + ", ";
                    }
                } else if (!leftValue.equalsIgnoreCase(rightValue)) {
                    errTextAct += leftValue + ", ";
                    errTextExp += rightValue + ", ";

                }
            }
        }

        if (isReportOnlyDiff && errorText.length() > 0) {
            errorText = "Expected: [" + errTextExp.substring(0, errTextExp.length() - 1).trim() + "]\n";
            errorText += "Actual : [" + errTextAct.substring(0, errTextAct.length() - 1).trim() + "]\n";
        } else if (!isReportOnlyDiff && errTextExp.length() > 0) {
            errorText = "Expected: " + rightList.toString() + "\n";
            errorText += "Actual  : " + leftList.toString() + "\n";
        }

        //return error text
        return errorText;
    }

    public static String compareListOfMaps(LinkedList<Map<String, String>> leftList, LinkedList<Map<String, String>> rightList, boolean isReportOnlyDiff) throws Throwable {
        String errorText = "";

        for (int i = 0; i < leftList.size(); i++) {
            errorText += compareMap(leftList.get(i), rightList.get(i), isReportOnlyDiff);
        }

        //return error text
        return errorText;
    }

    public static String compareMap(Map<String, String> expectedMap, Map<String, String> actualMap,
                                    boolean isReportOnlyDiff, boolean isReportOnlyDiffColumns) throws Throwable {
        String errorText = "", errTextExp = "", errTextAct = "", mismatchedColumns = "";
        boolean isMatched;

        //Loop and compare
        for (String key : actualMap.keySet()) {
            isMatched = true;
            String leftValue = "";
            if (expectedMap.containsKey(key))
                leftValue = expectedMap.get(key).toString().trim();
            else {
                mismatchedColumns += key + ";";
                continue;
            }
            String rightValue = actualMap.get(key).toString().trim();

            if (leftValue.contains("$")) {
                leftValue = leftValue.replace("$", "");
                if (rightValue.contains("$")) {
                    rightValue = rightValue.replace("$", "");
                }
            }

            if (NumberUtils.isNumber(leftValue) && !(rightValue.isEmpty() || rightValue.substring(0, 1).equalsIgnoreCase("*"))) {
                if (!NumberUtils.createNumber(leftValue).equals(NumberUtils.createNumber(rightValue))) {
                    isMatched = false;
                }
            } else {
                if (!rightValue.isEmpty() && rightValue.substring(0, 1).equalsIgnoreCase("*")) {
                    rightValue = rightValue.substring(1);
                    if (!rightValue.toLowerCase().contains(leftValue.trim().toLowerCase())) {
                        isMatched = false;
                    }
                } else if (FoxUI.isDate(leftValue, "MM/DD/YYYY")) {
                    rightValue = new E2EUtils().getFormattedDate(rightValue, "MM/DD/YYYY");
                    if (DateUtils.convertStringToDate(rightValue).compareTo(DateUtils.convertStringToDate(leftValue)) != 0) {
                        isMatched = false;
                    }
                } else if (!leftValue.equalsIgnoreCase(rightValue)) {
                    isMatched = false;
                }
            }

            if (!isMatched) {
                mismatchedColumns += key + ";";
                errTextAct += leftValue + ", ";
                errTextExp += rightValue + ", ";
            }
        }

        if (isReportOnlyDiffColumns)
            return mismatchedColumns;

        if (!errorText.isEmpty()) {
            if (isReportOnlyDiff) {
                errorText = "Expected: [" + errTextExp.substring(0, errTextExp.length() - 1).trim() + "]\n";
                errorText += "Actual : [" + errTextAct.substring(0, errTextAct.length() - 1).trim() + "]\n";
            } else if (!isReportOnlyDiff) {
                errorText = "Expected: " + expectedMap.toString() + "\n";
                errorText += "Actual  : " + actualMap.toString() + "\n";
            }
        }

        //return error text
        return errorText;
    }

    public static String compareMap(Map<String, String> expectedMap, Map<String, String> actualMap,
                                    boolean isReportOnlyDiff) throws Throwable {
        return compareMap(expectedMap, actualMap, isReportOnlyDiff, false);
    }

    public static LinkedList<Map<String, String>> transposeListsToMap(List<List<String>> lists, String keyNames) throws Throwable {
        LinkedList<Map<String, String>> maps = new LinkedList<>();

        int value = 0;
        for (List<String> list : lists) {
            String[] keys = keyNames.split(";");

            //find/replace with dynamic value
            for (int i = 0; i < keys.length; i++) {
                if (keys[i].trim().contains("=")) { //ex.: AC_LINE_NO=I+1
                    String oper, increment;

                    oper = keys[i].trim().split("=")[1].contains("+") ? "\\+" : "\\-";
                    increment = keys[i].trim().split("=")[1].split(oper)[1];
                    value += Integer.parseInt(increment);

                    keys[i] = keys[i].split("=")[0] + "=" + String.valueOf(value);
                }
            }

            maps.add(transposeListToMap(list, String.join(";", keys)));
        }

        return maps;
    }

    public static HashMap transposeListToMap(List<String> list, String keyNames) throws Throwable {
        HashMap<String, String> map = new HashMap<>();

        String[] keys = keyNames.split(";");
        String value;
        int valueIdx = 0;
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].contains("=")) {
                value = String.valueOf(keys[i].split("=")[1]);
                keys[i] = keys[i].split("=")[0];
            } else value = list.get(valueIdx++);

            map.put(keys[i].trim().toUpperCase(), value);
        }

        return map;
    }

    /**
     * @param expectedValue
     * @param actualValue
     * @param scenarioForScreenshot
     * @throws Throwable <<<<<<< HEAD
     * @Author: Janine 10/22
     */
    public void compareValue(String expectedValue, String actualValue, Scenario scenarioForScreenshot) throws Throwable {

        if (scenarioForScreenshot != null) {
            ReportUtil.getIntance().takeScreenshot(getDriver(), scenarioForScreenshot);
            scenarioForScreenshot.write("Expected value : " + expectedValue + "should match actual value: " + actualValue);
        }
        Assert.assertEquals("Expected value and actual value should match " + actualValue,
                expectedValue, actualValue);
    }

    public static String compareLists_temp(List<String> leftList, List<String> rightList, boolean isReportOnlyDiff) throws Throwable {
        String errorText = "", errTextExp = "", errTextAct = "";

        //Get the iterators
        ListIterator itL = leftList.listIterator();
        ListIterator itR = rightList.listIterator();

        //Loop and compare
        while (itL.hasNext()) {
            String leftValue = itL.next().toString().trim();
            String rightValue = itR.next().toString().trim();


            if (NumberUtils.isNumber(leftValue) && NumberUtils.isNumber(rightValue) && !(rightValue.length() == 0 || rightValue.substring(0, 1).equalsIgnoreCase("*"))) {
                if (rightValue.length() == 0) {
                    errTextAct += leftValue + ", ";
                    errTextExp += rightValue + ", ";
                    errorText += "Expected: " + leftValue + " but actual: " + rightValue + "\n";
                } else if (!NumberUtils.createNumber(leftValue).equals(NumberUtils.createNumber(rightValue))) {
                    errTextAct += leftValue + ", ";
                    errTextExp += rightValue + ", ";
                    errorText += "Expected: " + leftValue + " but actual: " + rightValue + "\n";
                }
            } else {
                if (rightValue.length() != 0 && rightValue.substring(0, 1).equalsIgnoreCase("*")) {
                    rightValue = rightValue.substring(1);
                    if (!rightValue.toLowerCase().matches(leftValue.trim().toLowerCase())) {
                        errTextAct += leftValue + ", ";
                        errTextExp += rightValue + ", ";
                    }
                } else if (!regexMatch(leftValue, rightValue) && !leftValue.equalsIgnoreCase(rightValue)) {
                    errTextAct += leftValue + ", ";
                    errTextExp += rightValue + ", ";
                    errorText += "Expected: " + leftValue + " but actual: " + rightValue + "\n";
                }
            }
        }

        if (isReportOnlyDiff && errorText.length() > 0) {
            errorText = "Expected: [" + errTextExp.substring(0, errTextExp.length() - 1).trim() + "]\n";
            errorText += "Actual : [" + errTextAct.substring(0, errTextAct.length() - 1).trim() + "]\n";
        } else if (!isReportOnlyDiff && errTextExp.length() > 0) {
            errorText = "Expected: " + leftList.toString() + "\n";
            errorText += "Actual  : " + rightList.toString() + "\n";
        }


        return errorText;
    }

    private static boolean regexMatch(String leftValue, String rightValue) {
        boolean isMatching = false;

        Pattern myPattern = Pattern.compile(leftValue, Pattern.CASE_INSENSITIVE);
        Matcher myMatcher = myPattern.matcher(rightValue);

        if (!myMatcher.matches()) {
            myPattern = Pattern.compile(rightValue, Pattern.CASE_INSENSITIVE);
            myMatcher = myPattern.matcher(leftValue);
        }

        return isMatching = myMatcher.matches();
    }

    /**
     * TcoE
     *
     * @param dateText
     * @param dateInFormat
     * @return
     */
    public static boolean isDate(String dateText, String dateInFormat) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateInFormat);
        try {
            Date date = dateFormat.parse(dateText);

        } catch (Exception e) {
            return false;
        }
        return true;

    }

    /**
     * This method remove null values from a list
     *
     * @param dataList
     * @return
     * @author TCOE - emarasi1
     */
    public static List<String> removeNullValues(List<String> dataList) {
        List<String> dataNewList = new ArrayList<>();
        for (String data : dataList) {
            if (data != null) {
                data = trimData(data);
                if (!data.trim().isEmpty()) {
                    dataNewList.add(data);
                }
            }
        }
        return dataNewList;
    }

    /**
     * This method trims unwanted spaces on the data.
     *
     * @param data
     * @return
     * @author TCOE - emarasi1
     */
    public static String trimData(String data) {

        if (data != null) {
            if ("null".equalsIgnoreCase(data)) {
                return "";
            } else {
                return data.trim();
            }
        } else {
            return "";
        }

    }

    /**
     * Description: use to return only field value from the gerkin step
     *
     * @param criteria
     * @author Janine
     */
    public static ArrayList<String> getValueList(List<String> criteria, String delimiter) {
        ArrayList<String> mapCrit = new ArrayList<>();
        for (String crit : criteria) {
            String[] withs = crit.split(delimiter);
            mapCrit.add(withs[1].trim());
        }
        return mapCrit;
    }

    public static Map<String, String> listToMap(List<String> criteria, String delimiter) {
        Map<String, String> mapCrit = new TreeMap<>();
        for (String crit : criteria) {
            String[] withs = crit.split(delimiter);
            if (withs.length < 2)
                mapCrit.put(withs[0].trim(), "");
            else
                mapCrit.put(withs[0].trim(), withs[1].trim());
        }
        return mapCrit;
    }

    /**
     * author nganji
     *
     * @param dateFromDB
     * @param expectedFormat
     * @param oldFormat
     * @return
     */
    public static String dateFormater(String dateFromDB, String expectedFormat, String oldFormat) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(oldFormat);
        Date date = null;
        String convertedDate = null;
        try {
            date = dateFormat.parse(dateFromDB);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(expectedFormat);
            convertedDate = simpleDateFormat.format(date);
        } catch (Exception e) {
            convertedDate = "";
        }

        return convertedDate;

    }

    /**
     * TcoE
     *
     * @param dateText
     * @param dateInFormat
     * @return
     */
    public static boolean isDate_temp(String dateText, String dateInFormat) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateInFormat);
        try {
            Date date = dateFormat.parse(dateText);

        } catch (Exception e) {
            return false;
        }
        return true;

    }

    /**
     * @param filePath
     * @param fileName
     * @return
     * @author nganji
     */
    public static File getNewFileFromDirectory(String filePath, String fileName, String extension) {
        File newfile = null;
        File directory = new File(filePath);
        FileFilter fileFilter = new WildcardFileFilter(fileName + ".*" + extension);
        File[] files = directory.listFiles(fileFilter);

        if (files.length > 0) {
            /** The newest file comes first due to sorting **/
            Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
            newfile = files[0];
        }
        return newfile;
    }

    /**
     * This method remove null values from a list
     *
     * @param dataList
     * @return
     * @author TCOE - emarasi1
     */
    public static List<String> removeNullValues_temp(List<String> dataList) {
        List<String> dataNewList = new ArrayList<>();
        for (String data : dataList) {
            if (data != null) {
                data = trimData_temp(data);
                if (!data.trim().isEmpty()) {
                    dataNewList.add(data);
                }
            }
        }
        return dataNewList;
    }

    /**
     * This method trims unwanted spaces on the data.
     *
     * @param data
     * @return
     * @author TCOE - emarasi1
     */
    public static String trimData_temp(String data) {

        if (data != null) {
            if ("null".equalsIgnoreCase(data)) {
                return "";
            } else {
                return data.trim();
            }
        } else {
            return "";
        }

    }

    /**
     * Description: use to return only field value from the gerkin step
     *
     * @param criteria
     * @author Janine
     */
    public static ArrayList<String> getValueList_temp(List<String> criteria, String delimiter) {
        ArrayList<String> mapCrit = new ArrayList<>();
        for (String crit : criteria) {
            String[] withs = crit.split(delimiter);
            mapCrit.add(withs[1].trim());
        }
        return mapCrit;
    }

    public static ArrayList<String> getGherkinVal(List<String> criteria, String delimiter) {
        ArrayList<String> mapCrit = new ArrayList<>();
        for (String crit : criteria) {
            String[] withs = crit.split(delimiter);
            mapCrit.add(withs[0].trim());
        }
        return mapCrit;
    }

    public void waitUntilFoxRotatingIconDisappear_temp() {
        try {
            await().atMost(20, TimeUnit.SECONDS).until(el("img.img-load")).not().present();
        } catch (Exception e) {
        }
    }

    /**
     * @param fileName
     * @return
     * @throws Throwable
     * @Author nganji
     */
    public static String readFile_temp(File fileName) throws Throwable {

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }

    }

    /**
     * @param messageTitle
     * @return
     * @throws Throwable
     * @author TCoE/nganji
     */
    public static boolean isMessageExists_temp(String messageTitle) throws Throwable {

        List<FluentWebElement> msgElements = foxUiApp.$(By.xpath("//fox-message-box"));

        for (FluentWebElement ele : msgElements) {
            try {
                ele.$(By.tagName("svg"));
                return ele.attribute("messageboxtitle").toLowerCase().contains(messageTitle.trim().toLowerCase());
            } catch (Exception e) {
            }
        }
        return false;

    }

    public static boolean isUserHasAccess() throws Throwable {
        try {
            foxUiApp.await().atMost(10, TimeUnit.SECONDS).until(foxUiApp.$(By.cssSelector("fox-insufficient-access-page > h1"))).present();
            return false;
        } catch (Exception e1) {
            return true;
        }
    }

    public static boolean isAccessDeniedMsgDisplayed() throws Throwable {
        return isUserHasAccess();
    }

    public void waitFor_temp(int sec) {

        try {
            Thread.sleep(sec * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void enterSeriesDetailsSameValues(LinkedList<String> values, FluentWebElement rootElement, String locator,
                                             int rowIdx, int startColIdx) throws Throwable {

        int colIdx = startColIdx - 1;

        FluentWebElement currEle;
        for (String value : values) {
            colIdx++;

            if (value.isEmpty()) {
                continue;
            }

            if (rootElement == null) {
                currEle = foxUiApp.el(By.cssSelector(locator + colIdx));
                if (!currEle.present())
                    currEle = foxUiApp.el(By.xpath("//table/tbody/tr[" + (rowIdx + 1) + "]/td[" + (colIdx + 1) + "]//input"));
                if (!currEle.present())
                    currEle = foxUiApp.el(By.xpath("//table/tbody/tr[" + (rowIdx + 1) + "]/td[" + (colIdx + 1) + "]//select"));
            } else {
                currEle = rootElement.el(By.cssSelector(locator + colIdx));
                if (!currEle.present())
                    currEle = rootElement.el(By.xpath("//tr[" + (rowIdx + 1) + "]/td[" + (colIdx + 1) + "]//input"));
            }

            this.sendKeys(currEle, value);
            FluentWebElement currElemAltK = foxUiApp.el(By.xpath("//table/tbody/tr[" + (rowIdx + 2) + "]/td[" + (colIdx + 1) + "]//input"));
            Actions action = new Actions(foxUiApp.getDriver());
            //PressTab
            action.sendKeys(Keys.TAB);
            //Go to previous tab
            action.keyDown(Keys.SHIFT);
            action.sendKeys(Keys.TAB);
            action.keyUp(Keys.SHIFT);
            //Replicate values to all rows
            action.keyDown(Keys.ALT);
            action.sendKeys("k");
            action.keyUp(Keys.ALT);
            foxUiApp.await().atMost(10, TimeUnit.SECONDS).until(currElemAltK).displayed();
            action.build().perform();
        }
    }

    /**
     * Convert old member number spacing to new spacing
     * 12345678911 to 123456789 1 1
     *
     * @param mbrNumber
     * @return
     * @auth ksadullo
     */
    public static String convertMemberToNewFormat(String mbrNumber) {
        mbrNumber.replaceAll(" ", "");
        if (mbrNumber.length() < 11)
            return mbrNumber;

        String[] memberFormat = splitToNChar(mbrNumber, 9);
        String part1 = memberFormat[0];
        String part1_2 = memberFormat[1];
        memberFormat = splitToNChar(part1_2, 1);
        String part2 = memberFormat[0];
        String part3 = memberFormat[1];

        String finalMemberNum = part1 + " " + part2 + " " + part3;
        return finalMemberNum;
    }

    /**
     * Split text into n number of characters.
     *
     * @param text the text to be split.
     * @param size the split size.
     * @return an array of the split text.
     */
    private static String[] splitToNChar(String text, int size) {
        List<String> parts = new ArrayList<>();

        int length = text.length();
        for (int i = 0; i < length; i += size) {
            parts.add(text.substring(i, Math.min(length, i + size)));
        }
        return parts.toArray(new String[0]);
    }

    /**
     * handling UI synchronization
     *
     * @param iCount count loop
     * @param iDelay waiting for each iterator
     * @return an array of the split text.
     */
    public void executer(Integer iCount, Integer iDelay) throws InterruptedException, ExecutionException {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        List<Future<Integer>> futures = new ArrayList<>(iCount);
        for (int i = 0; i < iCount; i++) {
            int j = i;
            futures.add(scheduler.schedule(() -> j, iDelay, TimeUnit.MILLISECONDS));
        }
        for (Future<Integer> e : futures) {
            e.get();
        }
    }

    //Dynamic wait
    public void scheduledExecutorService() throws InterruptedException, ExecutionException {
        int iCount = 5_000, iDelay = 5_000;
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        logger.info("Start ...");
        List<Future<Integer>> futures = new ArrayList<>(iCount);
        for (int i = 0; i < iCount; i++) {
            int j = i;
            futures.add(scheduler.schedule(() -> j, iDelay, TimeUnit.MILLISECONDS));
        }
        for (Future<Integer> e : futures) {
            e.get();
        }
        logger.info("Complete");
    }


    //-------------------  BROWSERS -----------------------------
    public static void openBrowser(boolean isNew, int index, String type) throws Throwable {
        int browserIdx;

        if (isNew) {
            //TODO Replace with switchTo().newWindow() once we migrated to Selenium 4.0

            JavascriptExecutor js = (JavascriptExecutor) foxUiApp.getDriver();
            Thread.sleep(1000);
            if (type.equalsIgnoreCase("TAB"))
                js.executeScript("window.open('', '_blank')");
            else if (type.equalsIgnoreCase("WINDOW"))
                js.executeScript("window.open('', '_blank', 'toolbar=0,location=0,menubar=0')");

            Thread.sleep(2000);
            browserIdx = foxUiApp.getDriver().getWindowHandles().size() - 1;
        } else browserIdx = index;

        //Open
        String newTabOrWindow = (String) foxUiApp.getDriver().getWindowHandles().toArray()[browserIdx];
        foxUiApp.getDriver().switchTo().window(newTabOrWindow);
        foxUiApp.getDriver().manage().window().maximize();
    }

    public static void closeBrowser(int indexToClose, int indexToActivate) throws Throwable {
        String handle = "";

        int bwrs = foxUiApp.getDriver().getWindowHandles().size();

        //Close
        handle = (String) foxUiApp.getDriver().getWindowHandles().toArray()[indexToClose];
        foxUiApp.getDriver().switchTo().window(handle);
        foxUiApp.getDriver().close();
        if (bwrs <= 1) return;

        //Activate other
        if (indexToActivate == -1)
            indexToActivate = 0;

        handle = (String) foxUiApp.getDriver().getWindowHandles().toArray()[indexToActivate];
        foxUiApp.getDriver().switchTo().window(handle);
    }

    /**
     * MM/dd/yyyy format
     *
     * @return
     * @author ksadullo
     * TIMEZONE USED IS USA/CANADA
     */
    public String getDate() {
        Date dNow = new Date();
        SimpleDateFormat ft =
                new SimpleDateFormat("MM/dd/yy");
        ft.setTimeZone(TimeZone.getTimeZone("USA/Canada"));
        // GUIDE ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
        // Current Date: Sun 2004.07.18 at 04:14:09 PM PDT
        return ft.format(dNow);
    }

    /**
     * defined format
     *
     * @return
     * @author nthavuta
     */
    public String getDate(String format) {
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat(format);
        return ft.format(dNow);
    }
}
