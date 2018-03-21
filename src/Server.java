import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.nio.file.Files;


public class Server {

	/**
	 * Create new threads for each http request
	 */
	public static void main(String argv[]) throws IOException, URISyntaxException {
		// Initialize a socket at port X
		ServerSocket socket = new ServerSocket(3000);

		while(true){
			Socket client = socket.accept();
			new Thread(new Task(client)).start();
		}
	}
}

/**
 * Task class
 */
class Task implements Runnable {
    public Task(Socket client) throws IOException {
        this.client = client;
    }

	/**
	 * Parse commands and choose correct function to handle http request
	 */
	@Override
    public void run(){
        try {

        	// Create streams for connection
			this.inputs = new DataInputStream(client.getInputStream());
	        this.outputs = new DataOutputStream(client.getOutputStream());

	        // Read first input from client
	        String firstLine = this.inputs.readLine();
	        String[] firstLineSplitted = firstLine.split(" ");

	        // Check for legal client request
			if(firstLineSplitted.length != 3) throw new IOException();
	        String operation = firstLineSplitted[0];

	        String path = firstLineSplitted[1];
	        if(path.equals("/")) path = "/index.html";


	        String protocol = firstLineSplitted[2];

	        // Parse headers from client
	        HeaderData headers = new HeaderData(this.inputs, false);

	        if(!headers.map.containsKey("Host")){
				outputs.writeBytes("400: Bad Request");
			}

			// Check for correct http version
			if(!protocol.equals("HTTP/1.1")){
				outputs.writeBytes("505: HTTP Version Not Supported");
			}

			// Choose correct handler for client http request
	        if(operation.equals("GET")){
	        	this.handleGet(path, headers);
		        inputs.close();
		        outputs.close();
	        }
	        else if(operation.equals("HEAD")){
	        	this.handleHead(path, headers);
		        inputs.close();
		        outputs.close();
	        }
	        else if(operation.equals("PUT")){
	        	this.handlePut(path, headers);
	        }
	        else if(operation.equals("POST")){
	        	this.handlePost(path,headers);
	        }
	        else{
	        	outputs.writeBytes("400: Bad Request");
	        }
		} catch (IOException e1) {
			try {
				outputs.writeBytes("HTTP/1.1:" + " 304 Not Modified\r\n");
			} catch (IOException e) {
				System.out.println("Lost connection with client");
			}
		}
    }

	/**
	 * Handle the GET request
	 * @param path The path to the file
	 * @throws IOException
	 */
	private void handleGet(String path, HeaderData headers) throws IOException {
		path = path.substring(1);
		File f = new File(path);
		String ifModifiedSince = headers.getIfModifiedSince();
		if(f.exists() && ifModifiedSince != null && !modifiedAfterDate(ifModifiedSince, f)){
			outputs.writeBytes("HTTP/1.1:" + " 304 Not Modified\r\n");
			outputs.writeBytes("Date: " + this.getServerTime() + "\r\n");
			outputs.writeBytes("\r\n");
			return;
		}
		this.handleHead(path, headers);
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
		else{
			outputs.writeBytes("HTTP/1.1" + "404 Not Found\r\n");
		}
	}


	/**
	 * Handles the PUT request
	 * @param path The path to the file
	 */
	private void handlePut(String path,HeaderData headers) throws IOException {
		// Parse input of client
		int length = headers.getContentLength();
		FileHandler handler = new FileHandler();
		String content = handler.getContent(this.inputs,length);
		handler.writeOutputToFile(content,path);

		// Initial server response
		outputs.writeBytes("HTTP/1.1: 200 OK\r\n");

		// Send headers
		outputs.writeBytes("Date: " + this.getServerTime() + "\r\n");
		outputs.writeBytes("\r\n");

        inputs.close();
        outputs.close();
	}


	/**
	 * Handles the POST request
	 * @param path The path to the file
	 */
	private void handlePost(String path,HeaderData headers) throws IOException {
		// Slash breaks directory structure
		String pathNoSlash = path.substring(1);

		// Get possible file at requested location
		File f = new File(pathNoSlash);

		// Handle case if file does or doesn't exist
		if(f.exists()){
			// Get metadata
			int length = headers.getContentLength();
			FileHandler handler = new FileHandler();
			String content = handler.getContent(inputs,length);

			// Append bytes to existing file
			Files.write(Paths.get(pathNoSlash), content.getBytes(), StandardOpenOption.APPEND);

			// Initial server response
			outputs.writeBytes("HTTP/1.1: 200 OK\r\n");

			// Send headers
			outputs.writeBytes("Date: " + this.getServerTime() + "\r\n");
			outputs.writeBytes("\r\n");
		}
		else{
			// Create new file if it doesn't exist yet
			handlePut(path,headers);
		}
	}


	/**
	 * Handles the HEAD request
	 * @param path The path to the file
	 * @throws IOException
	 */
	private void handleHead(String path, HeaderData headers) throws IOException {
		File f = new File(path);
		System.out.println(path);
		String ifModifiedSince = headers.getIfModifiedSince();
		if(f.exists() && ifModifiedSince != null && !modifiedAfterDate(ifModifiedSince, f)){
			outputs.writeBytes("HTTP/1.1:" + " 304 Not Modified\r\n");
			outputs.writeBytes("Date: " + this.getServerTime() + "\r\n");
			outputs.writeBytes("\r\n");
			return;
		}
		if(f.exists() && !f.isDirectory()) { 
		    try {
		    	System.out.println("length: " + f.length());
				outputs.writeBytes("HTTP/1.1:" + " 200 OK\r\n");
				outputs.writeBytes("Date: " + this.getServerTime() + "\r\n");
				outputs.writeBytes("Content-Length: " + f.length() + "\r\n");
				outputs.writeBytes("Content-Type: " + this.getFileExtension(f) + "\r\n");
				outputs.writeBytes("\r\n\r\n");
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			outputs.writeBytes("HTTP/1.1:" + " 404 Not Found\r\n");
		}
		
	}



	/**
	 * @return the current time in the correct format
	 */
	private String getServerTime() {
	    Calendar calendar = Calendar.getInstance();
	    SimpleDateFormat dateFormat = new SimpleDateFormat(
	        "EEE, dd MMM yyyy HH:mm:ss z", Locale.UK);
	    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	    return dateFormat.format(calendar.getTime());
	}

	/**
	 * Get the extension of the requested file
	 * @param file The file of which the exstension is requested
	 * @return The extension of the requested file
	 */
	private String getFileExtension(File file) {
	    String name = file.getName();
	    try {
	        return name.substring(name.lastIndexOf(".") + 1);
	    } catch (Exception e) {
	        return "";
	    }
	}
	
	private boolean modifiedAfterDate(String dateAsString, File file){
	    SimpleDateFormat dateFormat = new SimpleDateFormat(
	        "EEE, dd MMM yyyy HH:mm:ss z", Locale.UK);
	    Date date;
		try {
			date = dateFormat.parse(dateAsString);
		    Date lastModifiedDate = dateFormat.parse(dateFormat.format(file.lastModified()));
		    return lastModifiedDate.after(date);
		} catch (ParseException e) {
			return false;
		}
	}




	private Socket client;
    private DataInputStream inputs;
    private DataOutputStream outputs;
}
