
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

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
		System.out.println(host);
		System.out.println(path);
		int port = Integer.parseInt(argv[2]);
		System.out.println(port);

		// Establish connection with server
		Socket socket = new Socket(host, port);
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		DataInputStream inFromServer = new DataInputStream(socket.getInputStream());

		// System.out.println("GET " + path + " HTTP/1.1\r\n"+ "Host: " + host + "\r\n\r\n");

		// Get html code from the website
		out.writeBytes("GET " + path + " HTTP/1.1\r\n"
				+ "Host: " + host + "\r\n\r\n");
		int length = getContentLength(inFromServer);
		String content = getContent(inFromServer, length);
		writeOutputToFile(content, "response.html");

		File test = new File("response.html");
		Document doc = Jsoup.parse(test, "UTF-8");
		Elements elements = doc.getElementsByTag("img");
		String[] imgPaths = new String[elements.size()];
		for(int i = 0; i < elements.size(); i++){
			imgPaths[i] = findImagePath(elements.get(i));
		}
		for(String imgPath : imgPaths){
			writeImageToFile(host, imgPath, socket);
		}
		
		socket.close();

	}
	
	
	public static void writeOutputToFile(String content, String fileName) throws FileNotFoundException{
		PrintWriter writer = new PrintWriter(fileName);
		writer.print(content);
		writer.close();
	}
	
	public static String getContent(DataInputStream inFromServer, int length) throws IOException{
		// Skip headers
		String j = inFromServer.readLine();
		while(!j.equals("")){
			System.out.println(j);
			j = inFromServer.readLine();
			continue;
		}
		System.out.println(length);
		// Get data from server
		StringBuilder response = new StringBuilder();
		for(int i = 0; i<length; i++) {
			response.append((char) inFromServer.read());

		}
		return response.toString();
	}
	
	@SuppressWarnings("deprecation")
	public static byte[] getContentForImage(DataInputStream inFromServer, int length) throws IOException{
		// Remove headers
		String j = inFromServer.readLine();
		while(!j.equals("")){
			// System.out.println(j);
			j = inFromServer.readLine();
		}
		System.out.println(length);
		byte[] response = new byte[length];
		for(int i = 0; i<length; i++) {
			response[i] = (byte) inFromServer.read();
		}
		return response;
	}
	
	public static void writeImageToFile(String host, String imagePath, Socket socket) throws IOException{
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		DataInputStream inFromServer = new DataInputStream(socket.getInputStream());
		out.writeBytes("GET " + imagePath + " HTTP/1.1\r\n"
				+ "Host: " + host + "\r\n\r\n");
		int length = getContentLength(inFromServer);
		byte[] content = getContentForImage(inFromServer, length);
		//try (FileOutputStream fos = new FileOutputStream(imagePath.substring(1))) {
		//	   fos.write(content);
		//}
		System.out.println("sqkdjhgfkjsqdgfbjkhsqdbfjkhsqdbfjhqksdbfvhjksqdvkhjqsbvjhbqsdvbjqskdvbjqdshv" + imagePath.substring(1));

		String[] temp = imagePath.split("/");
		String dirName = imagePath.substring(0, imagePath.length() - temp[temp.length-1].length());
		dirName = dirName.replace("%20", " ");

		System.out.println(dirName);

		String fileName = temp[temp.length-1];
		File dir = new File (System.getProperty("user.dir") + dirName);
		File file = new File (dir, fileName);

		System.out.println(file.getCanonicalPath());

		//final File file = new File(imagePath);
        final FileOutputStream fileOut = new FileOutputStream(file);
        fileOut.write(content);
        fileOut.flush();
        fileOut.close();
		//PrintWriter writer = new PrintWriter(imagePath.substring(1));
		//writer.print(content);
		//writer.close();
	}
	
	public static String findImagePath(Element el){
		String path = el.toString();
		String[] split = path.split("src=\"");
		path = split[1];
		split = path.split("\"");
		path = split[0];
		String[] folders = path.split("/");
		String newFolders = "";
		for(int i = 0; i < folders.length -1; i++){
			newFolders += folders[i] + "/";
		}
		if(newFolders.length() > 0){
			newFolders = newFolders.substring(0, newFolders.length()-1);
			newFolders = newFolders.replace("%20", " ");
			new File(newFolders).mkdirs();
		}
		//path = path.substring("<img SRC=\"".length());
		//path = path.substring(0, path.length() - ">".length()-1);
		//path = "/" + path;
		System.out.println("kjghvjsdgfuskdgfkjdfbjk     " + path);
		return "/" + path;
	}
}
