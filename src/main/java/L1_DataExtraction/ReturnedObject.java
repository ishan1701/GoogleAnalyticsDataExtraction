package L1_DataExtraction;

public class ReturnedObject {
    String nextToken="";
    int count;
    String data;
    ReturnedObject(int count,String line ,String nextToken){
        this.count=count;
        this.data=line;
        this.nextToken=nextToken;

    }
}
