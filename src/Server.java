import javax.xml.crypto.Data;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class Server {
	
    public static void main(String argv[]) throws IOException, URISyntaxException {
        // Initialize a socket at port X
        ServerSocket socket = new ServerSocket(3000);

        while(true){
            Socket client = socket.accept();
            new Thread(new Task(client)).start();
        }
    }
}

class Task implements Runnable {
    public Task(Socket client) throws IOException {
        this.client = client;
    }

    @Override
    public void run(){
        try {
			this.inputs = new DataInputStream(client.getInputStream());
	        this.outputs = new DataOutputStream(client.getOutputStream());
	        String firstLine = this.inputs.readLine();
	        String[] firstLineSplitted = firstLine.split(" ");
			if(firstLineSplitted.length != 3) throw new IOException();
	        String operation = firstLineSplitted[0];
	        String path = firstLineSplitted[1];
	        if(path.equals("/")) path = "index.html";
	        String protocol = firstLineSplitted[2];
	        HeaderData headers = new HeaderData(this.inputs, false);
	        System.out.println(operation);
	        if(operation.equals("GET")){
	        	this.handleGet(path, protocol);
	        }
	        else if(operation.equals("HEAD")){
	        	this.handleHead(path, protocol);
	        }
	        else if(operation.equals("PUT")){
	        	this.handlePut(path, protocol);
	        }
	        else if(operation.equals("POST")){
	        	this.handlePost(path, protocol);
	        }
	        else{
	        	System.out.println("400 Bad Request");
	        }
	        
	        inputs.close();
	        outputs.close();
	        System.out.println("t is gebeurd"); //lol
		} catch (IOException e1) {
			System.out.println("Something wrong with the socket of the server");
		}
    }

    private void handlePost(String path, String protocol) {
		// TODO Add text to end of file
		
	}

	private void handlePut(String path, String protocol) {
		// TODO Add a new file with the text
		
	}

	private void handleHead(String path, String protocol) throws IOException {
		File f = new File(path);
		System.out.println(path);
		if(f.exists() && !f.isDirectory()) { 
		    try {
				outputs.writeBytes(protocol + " 200 OK\r\n");
				outputs.writeBytes("Date: " + this.getServerTime() + "\r\n");
				outputs.writeBytes("Content-Length: " + f.length() + "\r\n");
				outputs.writeBytes("Content-Type: " + this.getFileExtension(f) + "\r\n");
				outputs.writeBytes("\r\n");
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			//TODO zo'n code
			outputs.writeBytes(protocol + " 200 OK\r\n");
		}
		
	}

	private void handleGet(String path, String protocol) throws IOException {
		// TODO Send the html file of one of the hosted websites, DON'T FORGET THE HEADERS!
		this.handleHead(path, protocol);
		File f = new File(path);
		if(f.exists() && !f.isDirectory()) { 
			FileReader filer = new FileReader(f);
			BufferedReader buffr = new BufferedReader(filer);
			
			String s = buffr.readLine();
			while (s != null){
			  outputs.writeBytes(s + "\n");
			  s = buffr.readLine();
			}
			buffr.close();
		}
	}
	
	private String getServerTime() {
	    Calendar calendar = Calendar.getInstance();
	    SimpleDateFormat dateFormat = new SimpleDateFormat(
	        "EEE, dd MMM yyyy HH:mm:ss z", Locale.UK);
	    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	    return dateFormat.format(calendar.getTime());
	}
	
	private String getFileExtension(File file) {
	    String name = file.getName();
	    try {
	        return name.substring(name.lastIndexOf(".") + 1);
	    } catch (Exception e) {
	        return "";
	    }
	}

	private Socket client;
    private DataInputStream inputs;
    private DataOutputStream outputs;
}
