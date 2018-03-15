import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;

public class Client {
	public static void main(String argv[]) throws UnknownHostException, IOException, URISyntaxException{
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
		Socket socket = new Socket(host, port);
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		System.out.println("GET " + path + " HTTP/1.1\r\n"
				+ "Host: " + host + "\r\n\r\n");
		out.writeBytes("GET " + path + " HTTP/1.1\r\n"
				+ "Host: " + host + "\r\n\r\n");
		int length = getContentLength(inFromServer);
		String content = getContent(inFromServer, length);
		writeOutputToFile(content, "response.html");

		writeImageToFile(host, "/tos-jwall.jpg", socket);
		
		socket.close();
	}
	
	
	public static void writeOutputToFile(String content, String fileName) throws FileNotFoundException{
		PrintWriter writer = new PrintWriter(fileName);
		writer.print(content);
		writer.close();
	}
	
	public static int getContentLength(BufferedReader inFromServer) throws IOException{
		String nextLine = inFromServer.readLine();
		String header = "Content-Length:";
		while(!nextLine.startsWith(header)){
			System.out.println(nextLine);
			nextLine = inFromServer.readLine();
		}
		//remove the text of the header
		nextLine = nextLine.substring(header.length());
		//remove the leftover whitespace
		nextLine = nextLine.replaceAll(" ", "");
		return Integer.parseInt(nextLine);
	}
	
	public static String getContent(BufferedReader inFromServer, int length) throws IOException{
		int[] output = new int[length];
		String j;
		j = inFromServer.readLine();
		while(!j.equals("")){
			System.out.println(j);
			j = inFromServer.readLine();
			continue;
		}
		System.out.println(length);
		StringBuilder response = new StringBuilder();
		for(int i = 0; i<length; i++) {
			response.append((char) inFromServer.read());

		}
		return response.toString();
	}
	
	public static void writeImageToFile(String host, String imagePath, Socket socket) throws IOException{
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out.writeBytes("GET " + imagePath + " HTTP/1.1\r\n"
				+ "Host: " + host + "\r\n\r\n");
		int length = getContentLength(inFromServer);
		String content = getContent(inFromServer, length);
		writeOutputToFile(content, "imageInfo.txt");
		File file= new File("imageInfo.txt");
		BufferedImage image = ImageIO.read(file);
		File outputFile= new File("image.jpeg");
        ImageIO.write(image, "jpeg", outputFile);
	}
}
