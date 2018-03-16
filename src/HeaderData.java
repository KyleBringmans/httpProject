import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;
import java.util.Map;

public class HeaderData {

    public HeaderData(DataInputStream stream) throws IOException {

        String nextLine = stream.readLine();
        while(!nextLine.equals("")){
            System.out.println(nextLine);

            String[] data = nextLine.split(": ");
            this.map.put(data[0],data[1]);

            nextLine = stream.readLine();
        }
    }

    public int getContentLength(){
        return Integer.parseInt(this.map.get("Content-Length"));
    }

    Map<String,String> map = new HashMap();
}
