package util.endToEnd.AdditionalUtilsEndtoEnd;

import com.uhc.aarp.quality.core.utilities.DatabaseUtils;
import com.uhc.aarp.quality.core.utility.PropertyUtils;
import org.slf4j.LoggerFactory;
import util.ScrumTesting.HttpUtils;
import util.endToEnd.SharedMemory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Modified by abharga4 on 11/9/2017.
 */
public class EDI_DB {
    public static org.slf4j.Logger logger = LoggerFactory.getLogger(EDI_DB.class.getName());

    public DatabaseUtils dbUtils;
    public static Connection connection;
    public static ResultSet resultSet;
    private String databaseType = "", hostname = "", databaseNm = "", userName = "", password = "";
    int port = 1433;

    public EDI_DB(){}

    private Connection connectToDB() throws Exception {
        String ediEnv = PropertyUtils.getProperty("connectedEDIEnv").trim().split("_")[1];

        hostname = PropertyUtils.getProperty(ediEnv + "_hostnameDB").trim();
        databaseType = "SQL";
        databaseNm = PropertyUtils.getProperty(ediEnv + "_databasename").trim();
        userName = PropertyUtils.getProperty(ediEnv + "_usernameDB").trim();
        password = PropertyUtils.getProperty(ediEnv + "_passwordDB").trim();
        port = Integer.parseInt(PropertyUtils.getProperty(ediEnv + "_portDB").trim());

        //Connect
        connection = dbUtils.getDatabaseConnection(databaseType, hostname, databaseNm, userName, password, port);
        return connection;
    }

    public ResultSet executeAndGetResult(String query) throws Exception {
        connectToDB();
        Statement statement = connection.createStatement();
        resultSet = statement.executeQuery(query);

        return resultSet;
    }

    private void closeDBConnection() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }

            if (connection != null) {
                connection.close();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public String[] getTrackingID(String query) throws Exception {
        logger.debug("Connecting to EDIFECS Sql Server - fetching tracking ID...");

        String[] dbRes = new String[30];
        boolean cond = false, exists = true;

        try {
            connectToDB();

            int count = 0;
            do {
                Thread.sleep(5000);
                count++;

                executeAndGetResult(query);
                if (resultSet.next())
                    cond = (resultSet.getInt(2) == 0 ||
                            (!SharedMemory.getInstance().isMRR && resultSet.getInt(2) == 1) ||
                            (!SharedMemory.getInstance().isMRR && resultSet.getInt(3) <= 8));
                else exists = false;
            } while (((!exists) || cond) && count <= 12);

            if (!resultSet.getString(1).isEmpty()) {
                dbRes[0] = resultSet.getString(1);
            } else dbRes[0] = resultSet.getString(1);

            dbRes[1] = resultSet.getString(2);
            dbRes[2] = resultSet.getString(3);
        } catch(Exception e){
            e.printStackTrace();
        } finally{
            closeDBConnection();
        }

        return dbRes;
    }

    public String getFileNameForTrackingID(String trackingID) throws Throwable {
        String fileNameForTrackingID = "";

        connectToDB();

        //Execute Query
        String query = File837Ops.getQueryToGetFileNameForTrackingID(trackingID);
        executeAndGetResult(query);
        while (resultSet.next()) {
            fileNameForTrackingID = resultSet.getString(1);
            return fileNameForTrackingID;
        }

        return "";
    }

    public int randomControlNumber() {
        logger.debug("Random Control Number generator");
        int randomInt = 0, count = 0;

        try {
            do {
                while(Integer.toString(randomInt).length() < 9)
                    randomInt = new Random().nextInt(1000000000);

                String query = "select interchangecontrolnumber from interchange where interchangecontrolnumber in ('" + randomInt + "')";

                connectToDB();
                executeAndGetResult(query);

                count++;
            } while (resultSet.next() && count < 10);
        } catch(Exception e) { e.printStackTrace(); }
        finally {
            closeDBConnection();
        }
        return randomInt;
    }

    public List<String> getEDIFECSAttachmentFileNames(String fileName) throws Exception {
        List<String> attachments = new ArrayList<>();

        String query = "declare @filename nvarchar(1000) \n" +
                "set @filename = '" + fileName + "' \n" +
                "SELECT NAME FROM CLAIMDATA CD INNER JOIN CLAIM C ON CD.CLAIMSID = C.CLAIMSID \n" +
                "AND C.TRANSMISSIONTID = \n" +
                "(SELECT TOP 1 TRACKINGIDENTIFIER FROM TRANSMISSION \n" +
                "WHERE \n" +
                  "TRANSMISSIONFILENAME LIKE @filename + '%' AND \n" +
                "TRANSMISSIONFILENAME NOT LIKE @filename + '%.999' AND TRANSMISSIONFILENAME NOT LIKE @filename + '%.277' \n" +
                "ORDER BY lastmoddt DESC);";

        connectToDB();
        executeAndGetResult(query);
        while (resultSet.next())
            attachments.add(resultSet.getString("name"));

        return attachments;
    }

    public String downloadAttachmentFromTM(String baseUri, String trackingID, String attachmentFileName) throws Exception {
       try {
           return new HttpUtils().executePostTMAttachment(baseUri, trackingID, attachmentFileName);
       }catch(NullPointerException npe)
       {
           throw new Exception("No member validation response for this claim from Fox. Check the details in TM and rerun.");
       }
    }
}
