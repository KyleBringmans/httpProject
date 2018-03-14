import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.sun.corba.se.impl.ior.GenericTaggedComponent;

import sun.misc.IOUtils;

public class Client {
	public static void main(String argv[]) throws UnknownHostException, IOException, URISyntaxException{
		String sss = "a   b";
		String ssss = sss.replace(" ", "");
		System.out.println(ssss);
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
		Socket socket = new Socket(host, port);
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out.writeBytes("GET " + path + " HTTP/1.1\n"
				+ "Host: " + host + "\n\n");
		int length = getContentLength(inFromServer);
		int[] content = getContent(inFromServer, length);
		String contentString = new String(content, "CP1250");
		writeOutputToFile(contentString);
		
		socket.close();
	}
	
	
	public static void writeOutputToFile(String content) throws FileNotFoundException{
		System.out.println("hhier");
		PrintWriter writer = new PrintWriter("response.html");
		writer.print(content);
		writer.close();
	}
	
	public static int getContentLength(BufferedReader inFromServer) throws IOException{
		String nextLine = inFromServer.readLine();
		String header = "Content-Length:";
		while(!nextLine.startsWith(header))nextLine = inFromServer.readLine();
		//remove the text of the header
		nextLine = nextLine.substring(header.length());
		//remove the leftover whitespace
		nextLine = nextLine.replaceAll(" ", "");
		return Integer.parseInt(nextLine);
	}
	
	public static int[] getContent(BufferedReader inFromServer, int length) throws IOException{
		int[] output = new int[length];
		String j;
		j = inFromServer.readLine();
		while(j.equals("")){
			System.out.println(j);
			j = inFromServer.readLine();
			continue;
		}
		System.out.println("hier");		
		for(int i = 0; i<length; i++){
			output[i] = inFromServer.read();
		}
		System.out.println("hsubfqsdkjhfbhjsb");
		
		return output;
	}
}
