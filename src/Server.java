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
	        	//TODO ik denk dat er dan zo een status code geprint moet worden?
	        }
	        
	        inputs.close();
	        outputs.close();
	        System.out.println("t is gebeurd");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }

    private void handlePost(String path, String protocol) {
		// TODO Auto-generated method stub
		
	}

	private void handlePut(String path, String protocol) {
		// TODO Auto-generated method stub
		
	}

	private void handleHead(String path, String protocol) {
		// TODO Auto-generated method stub
		
	}

	private void handleGet(String path, String protocol) {
		// TODO Auto-generated method stub
		
	}

	private Socket client;
    private DataInputStream inputs;
    private DataOutputStream outputs;
}
