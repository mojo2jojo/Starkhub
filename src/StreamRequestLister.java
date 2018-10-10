
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/*
    this is RTSP server
    it stream the requested video
    it also handel seek ,play pause etc request made by client

*/

class Server extends Thread{
    
    private Socket s;
    private Connection con;
    private DataOutputStream dout;
    private DataInputStream dis;
    private final Object obj;
    private ServerSocket ss1;
    private int j,i;

    
    Server(Socket soc,Object obj,int i,ServerSocket ss1){
        s = soc;
        this.obj = obj;
        this.i = i;
        this.ss1 = ss1;
        j=0;
    }
    
    synchronized public void run(){
        try{
            
            dis=new DataInputStream(s.getInputStream());
            dout=new DataOutputStream(s.getOutputStream());
            
            System.out.println("Streams created");
            
            while(true)
            {
                try{
                    String name=dis.readUTF();
                    
                    
                    if(name.equals("play"))
                    {
                        String video = dis.readUTF();
                        
                        StreamRTSP stream = new StreamRTSP(5555,video,(("stream"+i).toString()+j),ss1);
                        Thread t1 = new Thread(new Runnable(){
                            @Override
                            public void run(){
                                try {
                                    stream.main();
                                } 
                                catch (Exception ex) {
                                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        });
                        t1.start();
                        Thread.sleep(1000);
                        dout.writeUTF(("stream"+i).toString()+j);
                        j++;
                    }
                    
                }
                catch(Exception e)
                {
                    System.out.println("Error in Stream Request Listener");
                    break;
                }
            }
            s.close();
        }
        catch (Exception e){
            JOptionPane.showMessageDialog(null,"Server : Some exception occured "+e.getMessage());
        }
        finally{
            try{
                s.close();
                dis.close();
                dout.close();
            }
            catch(Exception e){
                JOptionPane.showMessageDialog(null,e.getMessage());
            }
        }
    }
}


public class StreamRequestLister{

    private static Object obj;
    private static ServerSocket ss;
    
    public StreamRequestLister()
    {
        obj = new Object();
        int port=10000;
        try{
        ss = new ServerSocket(port);
        ServerSocket ss1;
        ss1 = new ServerSocket(8554);
        int i=0;
        while(true){
            try {
                System.out.println("Waiting for client requests");
                Socket s=ss.accept();
                i++;
                System.out.println("Connection Established");
                Server t1=new Server(s,obj,i,ss1);
                t1.start();
            } 
            catch(Exception e) {
                JOptionPane.showMessageDialog(null,e.getMessage());
                ss.close();
                break;
            }
        }
        }catch(Exception e)
           {
                JOptionPane.showMessageDialog(null,e.getMessage());        
           } 
        
    }
    
    /*
    public static void main(String[] args)  throws IOException, SQLException{
        
        System.out.println("Enter the port number !!!");
        obj = new Object();
        Scanner sc=new Scanner(System.in);
        int port=Integer.parseInt(sc.next());
        
        ss = new ServerSocket(port);
        ServerSocket ss1;
        ss1 = new ServerSocket(8554);
        int i=0;
        while(true){
            try {
                System.out.println("Waiting for client requests");
                Socket s=ss.accept();
                i++;
                System.out.println("Connection Established");
                Server t1=new Server(s,obj,i,ss1);
                t1.start();
            } 
            catch(Exception e) {
                JOptionPane.showMessageDialog(null,e.getMessage());
                ss.close();
                break;
            }
        }
    }
    */
}
