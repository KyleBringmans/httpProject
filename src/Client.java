import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;


public class Client {
	//TODO make sure print statements are at the correct spots and no excess ones are still in code
	//TODO make sure server closes a connection and not the client, check documentation to see what client has to do after server closed the connection
	//TODO implement chunked encoding if 'transfer-encoding' header says so, our server needn't support it
	/**
	 * Parse http command and select correct command handler
	 * @param argv Initial http command
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void main(String argv[]) throws IOException, URISyntaxException{
		// Check for valid command length
		if(argv.length != 3) throw new IOException();

		// Get HTTP command (GET,HEAD,PUT,POST)
		String httpCommand = argv[0];

		// Host and path etc.
		String uriString = argv[1];
		URI uri = new URI(uriString);

		// Parse host and path from uri
		String host = uri.getHost();
		String path = uri.getRawPath();

		// Case for when path isn't specified
		if(path == null || path.length() == 0){
			path = "/";
		}
		int port = Integer.parseInt(argv[2]);
		System.out.println(port);

		// Establish connection with server
		Socket socket = new Socket(host, port);
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		DataInputStream inFromServer = new DataInputStream(socket.getInputStream());

		// Select correct command handler
		if(httpCommand.equals("GET")){
			out.writeBytes(httpCommand + " " + path + " HTTP/1.1\r\n"
					+ "Host: " + host + "\r\n\r\n");
			get(inFromServer,socket,host);
		}
		else if(httpCommand.equals("HEAD")){
			out.writeBytes(httpCommand + " " + path + " HTTP/1.1\r\n"
					+ "Host: " + host + "\r\n\r\n");
			head(inFromServer);
		}
		// PUT and POST look the same from the client side
		else if(httpCommand.equals("PUT") || httpCommand.equals("POST")){
			out.writeBytes(httpCommand + " " + path + " HTTP/1.1\r\n"
					+ "Host: " + host + "\r\n");
			put(out);
		}
		socket.close();

	}

	/**
	 * Handle the GET command of the client
	 * @param inFromServer Stream of data from server
	 * @param socket Socket of where the connection is established
	 * @param host The host name
	 * @throws IOException
	 */
	public static void get(DataInputStream inFromServer, Socket socket, String host) throws IOException {
		// Read new line from server stream and print it
		System.out.println(inFromServer.readLine());

		// Parse headers
		HeaderData headers = new HeaderData(inFromServer,true);

		FileHandler handler = new FileHandler();

		String content;
		if(headers.map.get("Transfer-Encoding").equals("chunked")){
			content = chunked(inFromServer,socket,host);
		}
		else{
			int length = headers.getContentLength();
			content = handler.getContent(inFromServer, length);
		}

		// Write data to file
		handler.writeOutputToFile(content, "response.html");

		File f = new File("response.html");

		// Parse the html input so images can be found
		// TODO support alle soorten encoding, mss dus charsetName (UTF-8) een variabele maken
		Document doc = Jsoup.parse(f, "UTF-8");

		// Find images from parsed code
		Elements elements = doc.getElementsByTag("img");

		// Reconstruct paths for images
		String[] imgPaths = new String[elements.size()];
		for (int i = 0; i < elements.size(); i++) {
			imgPaths[i] = findImagePath(elements.get(i));
		}

		// Write image to file
		for (String imgPath : imgPaths) {
			writeImageToFile(host, imgPath, socket);
		}
	}

	public static String chunked(DataInputStream inFromServer, Socket socket, String host) throws IOException {
		ByteArrayOutputStream content = new ByteArrayOutputStream();
		String input = inFromServer.readLine();
		Byte b = 0;
		int length;
		while(!input.equals("0")){
			if(input.equals("")){
				length = 0;
			}else {
				length = Integer.parseInt(input.split("\r\n")[0], 16);
			}
			for(int i = 0; i<length; i++) {
				b = inFromServer.readByte();
				content.write(b);
			}
			input = inFromServer.readLine();

		}
		inFromServer.readLine();
		return content.toString();

	}

	/**
	 * Handle the HEAD command of the client
	 * @param inFromServer Stream of data from server
	 * @throws IOException
	 */
	public static void head(DataInputStream inFromServer) throws IOException {
		// Read line from server
		System.out.println(inFromServer.readLine());

		// Print the header data
		new HeaderData(inFromServer,true);
	}

	/**
	 * Handle the PUT command of the client
	 * @param outFromClient
	 * @throws IOException
	 */
	public static void put(DataOutputStream outFromClient) throws IOException{
		// Open input stream from user
		DataInputStream in = new DataInputStream(System.in);

		// Parse input
		String content = "";
		String line = in.readLine();
		while(!line.equals("")){
			content += line + "\n";
			line = in.readLine();
		}

		// Send headers
		int length = content.length();
		outFromClient.writeBytes("Content-Length: " + length + "\r\n");
		outFromClient.writeBytes("Content-Type: text/txt" + "\r\n"); //TODO
		outFromClient.writeBytes("\r\n");

		// Send file content
		outFromClient.writeBytes(content);
	}


	
	/**
	 * Returns the byte array for an image (Images are saved as bytes in a file)
	 */
	public static byte[] getContentForImage(DataInputStream inFromServer, int length) throws IOException{
		byte[] response = new byte[length];

		// Make byte array
		for(int i = 0; i<length; i++) {
			response[i] = (byte) inFromServer.read();
		}
		return response;
	}

	/**
	 * Establish connection with host and save imageFile embedded in website
	 * @param host The host
	 * @param imagePath The path of where the image needs to be stored
	 * @param socket At which socket the connection needs to be established
	 * @throws IOException
	 */
	public static void writeImageToFile(String host, String imagePath, Socket socket) throws IOException{
		// Make connection
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		DataInputStream inFromServer = new DataInputStream(socket.getInputStream());
		out.writeBytes("GET " + imagePath + " HTTP/1.1\r\n"
				+ "Host: " + host + "\r\n\r\n");

		// Consume server response
		inFromServer.readLine();

		// Make header for ImageFile
		HeaderData headers = new HeaderData(inFromServer,false);
		int length = headers.getContentLength();
		byte[] content = getContentForImage(inFromServer, length);

		// Make directory
		String[] temp = imagePath.split("/");
		String dirName = imagePath.substring(0, imagePath.length() - temp[temp.length-1].length());
		dirName = dirName.replace("%20", " ");
		String fileName = temp[temp.length-1];
		File dir = new File (System.getProperty("user.dir") + dirName);
		File file = new File (dir, fileName);

		// Write content to file
        final FileOutputStream fileOut = new FileOutputStream(file);
        fileOut.write(content);
        fileOut.flush();
        fileOut.close();

	}

	/**
	 * Find path for the image from el
	 * @param el Part of parsed html code where image path needs to be extracted from
	 * @return The image path of the element
	 */
	public static String findImagePath(Element el){
		// Parse the image path out of the html command
		String path = el.toString();

		// Parse for image paths (left of regex has to be ignored)
		String[] split = path.split("src=\"");

		// edge case, if src is upper case
		if(split.length == 1){
			split = path.split("SRC=\"");
		}

		// Ignore everything before regex
		path = split[1];

		// Split on (")
		split = path.split("\"");

		// Ignore everything after second (")
		path = split[0];

		// Split the directory and the file
		String[] folders = path.split("/");
		String newFolders = "";
		for(int i = 0; i < folders.length -1; i++){
			newFolders += folders[i] + "/";
		}

		// Make the new folders
		if(newFolders.length() > 0){
			newFolders = newFolders.substring(0, newFolders.length()-1);
			newFolders = newFolders.replace("%20", " ");
			new File(newFolders).mkdirs();
		}

		return "/" + path;
	}
}
