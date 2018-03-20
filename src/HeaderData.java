import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;
import java.util.Map;

public class HeaderData {

    public HeaderData(DataInputStream stream, boolean print) throws IOException {

        String nextLine = stream.readLine();
        while(!nextLine.equals("")){
            if(print){
                System.out.println(nextLine);
            }
            String[] data = nextLine.split(": ");
            this.map.put(data[0],data[1]);

            nextLine = stream.readLine();
        }
    }


    public int getContentLength(){
        return Integer.parseInt(this.map.get("Content-Length"));
    }
    public String getIfModifiedSince(){
        return this.map.get("If-Modified-Since");
    }

    /**
     * Hashmap that stores all content from the received headers in an easy to access format
     */
    public Map<String,String> map = new HashMap();
}
