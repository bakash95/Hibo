import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
public static void main(String s[]) throws UnknownHostException, IOException
{
	 //Initialize socket
    Socket socket = new Socket(InetAddress.getByName("localhost"), 5000);
    byte[] contents = new byte[10000];
    
    //Initialize the FileOutputStream to the output file's full path.
    FileOutputStream fos = new FileOutputStream("C:\\Users\\genius\\Desktop\\ama.png");
    BufferedOutputStream bos = new BufferedOutputStream(fos);
    InputStream is = socket.getInputStream();
    
    //No of bytes read in one read() call
    int bytesRead = 0; 
    
    while((bytesRead=is.read(contents))!=-1)
        bos.write(contents, 0, bytesRead); 
    
    bos.flush(); 
    socket.close(); 
    
    System.out.println("File saved successfully!");
}
}
