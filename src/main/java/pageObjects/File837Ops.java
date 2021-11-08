package util.endToEnd.AdditionalUtilsEndtoEnd;

import com.uhc.aarp.quality.core.utilities.DatabaseUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Random;

public class File837Ops {

public DatabaseUtils dbUtils;

    public ExitCode randomControlNumber(String databaseType,String hostname,String databaseNm,String userName,String password,int port) throws Exception{

        int res=0;
        int randomInt=0;
        int count=0;
        String query;
        Statement st = null;
        ResultSet rs = null;
        Random randomGenerator = new Random();
        Connection con1 = null;


        try {
            con1 = dbUtils.getDatabaseConnection(databaseType, hostname, databaseNm, userName, password, port);
            while (res == 0) {
                randomInt = randomGenerator.nextInt(1000000000);
                query = "select interchangecontrolnumber from interchange where interchangecontrolnumber in ('" + randomInt + "')";
                st = con1.createStatement();
                rs = st.executeQuery(query);

                boolean empty = true;
                while( rs.next() ) {
                    empty = false;
                }

                if( empty ) res=1;
            }
            return new ExitCode("Success", randomInt, 0);

        }catch(Exception e){

            return new ExitCode(e.getMessage(),0, 0);
        }finally{
            if(rs != null) rs.close();
            if(st != null) st = null;
            if(con1 != null) con1.close();
        }

    }

    public ExitCode generateControlNumberWithTS(String databaseType,String hostname,String databaseNm,String userName,String password,int port) throws Exception{

        int res=0;
        String ts="";
        String query;
        Statement st = null;
        ResultSet rs = null;
        Connection con1 = null;


        try {
            con1 = dbUtils.getDatabaseConnection(databaseType, hostname, databaseNm, userName, password, port);
            while (res == 0) {
                ts = "000" + String.format("%02d", Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
                        //+ String.format("%02d", Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
                        + String.format("%02d", Calendar.getInstance().get(Calendar.MILLISECOND));
                query = "select interchangecontrolnumber from interchange where interchangecontrolnumber in ('" + ts + "')";
                st = con1.createStatement();
                rs = st.executeQuery(query);

                boolean empty = true;
                while( rs.next() ) {
                    empty = false;
                }

                if( empty ) res=1;
            }
            return new ExitCode("Success", ts, 0);

        }catch(Exception e){
            return new ExitCode(e.getMessage(),0, 1);
        }finally{
            if(rs != null) rs.close();
            if(st != null) st = null;
            if(con1 != null) con1.close();
        }

    }

    public static String getQuery(String fileName){
        fileName = fileName.replace(".txt", "").replace(".TXT", "");
        String query = "SELECT TOP 1 TRACKINGIDENTIFIER, DISPOSITIONID, ACTIVITYSTATEID FROM TRANSMISSION WHERE " +
                "TRANSMISSIONFILENAME LIKE '" + fileName + "%' " +
                "AND TRANSMISSIONFILENAME NOT LIKE '%.999' " +
                "AND TRANSMISSIONFILENAME NOT LIKE '%.277' " +
                "ORDER BY lastmoddt DESC;";

        String oldQuery =
        "/* Declared variables */\n" +
        "DECLARE @FileName NVARCHAR(100)\n" +
        "       ,@277Name NVARCHAR(100)\n" +
        "       ,@999Name NVARCHAR(100)\n" +
        "       ,@SID NVARCHAR(50)\n" +
        "        ,@SNID NVARCHAR(50)\n" +
        "        ,@STID NVARCHAR(50)\n" +
        "        ,@MaxTDT datetime\n" +
        "        ,@MaxTTDT datetime\n" +
        "        ,@MaxTNDT datetime\n" +
        "        ,@MaxTID NVARCHAR(50)\n" +
        "        ,@MaxTTID NVARCHAR(50)\n" +
        "        ,@MaxTNID NVARCHAR(50)\n" +

"/* user setup, set file name. Results will be the 837, 999 and 277 counts for the latest time that file has been dropped */\n" +
         "       --select Max(trackingidentifier) from transmission where transmissionfilename = 'Inst_SNIP7_SmokeTest_v2.277'\n" +
                "SET @FileName = '" + fileName +  "'" + " --EXAMPLE USE WOULD BE 'SmokeTest_837P_55kErrors.txt', Inst_SNIP7_SmokeTest_v2.txt\n" +
"/* DONT CHANGE ANYTHING BELOW HERE*/\n" +
        "SET @277Name = replace(replace(@FileName, '.txt', '.277'), '.dat', '.277')\n" +
        "SET @999Name = replace(replace(@FileName, '.txt', '.999'), '.dat', '.999')\n" +
        "                                                                         \n" +
        "SELECT @MaxTDT = max(receiptdt)\n" +
        "FROM transmission\n" +
        "WHERE transmissionfilename = @FileName -- TID = tracking id (input file)\n" +
        "                                \n" +
        "SELECT @MaxTTDT = max(receiptdt)\n" +
        "FROM transmission\n" +
        "WHERE transmissionfilename = @277Name -- TTID = (T)wo seven seven file  id\n" +
        "and receiptdt between  @MaxTDT and GETDATE() --and it should be generated between the 837 and present to avoid previous files of the same name\n" +
        "                                \n" +
        "--select @MaxTTDT as '@MaxTTDT'\n" +
        "                                   \n" +
        "SELECT @MaxTNDT = max(receiptdt)\n" +
        "FROM transmission\n" +
        "WHERE transmissionfilename = @999Name -- TNID = (N)ine nine nine file id\n" +
        "and receiptdt between  @MaxTDT and GETDATE() --and it should be generated between the 837 and present to avoid previous files of the same name\n" +
        "                                \n" +
        "                                \n" +
        "SELECT @MaxTID = max(trackingidentifier)\n" +
        "FROM transmission\n" +
        "WHERE transmissionfilename = @FileName  -- TID = tracking id (input file)\n" +
        "and receiptdt = @MaxTDT\n" +
        "                        \n" +
        "        SELECT @MaxTTID = max(trackingidentifier)\n" +
        "FROM transmission\n" +
        "WHERE transmissionfilename = @277Name -- TTID = (T)wo seven seven file  id\n" +
        "and receiptdt = @MaxTTDT\n" +
        "                        \n" +
        "        SELECT @MaxTNID = max(trackingidentifier)\n" +
        "FROM transmission\n" +
        "WHERE transmissionfilename = @999Name -- TNID = (N)ine nine nine file id\n" +
        "and receiptdt = @MaxTNDT\n" +
        "                        \n" +
        "        SELECT @SID = transmissionsid\n" +
        "FROM transmission\n" +
        "WHERE trackingidentifier = @MaxTID -- SID = transmissionSid\n" +
        "                              \n" +
        "SELECT @STID = transmissionsid\n" +
        "FROM transmission\n" +
        "WHERE trackingidentifier = @MaxTTID -- STID = TransmisionSid for (T)wo seven seven file\n" +
        "                       \n" +
        "SELECT @SNID = transmissionsid\n" +
        "FROM transmission\n" +
        "WHERE trackingidentifier = @MaxTNID -- SNID =  TransmisionSid for (N)ine nine nine file\n" +
        "                        \n" +
        "SELECT t.transmissionfilename AS [File Name: 837]  --Name of the 837 file\n" +
        "        ,case when @MaxTTDT is null then '' else [File Name: 999] end as [File Name: 999]--Name of the 999 File, if the date is null, populate blank since its hard coded\n" +
        "        ,case when @MaxTNDT is null then '' else [File Name: 277] end as [File Name: 277] -- Name of the 277 File, populate blank since its hard coded\n" +
        "        ,@MaxTID as [TrackingID: 837]\n" +
		"		,@MaxTNID as [TrackingID: 999]\n" +
		"		,@MaxTTID as [TrackingID: 277]\n" +
        "        ,i.interchangecontrolnumber AS [ICN: 837] --Interchange control number of the 837\n" +
        "        ,[ICN: 277] --Interchange control number of the 277\n" +
        "        ,[ICN: 999] --Interchange control number of the 999\n" +
        "        ,datediff(s, t.receiptdt, t.activitystatemoddt) AS [Processing Time (Seconds)] --time between the receipt date time and the last activity staet mod date time\n" +
        "        ,i.interchangesenderidentifier AS [Sender]  --sender on the 837\n" +
        "        ,pi.NAME AS [Trading Partner Name] --Trading partner name\n" +
        "        ,t.receiptdt AS [Receipt DateTime] --Receipt Date Time\n" +
        "        ,t.activitystatemoddt AS [Complete DateTime]  --The last actvity state mod date\n" +
        "        ,[Count of claimProcessing: Total] --Count of CLM segments in the 837 file\n" +
        "        ,isnull([Count of claimProcessing: Accepted], 0) AS [Count of claimProcessing: Accepted]  --Count of CLM segments in the 837 file with an accepted disposition id, for all see:  select * from enclmdisposition\n" +
        "        ,isnull([Count of claimProcessing: Rejected], 0) AS [Count of claimProcessing: Rejected] --Count of CLM segments in the 837 file with an rejected disposition id, for all see:  select * from enclmdisposition\n" +
        "        ,[Count of File Attachments]  --count of attachments with a type id for the transmission data file (837, 277, 999 files), for all statuses see: select * from enattachmenttype\n" +
        "        ,CASE\n" +
        "WHEN isnull([Count of claimProcessing: Rejected], 0) = 0\n" +
        "AND [Count of File Attachments] = 2\n" +
        "THEN '2 of 2 files exist: PASS'   --If all claimProcessing were accepted then we do not get a 277CA so we should only have 2 attachments\n" +
        "WHEN isnull([Count of claimProcessing: Rejected], 0) > 0\n" +
        "AND [Count of File Attachments] = 3  --if 1 or more searchAndProcessing were rejected we should ahve 3 attachments since the 277CA was created\n" +
        "THEN '3 of 3 files exist: PASS'\n" +
        "ELSE 'A differnet number of attachments was expected (FAIL)'\n" +
        "END AS [Attachments Loaded]\n" +
        "        ,CASE\n" +
        "WHEN [Count of claimProcessing: Total] = fga.transactionsincluded\n" +
        "THEN '[Count of claimProcessing: Total] == [Count: 999 Total] (PASS)'\n" +
        "ELSE '[Count of claimProcessing: Total] == [Count: 999 Total] (FAIL)'\n" +
        "END AS [999 Total Check]  --Checking the total count of CLM segments on the 837 file and comparing it to the count on the GE segment of the 999\n" +
        "        ,CASE\n" +
        "WHEN isnull([Count of claimProcessing: Accepted], 0) = isnull(fga.transactionsaccepted, 0)\n" +
        "THEN '[Count of claimProcessing: Total] = [Count: 999 Accepted] (PASS)'\n" +
        "ELSE '[Count of claimProcessing: Total] != [Count: 999 Accepted] (FAIL)'\n" +
        "END AS [999 Accepted Check]  --Checking the Total CLM segments in the 837 with an accepted disposition against the total segments in the 999 that were accepted\n" +
        "        ,fga.transactionsincluded as [Count: 999 Total]  --Count of searchAndProcessing acknowledged in the 999\n" +
        "        ,fga.transactionsaccepted AS [Count: 999 Accepted]  --Count of searchAndProcessing with an accepted disposition from the 999\n" +
        "        ,CASE\n" +
        "WHEN isnull([Count of claimProcessing: Rejected], 0) = isnull(tssCount.rejectCount, 0)\n" +
        "THEN '[Count of claimProcessing: Rejected] = [Count: 277] (PASS)'\n" +
        "ELSE '[Count of claimProcessing: Rejected] != [Count: 277] (FAIL)'\n" +
        "END AS [277 Accept Check]  --Compare the count of CLM segments from the 837 file with a rejected disposition to the count of searchAndProcessing acknowleged in the 277CA\n" +
        "        ,tssCount.rejectCount AS [Count: 277] --Count of searchAndProcessing acknowledged in the 277CA\n" +
        "        ,CASE\n" +
        "WHEN isnull(tssCount.rejectCount, 0) + isnull(fga.transactionsaccepted, 0) = [Count of claimProcessing: Total]\n" +
        "THEN '[Count: 277] + [Count: 999 Accepted] == [Count of claimProcessing: Total] (PASS)'\n" +
        "ELSE '[Count: 277] + [Count: 999 Accepted] != [Count of claimProcessing: Total] (FAIL)'\n" +
        "END AS [All claimProcessing Acknowledged]  --checking that the 277 count + searchAndProcessing accepted on the 999 matches the total searchAndProcessing on the 837 inbound file.\n" +
        "        ,CASE\n" +
        "WHEN [Count of Successful Events: Claim] = 7\n" +
        "THEN '[Count of Successful Events: Claim] == 7 (PASS)'\n" +
        "ELSE '[Count of Successful Events: Claim] == ' + cast([Count of Successful Events: Claim] AS NVARCHAR(10)) + ' (FAIL)'\n" +
        "END AS [Successful Processing: Claim]  --checks that 7 successful events were create for the 837 file\n" +
        "        ,CASE\n" +
        "WHEN [Count of Successful Events: 999] = 6\n" +
        "THEN '[Count of Successful Events: 999] == 6 (PASS)'\n" +
        "ELSE '[Count of Successful Events: 999] == ' + cast([Count of Successful Events: 999] AS NVARCHAR(10)) + ' (FAIL)'\n" +
        "END AS [Successful Processing: 999]  --checks that 6 successful events were created for the 999 file\n" +
        "        ,CASE\n" +
        "WHEN [Count of Successful Events: 277] = 6\n" +
        "THEN '[Count of Successful Events: 277] == 6 (PASS)'\n" +
        "ELSE '[Count of Successful Events: 277] == ' + cast([Count of Successful Events: 277] AS NVARCHAR(10)) + ' (FAIL)'\n" +
        "END AS [Successful Processing: 277]  --checks that 6 successful events were created for the 277 file\n" +
        "        ,[Count of Successful Events: Claim]  --The count of successful events from the 837 file, , FOR FULL LIST:  select * from eneventsituationcategory\n" +
        "        ,[Count of Successful Events: 999]  --The count of successful events from the 999 file., FOR FULL LIST:  select * from eneventsituationcategory\n" +
        "        ,[Count of Successful Events: 277] --the count of successful events from the 277 file, FOR FULL LIST:  select * from eneventsituationcategory\n" +
        "        ,td.caption AS [Transmission Disposition] --verbal description of the disposition of the 837 file\n" +
        "        ,tas.caption AS [Transmission Activity State]  --verbal description of the activity state of the 837 file\n" +
        "        ,[999 Disposition] --verbal description of the disposition of the 999 file\n" +
        "        ,[999 Activity State]--verbal description of the activity state of the 999 file\n" +
		"		,[277 Disposition] --verbal description of the disposition of the 277 file\n" +
        "        ,[277 Activity State]--verbal description of the activity state of the 277 file\n" +
        "        ,[AttachmentName: 837]\n" +
        "        ,[AttachmentName: 999]\n" +
        "        ,[AttachmentName: 277]\n" +
        "FROM transmission t\n" +
        "INNER JOIN entransmactivitystate tas ON tas.enumid = activitystateid  -- join is for getting the transmission activity state of the 837\n" +
        "INNER JOIN entransmdisposition td ON td.enumid = dispositionid  --join is for getting the transmision disposition of the 837\n" +
        "INNER JOIN interchange i ON i.transmissiontid = t.trackingidentifier  --used to get functional group counts\n" +
        "INNER JOIN (  -- join on sub query gives us the total count of searchAndProcessing for the 837\n" +
        "SELECT count(1) AS [Count of claimProcessing: Total]\n" +
        "                        ,transmissiontid\n" +
        "FROM [claim]\n" +
        "WHERE transmissiontid = @MaxTID\n" +
        "        GROUP BY transmissiontid\n" +
        "        ) ClaimCounts ON ClaimCounts.transmissiontid = t.trackingidentifier\n" +
        "INNER JOIN ( -- join on sub query gives us the count of successful events on the 837\n" +
        "SELECT transmissionsid\n" +
        "                        ,count(1) AS [Count of Successful Events: Claim]\n" +
        "FROM [transmissionevent]\n" +
        "WHERE transmissionsid = @SID\n" +
        "        GROUP BY transmissionsid\n" +
        "        ) tevents ON tevents.transmissionsid = @SID\n" +
        "        left JOIN ( -- join on sub query gives us the 277 details, left join 277 since its not always present\n" +
        "        SELECT @MaxTID AS [JoinID]\n" +
        "        ,t.transmissionfilename AS [File Name: 277]\n" +
        "                        ,i.interchangecontrolnumber AS [ICN: 277]\n" +
        "                        ,td.caption AS [277 Disposition]\n" +
        "                        ,tas.caption AS [277 Activity State]\n" +
        "                        ,[Count of Successful Events: 277]\n" +
        "FROM transmission t\n" +
        "INNER JOIN entransmactivitystate tas ON tas.enumid = activitystateid\n" +
        "INNER JOIN entransmdisposition td ON td.enumid = dispositionid\n" +
        "INNER JOIN interchange i ON i.transmissiontid = t.trackingidentifier\n" +
        "INNER JOIN (\n" +
        "        SELECT transmissionsid\n" +
        "        ,count(1) AS [Count of Successful Events: 277]\n" +
        "FROM [transmissionevent]\n" +
        "WHERE transmissionsid = @STID\n" +
        "        AND eventsituationdispositionid IN (\n" +
        "        1\n" +
        "        ,2\n" +
        "        ,7\n" +
        "                                                        ) -- 1 = start, 2 = stop, 7 = complete, FOR FULL LIST:  select * from eneventsituationcategory\n" +
        "GROUP BY transmissionsid\n" +
        "                        ) ttevents ON ttevents.transmissionsid = @STID\n" +
        "        WHERE t.trackingidentifier = @MaxTTID\n" +
        "        ) twosvnsvn ON twosvnsvn.JoinID = t.trackingidentifier\n" +
        "INNER JOIN ( -- join on sub query gives us the 999 details,\n" +
        "        SELECT @MaxTID AS [JoinID]\n" +
        "        ,t.transmissionfilename AS [File Name: 999]\n" +
        "                        ,i.interchangecontrolnumber AS [ICN: 999]\n" +
        "                        ,td.caption AS [999 Disposition]\n" +
        "                        ,tas.caption AS [999 Activity State]\n" +
        "                        ,[Count of Successful Events: 999]\n" +
        "FROM transmission t\n" +
        "INNER JOIN entransmactivitystate tas ON tas.enumid = activitystateid\n" +
        "INNER JOIN entransmdisposition td ON td.enumid = dispositionid\n" +
        "INNER JOIN interchange i ON i.transmissiontid = t.trackingidentifier\n" +
        "INNER JOIN (\n" +
        "        -- gets us the number of events with a successful status on the 999\n" +
        "SELECT transmissionsid\n" +
        "                                        ,isnull(count(1), 0) AS [Count of Successful Events: 999]\n" +
        "FROM [transmissionevent]\n" +
        "WHERE transmissionsid = @SNID\n" +
        "        AND eventsituationdispositionid IN (\n" +
        "        1\n" +
        "        ,2\n" +
        "        ,7\n" +
        "                                                        ) -- 1 = start, 2 = stop, 7 = complete , FOR FULL LIST:  select * from eneventsituationcategory\n" +
        "GROUP BY transmissionsid\n" +
        "                        ) tnevents ON tnevents.transmissionsid = @SNID\n" +
        "        WHERE t.trackingidentifier = @MaxTNID\n" +
        "        ) nnn ON nnn.joinid = t.trackingidentifier\n" +
        "                                                    \n" +
        "LEFT JOIN ( --Adding good searchAndProcessing count and Bad searchAndProcessing count\n" +
        "        -- gets us the # of accepted searchAndProcessing on the 837 file\n" +
        "SELECT isnull(count(1), 0) AS [Count of claimProcessing: Accepted]\n" +
        "                        ,transmissiontid\n" +
        "FROM [claim]\n" +
        "WHERE transmissiontid = @MaxTID\n" +
        "        AND dispositionid = 5 --5 is for accepted\n" +
        "GROUP BY transmissiontid\n" +
        "        ) ClaimCountsGood ON ClaimCountsGood.transmissiontid = t.trackingidentifier\n" +
        "LEFT JOIN ( -- gets us the # of rejected searchAndProcessing on the 837 file\n" +
        "SELECT isnull(count(1), 0) AS [Count of claimProcessing: Rejected]\n" +
        "                        ,transmissiontid\n" +
        "FROM [claim]\n" +
        "WHERE transmissiontid = @MaxTID\n" +
        "        AND dispositionid = 3 --3 is for rejected\n" +
        "GROUP BY transmissiontid\n" +
        "        ) BadCountsGood ON BadCountsGood.transmissiontid = t.trackingidentifier\n" +
        "INNER JOIN functionalgroupack fga ON fga.transmissiontid = @MaxTNID  --Provides the functional group info from the 999 file\n" +
        "LEFT JOIN ( --gets us a count of the rejects on the 277 file\n" +
        "SELECT transmissiontid\n" +
        "                        ,isnull(count(DISTINCT payerclaimcontrolnumber), 0) AS [rejectCount]\n" +
        "FROM claimack\n" +
        "WHERE transmissiontid = @MaxTTID\n" +
        "        GROUP BY transmissiontid\n" +
        "        ) tssCount ON tssCount.transmissiontid = @MaxTTID\n" +
        "        LEFT JOIN (\n" +
        "        --Join is to get us the count of attachments of type Transmission Data File (the 837/999/277CA files) so we can confirm the correct number of files were made\n" +
        "        SELECT @MaxTID AS [attjoin]\n" +
        "        ,count(1) AS [Count of File Attachments]\n" +
        "FROM transmissiondata\n" +
        "WHERE transmissionsid IN (\n" +
        "@SID\n" +
        "                                        ,@STID\n" +
        "                                        ,@SNID\n" +
        "                                        )\n" +
        "AND attachmenttypeid = 1  --FOR all options see: select * from enattachmenttype\n" +
        "        ) attCount ON attCount.[attjoin] = @MaxTID\n" +
        "LEFT JOIN (\n" +
        "        --Join is to get us the Attachment name of the: 837\n" +
        "SELECT @MaxTID AS [attjoin]\n" +
        "                        ,NAME AS [AttachmentName: 837]\n" +
        "FROM transmissiondata\n" +
        "WHERE transmissionsid IN (@SID)\n" +
        "AND attachmenttypeid = 1 --AttachmentTypeID is the actual data file\n" +
        "        ) attCountEN ON attCountEN.[attjoin] = @MaxTID\n" +
        "LEFT JOIN (\n" +
        "        --Join is to get us the Attachment name of the: 999\n" +
        "SELECT @MaxTID AS [attjoin]\n" +
        "                        ,NAME AS [AttachmentName: 999]\n" +
        "FROM transmissiondata\n" +
        "WHERE transmissionsid IN (@SNID)\n" +
        "AND attachmenttypeid = 1 --AttachmentTypeID is the actual data file\n" +
        "        ) attCountNN ON attCountNN.[attjoin] = @MaxTID\n" +
        "LEFT JOIN (\n" +
        "        --Join is to get us the Attachment name of the: 277\n" +
        "SELECT @MaxTID AS [attjoin]\n" +
        "                        ,NAME AS [AttachmentName: 277]\n" +
        "FROM transmissiondata\n" +
        "WHERE transmissionsid IN (@STID)\n" +
        "AND attachmenttypeid = 1 --AttachmentTypeID is the actual data file\n" +
        "        ) attCountTN ON attCountTN.[attjoin] = @MaxTID\n" +
        "INNER JOIN [partyinfo] pi --Join is to get us the [Trading Partner Name]\n" +
        "ON pi.partysid = t.tradingpartnersid\n" +
        "WHERE t.trackingidentifier = @MaxTID\n" +
        "        ORDER BY t.receiptdt DESC";

        return query;
    }

    public static String getQueryToGetFileNameForTrackingID(String trackingID) throws Throwable {
        String query = "SELECT TRANSMISSIONFILENAME FROM TRANSMISSION WHERE TRACKINGIDENTIFIER = '" + trackingID + "'";
        return query;
    }

    public static String getQueryToValidateClaims(String trackingID) throws Throwable {
        String query = "SELECT CLAIMSID FROM CLAIM WHERE TRACKINGIDENTIFIER = '" + trackingID + "'";
        return query;
    }

    public static String getQueryToValidateClaimIsNotExisting(String membershipNumber, String serviceDate ) throws Throwable {
        String query = "SELECT CLAIMSID FROM CLAIM WHERE UHCFOXSUBSCRIBERID = '" + membershipNumber + "' AND SERVICEBEGINDATE = '" + serviceDate + "'";
        return query;
    }

    public static String getQueryToValidateRxClaims(String trackingID) throws Throwable {
        String query = "SELECT UHCORXCLAIMSID FROM UHCORXCLAIM WHERE TRACKINGIDENTIFIER = '" + trackingID + "'";
        return query;
    }
}
