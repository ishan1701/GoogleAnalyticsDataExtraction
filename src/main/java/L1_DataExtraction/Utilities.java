package L1_DataExtraction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.AnalyticsReportingScopes;
import com.google.api.services.analyticsreporting.v4.model.*;

import static L1_DataExtraction.Constants.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utilities {
    public static AnalyticsReporting initializeAnalyticsReporting() throws GeneralSecurityException, IOException {

        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = GoogleCredential
                .fromStream(new FileInputStream(KEY_FILE_LOCATION))
                .createScoped(AnalyticsReportingScopes.all());

        // Construct the Analytics Reporting service object.
        return new AnalyticsReporting.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME).build();
    }

    public static GetReportsResponse getReport(AnalyticsReporting service,String viewId,String pageToken) throws IOException {
        // Create the DateRange object.
        DateRange dateRange = new DateRange();
        dateRange.setStartDate(START_DATE);
        dateRange.setEndDate(END_DATE);

        //Creating metric object

        String[] constantMetric = Metrics.split(",");
        List<Metric> metricList = new ArrayList<Metric>();
        for (String metricString : constantMetric) {
            Metric metric = new Metric().setExpression(metricString);
            metricList.add(metric);
        }

        //Creating dimension Object
        String[] constantDimension = Dimensions.split(",");
        List<Dimension> dimensionList = new ArrayList<Dimension>();
        for (String dimensionString : constantDimension) {
            Dimension dimension = new Dimension().setName(dimensionString);
            dimensionList.add(dimension);
        }


        //Creating the filter Object
        Boolean isFiltered=false;
        List<DimensionFilter> dFilter=null;
        if(Filter.length()>0) {
            isFiltered = true;
            String dimensionFilter = Filter.split("-")[0];
            String filterValue = Filter.split("-")[1];

            List<String> filter = new ArrayList<String>();
            filter.add(filterValue);


            ///setDimensionFilterClauses takes List<DimensionFilterClause
            /// DimensionFilterClause take List<DimensionFilter>
            ///DimensionFilter take ArrayList

            dFilter = new ArrayList<DimensionFilter>();
            DimensionFilter dF = new DimensionFilter().setDimensionName(dimensionFilter).setOperator("EXACT").setExpressions(filter);

            dFilter.add(dF);
        }

        List<DimensionFilterClause> filterClause = new ArrayList<DimensionFilterClause>();
        filterClause.add(new DimensionFilterClause().setFilters(dFilter));


        // Create the ReportRequest object.
        ReportRequest request = new ReportRequest()
                .setViewId(viewId)
                .setDateRanges(Arrays.asList(dateRange))
                .setMetrics(metricList)
                .setDimensions(dimensionList)
                //.setDimensionFilterClauses(filterClause)
                .setPageSize(5000)
                ;
        if(isFiltered)
            request.setDimensionFilterClauses(filterClause);

        if(pageToken!="NO_VALUE")
            request.setPageToken(pageToken);

        List<ReportRequest> requests = new ArrayList<ReportRequest>();
        requests.add(request);

        // Create the GetReportsRequest object.
        GetReportsRequest getReport = new GetReportsRequest()
                .setReportRequests(requests);

        // Call the batchGet method.
        GetReportsResponse response = service.reports().batchGet(getReport).execute();

        // Return the response.
        return response;
    }

    public static ReturnedObject getResponse(GetReportsResponse response,String viewId,String dimenstionFilterKey,String dimensionfilterValue) throws JsonProcessingException {

        List<String> dimColumns = new ArrayList<String>();
        List<String> metricColumns = new ArrayList<String>();
        String[] colDimensions = Dimensions.split(",");
        String[] colMetrics = Metrics.split(",");


       // String filters = Filter.split("-")[0].split(":")[1];
        //ga:language,
        int counter=0;
        for (String dim : colDimensions) {
            dimColumns.add(dim.split(":")[1]);
        }
        //ga:pageviews
        for (String metric : colMetrics)
            metricColumns.add(metric.split(":")[1]);

        String nextToken=null;
       // while(nextToken!=null) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = null;
            ArrayNode arrayNode = mapper.createArrayNode();
            System.out.println(response.getReports().size());
          //  response.getReports().size();

            for (Report report : response.getReports()) {


                List<ReportRow> rows = report.getData().getRows();
                nextToken=report.getNextPageToken();




                node = mapper.createObjectNode();

                for (ReportRow row : rows) {

                    node = mapper.createObjectNode();
                    List<String> dimensions = row.getDimensions();
                    List<DateRangeValues> metrics = row.getMetrics();
                    for (int i = 0; i < dimensions.size(); i++) {
                        //System.out.println(dimensions.get(i));
                        node.put(dimColumns.get(i), dimensions.get(i).toString());
                    }

                    for (int i = 0; i < metrics.size(); i++) {
                        List<String> metricArray = metrics.get(i).getValues();
                        // System.out.print("Date Range (" + i + "): ");

                        for (int j = 0; j < metricArray.size(); j++) {
                           // System.out.println(metricArray.get(j));
                            node.put(metricColumns.get(j), metricArray.get(j).toString());
                        }

                    }
                    node.put("viewid",viewId);
                    if(dimenstionFilterKey.length()>0){
                        node.put(dimenstionFilterKey,dimensionfilterValue);
                    }



                    arrayNode.add(node);
                    counter++;
                }

            }

            String line = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode);
            System.out.println(line);
            return new ReturnedObject(counter,line,nextToken);
            //return line;
        }

        //return line;
    //}
}
            /*ColumnHeader header = report.getColumnHeader();
            List<String> dimensionHeaders = header.getDimensions();
            List<MetricHeaderEntry> metricHeaders = header.getMetricHeader().getMetricHeaderEntries();
            List<ReportRow> rows = report.getData().getRows();

            if (rows == null) {
                System.out.println("No data found for " + VIEW_ID);
                return;
            }

            for (ReportRow row : rows) {
                List<String> dimensions = row.getDimensions();
                List<DateRangeValues> metrics = row.getMetrics();


                for (int i = 0; i < dimensionHeaders.size() && i < dimensions.size(); i++) {
                    System.out.println(dimensionHeaders.get(i) + ": " + dimensions.get(i));
                }

                for (int j = 0; j < metrics.size(); j++) {
                    System.out.print("Date Range (" + j + "): ");
                    DateRangeValues values = metrics.get(j);
                    for (int k = 0; k < values.getValues().size() && k < metricHeaders.size(); k++) {
                        System.out.println(metricHeaders.get(k).getName() + ": " + values.getValues().get(k));
                    }
                }
            }
        }
    }
}*/
