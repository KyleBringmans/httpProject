import javax.xml.crypto.Data;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;

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
	        String protocol = firstLineSplitted[2];
	        HeaderData headers = new HeaderData(this.inputs, false);
	        if(operation == "GET"){
	        	this.handleGet(path, protocol);
	        }
	        else if(operation == "HEAD"){
	        	this.handleHead(path, protocol);
	        }
	        else if(operation == "PUT"){
	        	this.handlePut(path, protocol);
	        }
	        else if(operation == "POST"){
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

	private void handleHead(String path, String protocol) {
		// TODO Send headers related to the requested html file
		
	}

	private void handleGet(String path, String protocol) {
		// TODO Send the html file of one of the hosted websites, DON'T FORGET THE HEADERS!
		
	}

	private Socket client;
    private DataInputStream inputs;
    private DataOutputStream outputs;
}
