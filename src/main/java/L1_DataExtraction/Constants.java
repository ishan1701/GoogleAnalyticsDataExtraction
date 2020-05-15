package L1_DataExtraction;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

public  class Constants {

    Constants(){

    }
    public static final String APPLICATION_NAME = "GOOGLE ANALYTICS DATA EXTRACTION";
    public static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    public static final String KEY_FILE_LOCATION = "";
    public static final String VIEW_ID = "1223566";
    public static final String START_DATE="today";
    public static final String END_DATE="today";
    public static final String Dimensions="ga:language,ga:country,ga:date,ga:sourcemedium,ga:city,ga:hostname,ga:pagePath,ga:channelGrouping,ga:region";
    public static final String Metrics="ga:pageviews,ga:uniquePageviews";
    public static final String Filter="";
    //public static final String Filter="ga:deviceCategory-desktop";

}
