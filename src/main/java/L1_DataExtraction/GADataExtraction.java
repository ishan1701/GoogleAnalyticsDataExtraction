package L1_DataExtraction;

import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;

import java.util.List;

import static L1_DataExtraction.Utilities.getReport;
import static L1_DataExtraction.Utilities.initializeAnalyticsReporting;
import static L1_DataExtraction.Utilities.getResponse;

import static L1_DataExtraction.Constants.*;

public class GADataExtraction extends Thread{
    public static void main(String[] args) {
        try {
            AnalyticsReporting service = initializeAnalyticsReporting();
            String [] viewIds=VIEW_ID.split(",");
           // for(String viewid:viewIds) {
                Boolean isFiltered = false;
                String dimensionFilter = "";
                String filterValue = "";

                if (Filter.length() > 0) {
                    isFiltered = true;
                    dimensionFilter = Filter.split("-")[0];
                    filterValue = Filter.split("-")[1];
                }
                for (String view : viewIds) {
                    String pageToken = "NO_VALUE";
                    while (pageToken != null) {

                        GetReportsResponse response = getReport(service, view, pageToken);
                        ReturnedObject retObj = getResponse(response, view, dimensionFilter, filterValue);
                        pageToken = retObj.nextToken;
                        System.out.println("the token received is" + pageToken);
                        System.out.println(retObj.data);
                        System.out.println(retObj.count);
                        Thread.sleep(10000);
                    }
                }
                Thread.sleep(10000);

           // System.out.println(responseLine);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
