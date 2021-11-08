package util.endToEnd.AdditionalUtilsEndtoEnd;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.slf4j.LoggerFactory;
import util.endToEnd.SharedMemory;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.*;
import java.io.FileOutputStream;
import static java.lang.Boolean.*;

/**
 * Created by schinnag on 4/6/2018.
 * This class is used to read data from excel file. From Feature file excel column name will be passed
 * to retrieve the data for that column from specified sheet and row.
 * <p>
 * Created to utilize excel data for E2E scenarios.
 */
public class GetDataFromExcel {
    //Logger
    public static org.slf4j.Logger logger = LoggerFactory.getLogger(GetDataFromExcel.class.getName());

    //Variables
    public String excelFilePath = "";
    private Workbook excelBook = null;
    private Sheet excelSheet = null;
    private String sheetName = "";
    private FileInputStream fIS = null;
    private String currentScenarioID = "";
    private int scenarioDataID = 0;
    public String rowName=null;

    //Constructor
    public GetDataFromExcel(String excelFilePath, String sheetName) {
        this.excelFilePath = excelFilePath;
        this.sheetName = sheetName;
    }

    //Connect the file
    private void connectExcelFile() throws Throwable {
        try {
            excelFilePath = (excelFilePath.startsWith("/")) ? excelFilePath.substring(1) : excelFilePath;
            excelBook = WorkbookFactory.create(new File(excelFilePath), (String) null, true); //Open in READ_ONLY mode
        } catch (Exception e) {
            //Delete old one
            new File(excelFilePath).delete();

            //Recopy the file and try again
            String srcFilePath = excelFilePath.replace("target/test-classes", "src/test/resources");
            srcFilePath = (srcFilePath.startsWith("/")) ? srcFilePath.substring(1) : srcFilePath;
            FileUtils.copyFile(new File(srcFilePath), new File(excelFilePath));
            excelBook = WorkbookFactory.create(new File(excelFilePath), (String) null, true); //Open in READ_ONLY mode
        }

        excelSheet = excelBook.getSheet(sheetName);
        if (excelSheet == null)
            throw new Exception("Invalid data file/sheet: " + excelFilePath + "::" + sheetName);
    }

    //Close the file
    public void closeExcelFile() throws Throwable {
        if (excelBook != null) {
            excelBook.close();
        }
        if (fIS != null) {
            fIS.close();
        }
    }

    //Set Sheet
    public void setSheet(String sheetName) throws Throwable {
        this.sheetName = sheetName;
    }

    //Get Row
    public Row getRow(String sheetName, int rowNumber) throws Throwable {
        return excelBook.getSheet(sheetName).getRow(rowNumber);
    }

    //Set current scenario ID
    public void setCurrentScenario(String scenarioID) {
        this.currentScenarioID = scenarioID;
        this.scenarioDataID = 1;
    }

    public void setCurrentScenario(String scenarioID, int scenarioDataNo) {
        this.currentScenarioID = scenarioID;
        this.scenarioDataID = scenarioDataNo;
    }

    //Get column index
    private Map<String, Integer> getColumnIndexes(String columnNames) {
        //Get the column names
        String[] expColumns = null;
        if (columnNames == null || columnNames.length() == 0 || columnNames.isEmpty()) {
            expColumns = new String[]{};
        } else {
            expColumns = ("SCENARIO_ID;" + columnNames).split(";");
        }

        //Get the actual columns and loop
        Cell currCell = null;
        Map<String, Integer> indexes = new HashMap<>();
        Row header = excelSheet.getRow(0);

        for (int actCol = 0; actCol < header.getPhysicalNumberOfCells(); actCol++) {
            currCell = header.getCell(actCol);
            if (currCell == null) continue;

            //Check for expected columns
            if (expColumns.length == 0)
                indexes.put(currCell.getStringCellValue().trim().toUpperCase(), actCol);
            else {
                for (int expCol = 0; expCol < expColumns.length; expCol++) {
                    if (currCell.getStringCellValue().trim().equalsIgnoreCase(expColumns[expCol].trim())) {
                        indexes.put(currCell.getStringCellValue().trim().toUpperCase(), actCol);
                        break;
                    }
                }
            }

            //Exit if collected all
            if (expColumns.length == indexes.size()) {
                break;
            }
        }

        return indexes;
    }

    private Map<String, Integer> getColumnIndexes() {
        //Get the actual columns and loop
        Cell currCell = null;
        Map<String, Integer> indexes = new HashMap<>();
        Row header = excelSheet.getRow(0);

        for (int actCol = 0; actCol < header.getPhysicalNumberOfCells(); actCol++) {
            currCell = header.getCell(actCol);

            //ADD INDEX
            if ((currCell.getCellType() != CellType.BLANK) && (currCell.getStringCellValue().length() != 0))
                indexes.put(currCell.getStringCellValue().trim().toUpperCase(), actCol);
        }

        return indexes;
    }

    /**
     * Get String data from given row and col
     *
     * @param row
     * @param col Added by bobby
     * @return
     */
    public String getCellData(int row, int col) {
        Cell cell = excelSheet.getRow(row).getCell(col);
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue();
    }

    /**
     * Get the table data into the hash map
     *
     * @param rowLength  the table length
     * @param colLength  the table width
     * @param identifier the key identifier for the map
     * @param withHeader true if header is skipped
     * @return
     */
    public Map<String, List<String>> getTableData(int rowLength, int colLength, int identifier, boolean withHeader) throws Throwable {
        int i = 0;
        if (withHeader) {
            i = 1;
        }
        if (excelSheet == null || excelBook == null) {
            connectExcelFile();
        }
        Map<String, List<String>> mapData = new HashMap<>();
        for (int r = i; r < rowLength; r++) {
            List<String> rowData = new ArrayList<>();
            for (int c = 0; c < colLength; c++) {
                rowData.add(getCellData(r, c));
            }
            mapData.put(rowData.get(identifier), rowData);
        }
        return mapData;
    }

    private Map<String, Integer> getColumnIndexes(List<String> columnNames) {
        //Get the column names
        String finalColumns = "";
        String[] expColumns = null;
        if (columnNames == null || columnNames.size() == 0) {
            expColumns = new String[]{"SCENARIO_ID"};
        } else {
            for (String column : columnNames) {
                finalColumns = finalColumns + column.trim().toUpperCase() + ";";
            }

            finalColumns = finalColumns.substring(0, finalColumns.length() - 1);
            expColumns = ("SCENARIO_ID;" + finalColumns).split(";");
        }

        //Get the actual columns and loop
        Cell currCell = null;
        Map<String, Integer> indexes = new HashMap<>();
        Row header = excelSheet.getRow(0);

        for (int actCol = 0; actCol < header.getPhysicalNumberOfCells(); actCol++) {
            currCell = header.getCell(actCol);

            //Check for expected columns
            for (int expCol = 0; expCol < expColumns.length; expCol++) {
                if (currCell.getStringCellValue().trim().equalsIgnoreCase(expColumns[expCol].trim())) {
                    indexes.put(currCell.getStringCellValue().trim().toUpperCase(), actCol);
                    break;
                }
            }

            //Exit if collected all
            if (expColumns.length == indexes.size()) {
                break;
            }
        }

        return indexes;
    }

    private String getCellValue(Cell cell) throws Throwable {
        String currCell = "";

        if (cell == null || cell.getCellType() == CellType.BLANK) {
            currCell = "";
        } else if (cell.getCellType() == CellType.NUMERIC) {
            if (((XSSFCell) cell).getRawValue().toLowerCase().contains("e"))
                currCell = String.format("%s", (new BigDecimal(((XSSFCell) cell).getRawValue())).longValue());
            else
                currCell = String.format("%s", ((XSSFCell) cell).getRawValue());
        } else if (cell.getCellType() == CellType.STRING) {
            currCell = cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.FORMULA) {
            if (cell.getCachedFormulaResultType() == CellType.NUMERIC) {
                if (((XSSFCell) cell).getRawValue().toLowerCase().contains("e"))
                    currCell = String.format("%s", (new BigDecimal(((XSSFCell) cell).getRawValue())).longValue());
                else
                    currCell = String.format("%s", ((XSSFCell) cell).getRawValue());

            } else
                currCell = cell.getRichStringCellValue().toString();
        }
        return currCell;
    }

    //Get Data
    public Map<String, String> getData() throws Throwable {
        List<Map<String, String>> temp;
        if(rowName==null)
            temp = getExcelData(false);
        else
            temp = getExcelData("SCENARIO_ID", rowName, false);
        if (temp.size() == 0)
            throw new Exception("Check the data file/sheet or file type. No data found for this scenario.");
        return temp.get(0);
    }

    public List<Map<String, String>> getAllData() throws Throwable {
        return getExcelData(true);
    }

    public List<Map<String, String>> getAllData(String lineNo) throws Throwable {
        rowName=lineNo;
        return getExcelData("SCENARIO_ID", rowName, true);
    }

    public List<Map<String, String>> getExcelData(String masterColName, String masterColVal, boolean allMatchedRows) throws Throwable {
        String cellData = "", currCell = "", cellHeader = "";
        int totalRows = 0, currScenarioDataNo = 0;
        Map<String, Integer> indexes = null;
        Map<String, String> data = new LinkedHashMap<>();
        List<Map<String, String>> allData = new LinkedList<>();

        try {
            //Get the Book and sheet
            connectExcelFile();
            indexes = getColumnIndexes("");

            //Get the rows count and loop to find scenario
            Row currentRow = null;
            Cell currentCell = null;

            for (int currRow = 1; currRow <= excelSheet.getPhysicalNumberOfRows(); currRow++) {
                //get the current cell data
                currentRow = excelSheet.getRow(currRow);
                if (currentRow == null) continue;
                currentCell = currentRow.getCell(indexes.get(masterColName));
                currCell = getCellValue(currentCell);

                //Find the scenario row
                if (currCell.trim().equalsIgnoreCase(masterColVal)) {
                    currScenarioDataNo += 1;

                    //Check if this is the row instance we required
                    if (currScenarioDataNo != scenarioDataID && !allMatchedRows)
                        continue;

                    //Collect the data
                    data = collectDataForRow(currRow);
                    allData.add(data);

                    if (!allMatchedRows) //Don't want to read all rows matched
                        break;
                } else { //if the scenario is different from what we are searching for, reset to 0
                    currScenarioDataNo = 0;
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            closeExcelFile();
        }

        //Assign
        SharedMemory.getInstance().excel = this;
        SharedMemory.getInstance().excelData = data;
        SharedMemory.getInstance().excelAllData = allData;
        if (allMatchedRows)
            SharedMemory.getInstance().excelAllDataInMap = allData;
        else
            SharedMemory.getInstance().excelAllDataInMap = new ArrayList<Map<String, String>>();

        //Return
        return allData;
    }

    private List<Map<String, String>> getExcelData(boolean allMatchedRows) throws Throwable {
        return getExcelData("SCENARIO_ID", currentScenarioID, allMatchedRows);
    }

    public Map<String, String> getDataFromMultipleID(String otherColumn, String expectedValue) throws Throwable {
        String cellData = "", currScenarioCell = "", cellHeader = "", currClaimTypeCell;
        int totalRows = 0, currScenarioDataNo = 0;
        Map<String, Integer> indexes = null;
        Map<String, String> data = null;

        try {
            //Get the Book and sheet
            connectExcelFile();
            indexes = getColumnIndexes(otherColumn);

            //Get the rows count and loop to find scenario
            Row currentRow = null;
            Cell currentScenarioCell = null;
            Cell currentClaimTypeCell = null;

            for (int currRow = 1; currRow <= excelSheet.getPhysicalNumberOfRows(); currRow++) {
                //get the current cell data
                currentRow = excelSheet.getRow(currRow);
                currentScenarioCell = currentRow.getCell(indexes.get("SCENARIO_ID"));
                currScenarioCell = getCellValue(currentScenarioCell);

                currentClaimTypeCell = currentRow.getCell(indexes.get(otherColumn));
                currClaimTypeCell = getCellValue(currentClaimTypeCell);


                //Find the scenario row
                if (currScenarioCell.trim().equalsIgnoreCase(currentScenarioID)) {
                    //Collect the data
                    if (currClaimTypeCell.trim().equalsIgnoreCase(expectedValue)) {
                        data = collectDataForRow(currRow);
                        break;
                    }
                    //  }
                } else { //if the scenario is different from what we are searching for, reset to 0
                    currScenarioDataNo = 0;
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            closeExcelFile();
        }

        //Return
        return data;
    }

    public Map<String, String> getFilteredData(String columnValuesToFilter) throws Throwable {
        return getFilteredData(columnValuesToFilter, null);
    }

    public Map<String, String> getFilteredData(String columnValuesToFilter, String requiredColumnsToFetch) throws Throwable {
        Map<String, String> data = new HashMap<String, String>();

        try {
            //Get the Book and sheet
            connectExcelFile();

            //Get the required row
            int reqRow = findRow(columnValuesToFilter);
            data = collectDataForRow(reqRow, requiredColumnsToFetch);

        } catch (Exception e) {
            throw e;
        } finally {
            closeExcelFile();
        }

        //Return
        SharedMemory.getInstance().excel = this;
        SharedMemory.getInstance().excelData = data;
        return data;
    }

    private int findRow(String columnValuesToFilter) throws Throwable {
        String[] filterByColumns = null, filterByValues = null;
        String cellData = "", currCell = "", cellHeader = "";
        int totalRows = 0, currScenarioDataNo = 0;
        Map<String, Integer> indexes = null;

        //Get the columns and values to filter
        filterByColumns = new String[columnValuesToFilter.split(";").length];
        filterByValues = new String[columnValuesToFilter.split(";").length];
        for (int fCol = 0; fCol < columnValuesToFilter.split(";").length; fCol++) {
            filterByColumns[fCol] = columnValuesToFilter.split(";")[fCol].split("=")[0].trim().toUpperCase();
            filterByValues[fCol] = (columnValuesToFilter.split(";")[fCol].split("=").length == 2) ? columnValuesToFilter.split(";")[fCol].split("=")[1] : "";
        }

        //Get the indexes
        indexes = getColumnIndexes();

        //Get the rows count and loop to find scenario
        Row currentRow = null;
        Cell currentCell = null;

        for (int currRow = 1; currRow <= excelSheet.getPhysicalNumberOfRows(); currRow++) {
            //Get current row
            currentRow = excelSheet.getRow(currRow);

            //Check for row
            for (int filter = 0; filter < filterByColumns.length; filter++) {
                currentCell = currentRow.getCell(indexes.get(filterByColumns[filter]));
                currCell = getCellValue(currentCell);

                //Find the scenario row
                if (currCell.trim().equalsIgnoreCase(filterByValues[filter]))
                    currScenarioDataNo += 1;

                //Check if we got the match
                if (currScenarioDataNo == filterByColumns.length)
                    return currRow;
            }
        }
        return -1;
    }

    private Map<String, String> collectDataForRow(int rowNo) throws Throwable {
        return collectDataForRow(rowNo, null);
    }

    private Map<String, String> collectDataForRow(int rowNo, String columnNames) throws Throwable {
        String cellData = "", cellHeader = "";
        Map<String, String> data = new HashMap<String, String>();

        //Collect the data
        Row currentRow = excelSheet.getRow(rowNo);
        if (columnNames == null) {
            Row headerRow = excelSheet.getRow(0);

            for (int col = 0; col < headerRow.getPhysicalNumberOfCells(); col++) {
                cellHeader = getCellValue(headerRow.getCell(col));
                cellData = getCellValue(currentRow.getCell(col));
                data.put(cellHeader, cellData);
            }
        } else {
            Cell tempCell = null;
            Map<String, Integer> indexes = null;
            indexes = getColumnIndexes(columnNames);

            for (int col = 0; col < columnNames.split(";").length; col++) {
                cellHeader = columnNames.split(";")[col].trim().toUpperCase();
                tempCell = currentRow.getCell(indexes.get(columnNames.split(";")[col].trim().toUpperCase()));
                cellData = getCellValue(tempCell).trim();
                data.put(cellHeader, cellData);
            }
        }
        return data;
    }

    private LinkedHashMap<String, String> collectDataForRowInOrder(int rowNo) throws Throwable {
        return collectDataForRowInOrder(rowNo, null);
    }

    private LinkedHashMap<String, String> collectDataForRowInOrder(int rowNo, String columnNames) throws Throwable {
        String cellData = "", currCell = "", cellHeader = "";
        Cell currentCell = null;
        LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();

        //Collect the data
        Row currentRow = excelSheet.getRow(rowNo);
        if (columnNames == null) {
            Row headerRow = excelSheet.getRow(0);

            for (int col = 0; col < headerRow.getPhysicalNumberOfCells(); col++) {
                cellHeader = getCellValue(headerRow.getCell(col));
                cellData = getCellValue(currentRow.getCell(col));
                data.put(cellHeader, cellData);
            }
        } else {
            Cell tempCell = null;
            Map<String, Integer> indexes = null;
            indexes = getColumnIndexes(columnNames);

            for (int col = 0; col < columnNames.split(";").length; col++) {
                cellHeader = columnNames.split(";")[col].trim().toUpperCase();
                tempCell = currentRow.getCell(indexes.get(columnNames.split(";")[col].trim().toUpperCase()));
                cellData = getCellValue(tempCell).trim();
                data.put(cellHeader, cellData);
            }
        }
        return data;
    }

    public Map<String, String> getFilteredDataInOrder(String columnValuesToFilter, String requiredColumnsToFetch) throws Throwable {
        Map<String, String> data = new HashMap<String, String>();

        try {
            //Get the Book and sheet
            connectExcelFile();

            //Get the required row
            int reqRow = findRow(columnValuesToFilter);
            data = collectDataForRowInOrder(reqRow, requiredColumnsToFetch);

        } catch (Exception e) {
            throw e;
        } finally {
            closeExcelFile();
        }

        //Return
        return data;
    }

    public Map<String, String> getFilteredDataInOrder(String columnValuesToFilter) throws Throwable {
        Map<String, String> data = new HashMap<String, String>();

        try {
            //Get the Book and sheet
            connectExcelFile();

            //Get the required row
            int reqRow = findRow(columnValuesToFilter);
            data = collectDataForRowInOrder(reqRow, null);

        } catch (Exception e) {
            throw e;
        } finally {
            closeExcelFile();
        }

        //Return
        return data;
    }

    public List<String> getData(String columnNames) throws Throwable {
        String cellData = "", currCell = "";
        int totalRows = 0, currScenarioDataNo = 0;
        Map<String, Integer> indexes = null;
        List<String> data = new ArrayList<>();

        //Prepare
        columnNames = columnNames.trim().toUpperCase();

        try {
            //Get the Book and sheet
            connectExcelFile();
            indexes = getColumnIndexes(columnNames);

            //Get the rows count and loop to find scenario
            Row currentRow = null;
            for (int currRow = 1; currRow <= excelSheet.getPhysicalNumberOfRows(); currRow++) {
                //get the current cell data
                currentRow = excelSheet.getRow(currRow);
                currCell = getCellValue(currentRow.getCell(indexes.get("SCENARIO_ID")));

                //Find the scenario row
                if (currCell.trim().equalsIgnoreCase(currentScenarioID)) {
                    currScenarioDataNo += 1;

                    //Check if this is the row instance we required
                    if (currScenarioDataNo != scenarioDataID)
                        continue;

                    //Collect the data
                    Cell tempCell = null;
                    for (int col = 0; col < columnNames.split(";").length; col++) {
                        tempCell = currentRow.getCell(indexes.get(columnNames.split(";")[col]));
                        cellData = getCellValue(tempCell).trim();
                        data.add(cellData);
                    }

                    break;
                } else { //if the scenario is different from what we are searching for, reset to 0
                    currScenarioDataNo = 0;
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            closeExcelFile();
        }

        //Return
        return data;
    }

    public List<String> getData(List<String> columnNames) throws Throwable {
        String cellData = "", currCell = "";
        int totalRows = 0, currScenarioDataNo = 0;
        Map<String, Integer> indexes = null;
        List<String> data = new ArrayList<>();


        try {
            //Get the Book and sheet
            connectExcelFile();
            indexes = getColumnIndexes(columnNames);

            //Get the rows count and loop to find scenario
            Row currentRow = null;
            Cell currentCell = null;
            for (int currRow = 1; currRow <= excelSheet.getPhysicalNumberOfRows(); currRow++) {
                //get the current cell data
                currentRow = excelSheet.getRow(currRow);
                currentCell = currentRow.getCell(indexes.get("SCENARIO_ID"));
                currCell = getCellValue(currentCell);

                //Find the scenario row
                if (currCell.trim().equalsIgnoreCase(currentScenarioID)) {
                    currScenarioDataNo += 1;

                    //Check if this is the row instance we required
                    if (currScenarioDataNo != scenarioDataID)
                        continue;

                    //Collect the data
                    Cell tempCell = null;
                    for (int col = 0; col < columnNames.size(); col++) {
                        tempCell = currentRow.getCell(indexes.get(columnNames.get(col)));

                        cellData = getCellValue(tempCell);
                        data.add(cellData);
                    }
                    break;
                } else { //if the scenario is different from what we are searching for, reset to 0
                    currScenarioDataNo = 0;
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            closeExcelFile();
        }

        //Return
        return data;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getMapping(String keyColumn, boolean IsDuplicateColumnsAvailable) throws Throwable {
        LinkedHashMap<String, LinkedHashMap<String, String>> wholeMap = new LinkedHashMap<String, LinkedHashMap<String, String>>();
        LinkedHashMap<String, String> currMap;

        try {
            //Get the Book and sheet
            connectExcelFile();

            //Get the rows count and loop to find scenario
            Row headerRow = excelSheet.getRow(0);
            for (int currRow = 1; currRow <= excelSheet.getPhysicalNumberOfRows(); currRow++) {
                currMap = new LinkedHashMap<String, String>();

                //get the current row data
                Row currentRow = excelSheet.getRow(currRow);
                if (currentRow == null || currentRow.toString().isEmpty()) continue;
                for (int currCell = 0; currCell < currentRow.getPhysicalNumberOfCells(); currCell++) {
                    Cell currentCell = currentRow.getCell(currCell);
                    if (((currentCell == null || currentCell.toString().isEmpty()) && currMap.isEmpty()) ||
                            headerRow.getCell(0).toString().isEmpty()) continue;
                    currMap.put(
                            getCellValue(headerRow.getCell(currCell)).trim().toUpperCase(),
                            getCellValue(currentCell));
                }

                //Add to master map
                if (currMap.isEmpty()) continue;

                String tempKey = currMap.get(keyColumn.toUpperCase()).trim().toUpperCase();
                if (IsDuplicateColumnsAvailable) {
                    int keySfx = ((int) wholeMap.entrySet().stream()
                            .filter(entry -> StringUtils.startsWith(entry.getKey(), tempKey))
                            .map(Map.Entry::getKey).count());
                    keySfx += 1;
                    wholeMap.put(tempKey + "_" + keySfx, currMap);
                } else
                    wholeMap.put(tempKey, currMap);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            closeExcelFile();
        }
        return wholeMap;
    }

    public LinkedList<LinkedHashMap<String, String>> getMappingList() throws Throwable {
        LinkedList<LinkedHashMap<String, String>> wholeList = new LinkedList<LinkedHashMap<String, String>>();
        LinkedHashMap<String, String> currMap;

        try {
            //Get the Book and sheet
            connectExcelFile();

            //Get the rows count and loop to find scenario
            Row headerRow = excelSheet.getRow(0);
            for (int currRow = 1; currRow <= excelSheet.getPhysicalNumberOfRows(); currRow++) {
                currMap = new LinkedHashMap<String, String>();

                //get the current row data
                Row currentRow = excelSheet.getRow(currRow);
                if (currentRow == null || currentRow.toString().isEmpty() ||
                        currentRow.getCell(0).getStringCellValue().isEmpty()) continue;

                for (int currCell = 0; currCell < currentRow.getPhysicalNumberOfCells(); currCell++) {
                    Cell currentCell = currentRow.getCell(currCell);
                    if (((currentCell == null || currentCell.toString().isEmpty()) && currMap.isEmpty()) ||
                            headerRow.getCell(0).toString().isEmpty()) continue;
                    currMap.put(
                            getCellValue(headerRow.getCell(currCell)).trim().toUpperCase(),
                            getCellValue(currentCell));
                }

                //Add to master list
                wholeList.add(currMap);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            closeExcelFile();
        }
        return wholeList;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getMapping(
            String keyColumn,
            boolean IsDuplicateColumnsAvailable,
            String fromKeyColumnValue,
            String untilKeyColumnValue) throws Throwable {
        LinkedHashMap<String, LinkedHashMap<String, String>> wholeMap = new LinkedHashMap<String, LinkedHashMap<String, String>>();
        LinkedHashMap<String, String> currMap;

        try {
            //Get the Book and sheet
            connectExcelFile();

            //Get the rows count and loop to find scenario
            boolean fromFound = false;
            Row headerRow = excelSheet.getRow(0);
            for (int currRow = 1; currRow <= excelSheet.getPhysicalNumberOfRows(); currRow++) {
                currMap = new LinkedHashMap<String, String>();

                //get the current row data
                Row currentRow = excelSheet.getRow(currRow);
                if (currentRow == null || currentRow.toString().isEmpty()) continue;
                if (!fromFound) {
                    if (currentRow.getCell(0).getStringCellValue().equalsIgnoreCase(fromKeyColumnValue))
                        fromFound = true;
                    else continue;
                }
                if (currentRow.getCell(0).getStringCellValue().equalsIgnoreCase(untilKeyColumnValue))
                    break;

                for (int currCell = 0; currCell < currentRow.getPhysicalNumberOfCells(); currCell++) {
                    Cell currentCell = currentRow.getCell(currCell);
                    if (((currentCell == null || currentCell.toString().isEmpty()) && currMap.isEmpty()) ||
                            headerRow.getCell(0).toString().isEmpty()) continue;
                    currMap.put(
                            getCellValue(headerRow.getCell(currCell)).trim().toUpperCase(),
                            getCellValue(currentCell));
                }

                //Add to master map
                if (currMap.isEmpty()) continue;

                String tempKey = currMap.get(keyColumn.toUpperCase()).trim().toUpperCase();
                if (IsDuplicateColumnsAvailable) {
                    int keySfx = ((int) wholeMap.entrySet().stream()
                            .filter(entry -> StringUtils.startsWith(entry.getKey(), tempKey))
                            .map(Map.Entry::getKey).count());
                    keySfx += 1;
                    wholeMap.put(tempKey + "_" + keySfx, currMap);
                } else
                    wholeMap.put(tempKey, currMap);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            closeExcelFile();
        }
        return wholeMap;
    }

    /**
     * To collect the values from already read map for given list of columns
     * @param columnNames
     * @return
     * @throws Throwable
     */
    public String[] collectFilteredData(Map<String, String> data, String columnNames) throws Throwable {
        LinkedList<String> filteredData = new LinkedList<>();

        String[] columns = columnNames.split(";");
        String temp;
        for (String col : columns) {
            if (data.containsKey(col))
                temp = data.get(col);
            else if (col.matches("[AL|AC|TS1|TS2|SQ|QC]+_.*"))  //if excel column name, get it
                temp = "";
            else temp = col; //else, carry the direct value

            filteredData.add(temp);
        }

        return filteredData.toArray(new String[filteredData.size()]);
    }

    //Additional Validation for grace period
    public Map<String, String> getGracePeriodFileValues() throws Throwable {
        Map<String, String> line = new LinkedHashMap<>();

        try {
            connectExcelFile();

            for(int currRow = 0; currRow <= excelSheet.getPhysicalNumberOfRows(); currRow++){
                if(currRow != 0) {
                    Row currentRow = excelSheet.getRow(currRow);
                    line.put(currentRow.getCell(0).getStringCellValue(), currentRow.getCell(1).getStringCellValue());
                }

            }

        } catch (NullPointerException e) {
            logger.error ("Cell has no value", e);

        } finally {
            closeExcelFile();
        }

        return line;
    }

}
