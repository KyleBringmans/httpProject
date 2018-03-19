
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;


public class Client {
	public static void main(String argv[]) throws IOException, URISyntaxException{
		if(argv.length != 3) throw new IOException();
		String httpCommand = argv[0];
		String uriString = argv[1];
		URI uri = new URI(uriString);
		String host = uri.getHost();
		String path = uri.getRawPath();
		if(path == null || path.length() == 0){
			path = "/";
		}
		int port = Integer.parseInt(argv[2]);
		System.out.println(port);

		// Establish connection with server
		Socket socket = new Socket(host, port);
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		DataInputStream inFromServer = new DataInputStream(socket.getInputStream());

		System.out.println(inFromServer.readLine());

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
	 * @param socket Socket of where the connection needs to be established
	 * @param host The host name
	 * @throws IOException
	 */
	public static void get(DataInputStream inFromServer, Socket socket, String host) throws IOException {
		System.out.println(inFromServer.readLine());
		HeaderData headers = new HeaderData(inFromServer,true);
		int length = headers.getContentLength();

		// Only get data if command is GET
		FileHandler handler = new FileHandler();
		String content = handler.getContent(inFromServer, length);
		handler.writeOutputToFile(content, "response.html");

		File test = new File("response.html");

		// Parse the html input so images can be found
		Document doc = Jsoup.parse(test, "UTF-8");
		Elements elements = doc.getElementsByTag("img");
		String[] imgPaths = new String[elements.size()];
		for(int i = 0; i < elements.size(); i++){
			imgPaths[i] = findImagePath(elements.get(i));
		}
		for(String imgPath : imgPaths){
			writeImageToFile(host, imgPath, socket);
		}

	}

	/**
	 * Handle the HEAD command of the client
	 * @param inFromServer Stream of data from server
	 * @throws IOException
	 */
	public static void head(DataInputStream inFromServer) throws IOException {
		System.out.println(inFromServer.readLine());
		new HeaderData(inFromServer,true);
	}

	/**
	 *
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
			content += line + "\r\n";
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
	 * Get the content of the image???
	 */
	public static byte[] getContentForImage(DataInputStream inFromServer, int length) throws IOException{
		// Read in data
		byte[] response = new byte[length];
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
	 *
	 * @param el Part of parsed html code where image path needs to be extracted from
	 * @return The image path of the element
	 */
	public static String findImagePath(Element el){
		// Parse the image path out of the html command
		String path = el.toString();
		String[] split = path.split("src=\"");
		path = split[1];
		split = path.split("\"");
		path = split[0];
		String[] folders = path.split("/");

		// Make the folder names
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
	//TODO handle case where content length not in header
}
