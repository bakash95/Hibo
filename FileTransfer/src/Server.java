import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTextArea;

public class Server extends Thread 
{
	static String s;
public static void main(String a[])throws IOException 
{
	JFrame f=new JFrame();
	Dimension t=Toolkit.getDefaultToolkit().getScreenSize();
	f.setSize((int)t.getWidth(),(int)t.getHeight());
	JTextArea myPanel = new JTextArea();
	myPanel.setDropTarget(new DropTarget() {
	    public synchronized void drop(DropTargetDropEvent evt) {
	        try {
	            evt.acceptDrop(DnDConstants.ACTION_COPY);
	            List<File> droppedFiles = (List<File>)
	                evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
	            System.out.println("exe");
	            s=droppedFiles.get(0).getAbsolutePath();
	            Server t1=new Server();
	            t1.start();
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    }
		
	});
	f.add(myPanel);
	f.setVisible(true);
  }
private void sendFile(String s) throws IOException, UnknownHostException, FileNotFoundException {
	ServerSocket ssock = new ServerSocket(5000);
	  Socket socket = ssock.accept();
	  
	  //The InetAddress specification
	  InetAddress IA = InetAddress.getByName("localhost"); 
	  
	  //Specify the file
	  File file = new File(s);
	  FileInputStream fis = new FileInputStream(file);
	  BufferedInputStream bis = new BufferedInputStream(fis); 
	    
	  //Get socket's output stream
	  OutputStream os = socket.getOutputStream();
	          
	  //Read File Contents into contents array 
	  byte[] contents;
	  long fileLength = file.length(); 
	  long current = 0;
	   
	  long start = System.nanoTime();
	  while(current!=fileLength){ 
	      int size = 10000;
	      if(fileLength - current >= size)
	          current += size;    
	      else{ 
	          size = (int)(fileLength - current); 
	          current = fileLength;
	      } 
	      contents = new byte[size]; 
	      bis.read(contents, 0, size); 
	      os.write(contents);
	      System.out.print("Sending file ..."+(current*100)/fileLength+"% complete!\n");
	  }       
	  os.flush(); 
	  socket.close();
	  ssock.close();
	  System.out.println("File sent succesfully!");
}
public void run()
{
	System.out.println("execuing");
	try {
		sendFile(s);
	} catch (UnknownHostException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
}


