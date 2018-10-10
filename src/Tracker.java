

/*
    this class is backbone of starkhub server
*/


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import java.sql.*;
import java.util.concurrent.TimeUnit;

class wait5min extends Thread
{
    public void run()
    {
        try{
        Connect_db db=new Connect_db();
        Connection con=db.still_connecting();
        Statement st=con.createStatement();
        System.out.println("good to go");
        while(true)
        {   

            String sqlin="UPDATE Videos SET trendview=0 ";
            st.executeUpdate(sqlin);
            System.out.println("trending list updated");
            TimeUnit.SECONDS.sleep(60);
        }
        }catch(Exception e)
        {
            System.out.println(e);
        }
    }
}


/*
     below client is a listener it listen for the message coming 
     from different connected clients and manages request
     and take specified action
     it also manage database
*/


class Lis extends Thread
{
    private Socket socket=null,lsoc=null;
    private DataInputStream  in  = null;
    private DataOutputStream out=null;
    private Tracker tr;
    private Connection obj;
    private Statement st=null;
    private String uname="";
    private static int portnum=11001;
    
    void listenM(Tracker t,Socket sc )
    {
        try {
            socket=sc;
            tr=t;
            in = new DataInputStream(socket.getInputStream());
            out=new DataOutputStream(socket.getOutputStream());
            obj=t.getConObj();
            this.start();
        } catch (Exception ex) {
            System.out.println("Error in contructor of Lis class");
        }
  
    }
    
    public void run()
    {
        while(true)
        {
            try {
                System.out.print("new:");
                String line;
                line = in.readUTF();
                System.out.println(socket+" "+line);
                st=obj.createStatement();
                
                
                if(line.equals("req@login"))
                {
                    uname=in.readUTF();
                    tr.setUnameFromSocket(socket, uname);
                    String pwd=in.readUTF();
                    
                    String sql="SELECT password FROM Account WHERE username= '"+uname+"'";
                    ResultSet rs=st.executeQuery(sql);
                    boolean res=rs.next();
                    if(res)
                    {
                        String s=rs.getString(1);
                        if(s.equals(pwd))
                        {
                            String sqlin="UPDATE Videos SET state = 'online' WHERE username ='"+uname+"'";
                            st.executeUpdate(sqlin);
                            sqlin="UPDATE Channel SET state = 'online' WHERE username ='"+uname+"'";
                            st.executeUpdate(sqlin);
                            out.writeUTF("success");
                        }
                        else
                            out.writeUTF("wrong password");
                    }
                    else
                    out.writeUTF("account does not exist");
                    
                }
                
                else if(line.equals("req@signup"))
                {
                    try{
                    String u=in.readUTF();
                    String pwd=in.readUTF();
                    String sql="insert into Account(username,password) values('"+u+"','"+pwd+"')";
                    st.executeUpdate(sql);
                    out.writeUTF("success");
                    }catch(Exception e)
                    {
                        out.writeUTF("Error in Account creation");
                    }
                    
                }
                
                else if(line.equals("req@makeListner"))
                {
                    out.writeUTF(""+portnum);
                    ServerSocket sc=new ServerSocket(portnum);
                    lsoc=sc.accept();
                    System.out.println("Listner created");
                    portnum++;
                    DataOutputStream oo=new DataOutputStream(lsoc.getOutputStream());
                    tr.setOutStream(uname, oo);
                }
                
                else if(line.equals("req@newChannel"))
                {
                    String cname=in.readUTF();
                    String des=in.readUTF();
                    String head=in.readUTF();
                    
                    try{
                    String sql="insert into Channel(channelname,username,description,head) values('"+cname+"','"+uname+"','"+des+"','"+head+"')";
                    st.executeUpdate(sql);
                    out.writeUTF("success");
                    }catch(Exception ex)
                    {
                        out.writeUTF("error in channel creation");
                    }
                }
                else if(line.equals("req@uploadVideo"))
                {
                    String cname=in.readUTF();
                    String path=in.readUTF();
                    String vname=in.readUTF();
                    String tag=in.readUTF();
                    try{
                    String sql="insert into Videos(channelname,videopath,videoname,username,tags) values('"+cname+"','"+path+"','"+vname+"','"+uname+"','"+tag+"')";
                    st.executeUpdate(sql);
                    out.writeUTF("success");
                    String s=java.time.LocalDateTime.now()+" :: "+uname+" uploaded new Video '"+vname+"' via channel "+cname+"\n Please click on 'All Videos' to see";
                    tr.broadcastNotify(s,uname,cname);
                    
                    }catch(Exception ex)
                    {
                        out.writeUTF("error in video Uploading");
                    }
                }
                else if(line.equals("req@channelList"))
                {
                    try{
                    String sql="SELECT count(channelname) from Channel where state='online' ";
                    ResultSet rs=st.executeQuery(sql);
                    boolean res=rs.next();
                    if(res)
                    {
                        String s=rs.getString(1);
                        out.writeUTF(s);
                        
                        String sqlin="SELECT channelname from Channel where state='online ' ";
                        ResultSet rsin=st.executeQuery(sqlin);
                        while(rsin.next())
                        {
                            //System.out.println(rsin.getString("channelname"));
                            out.writeUTF(rsin.getString(1));
                        }
                        
                    }
                    else
                    out.writeUTF("0");
                    
                    }catch(Exception e)
                    {
                        System.out.println("Error in req channel");
                    }
                }
                
                else if(line.equals("req@myChannelList"))
                {
                    try{
                        
                    String sql="SELECT count(channelname) from Channel where username='"+uname+"' ";
                    ResultSet rs=st.executeQuery(sql);
                    boolean res=rs.next();
                    if(res)
                    {
                        String s=rs.getString(1);
                        out.writeUTF(s);
                        
                        String sqlin="SELECT channelname from Channel where username='"+uname+"' ";
                        ResultSet rsin=st.executeQuery(sqlin);
                        while(rsin.next())
                        {
                            //System.out.println(rsin.getString("channelname"));
                            out.writeUTF(rsin.getString(1));
                        }
                        
                    }
                    else
                    out.writeUTF("0");
                    
                    }catch(Exception e)
                    {
                        System.out.println("Error in req my channel");
                    }
                }
                
                else if(line.equals("req@subChannelList"))
                {
                    try{
                        
                    String sql="SELECT count(channelname) from Subscription where username='"+uname+"' ";
                    ResultSet rs=st.executeQuery(sql);
                    boolean res=rs.next();
                    if(res)
                    {
                        String s=rs.getString(1);
                        out.writeUTF(s);
                        
                        String sqlin="SELECT channelname from Subscription where username='"+uname+"' ";
                        ResultSet rsin=st.executeQuery(sqlin);
                        while(rsin.next())
                        {
                            //System.out.println(rsin.getString("channelname"));
                            out.writeUTF(rsin.getString(1));
                        }
                        
                    }
                    else
                    out.writeUTF("0");
                    
                    }catch(Exception e)
                    {
                        System.out.println("Error in req subscribed channel");
                    }
                }
                
                else if(line.equals("req@getWatchLater"))
                {
                    try{
                        
                    String sql="SELECT count(videopath) from Watchlater where username='"+uname+"' ";
                    ResultSet rs=st.executeQuery(sql);
                    boolean res=rs.next();
                    if(res)
                    {
                        String s=rs.getString(1);
                        out.writeUTF(s);
                        
                        String sqlin="SELECT videopath from Watchlater where username='"+uname+"' ";
                        ResultSet rsin=st.executeQuery(sqlin);
                        while(rsin.next())
                        {
                            //System.out.println(rsin.getString("channelname"));
                            out.writeUTF(rsin.getString(1));
                        }
                        
                    }
                    else
                    out.writeUTF("0");
                    
                    }catch(Exception e)
                    {
                        System.out.println("Error in req getWatchlater");
                    }
                }
                
                else if(line.equals("req@getAllVideos"))
                {
                    try{
                    String sql="SELECT count(videopath) from Videos where state='online' ";
                    ResultSet rs=st.executeQuery(sql);
                    boolean res=rs.next();
                    if(res)
                    {
                        String s=rs.getString(1);
                        out.writeUTF(s);
                        
                        String sqlin="SELECT videoname, videopath, views,time,channelname from Videos where state='online' order by time DESC";
                        ResultSet rsin=st.executeQuery(sqlin);
                        while(rsin.next())
                        {
                            out.writeUTF(rsin.getString(1));
                            out.writeUTF(rsin.getString(2));
                            out.writeUTF(rsin.getString(3));
                            out.writeUTF(rsin.getString(4));
                            out.writeUTF(rsin.getString(5));
                        }
                        
                    }
                    else
                    out.writeUTF("0");
                    
                    }catch(Exception e)
                    {
                        System.out.println("Error in req all Videos");
                    }
                }
                
                else if(line.equals("req@getTrending"))
                {
                    try{
                    String sql="SELECT count(videopath) from Videos where state='online' ";
                    ResultSet rs=st.executeQuery(sql);
                    boolean res=rs.next();
                    if(res)
                    {
                        String s=rs.getString(1);
                        out.writeUTF(s);
                        
                        String sqlin="SELECT videoname, videopath, views,time,channelname from Videos where state='online' order by trendview DESC";
                        ResultSet rsin=st.executeQuery(sqlin);
                        while(rsin.next())
                        {
                            out.writeUTF(rsin.getString(1));
                            out.writeUTF(rsin.getString(2));
                            out.writeUTF(rsin.getString(3));
                            out.writeUTF(rsin.getString(4));
                            out.writeUTF(rsin.getString(5));
                        }
                        
                    }
                    else
                    out.writeUTF("0");
                    
                    }catch(Exception e)
                    {
                        System.out.println("Error in req trending Videos");
                    }
                }
                
                else if(line.equals("req@getAllVideosOfChannel"))
                {
                    try{
                        String ch=in.readUTF();
                    String sql="SELECT count(videopath) from Videos where channelname='"+ch+"' and state='online' order by views DESC";
                    ResultSet rs=st.executeQuery(sql);
                    boolean res=rs.next();
                    if(res)
                    {
                        String s=rs.getString(1);
                        out.writeUTF(s);
                        
                        String sqlin="SELECT videoname, videopath, views,time,channelname from Videos where channelname='"+ch+"' and state='online' order by views DESC";
                        ResultSet rsin=st.executeQuery(sqlin);
                        while(rsin.next())
                        {
                            out.writeUTF(rsin.getString(1));
                            out.writeUTF(rsin.getString(2));
                            out.writeUTF(rsin.getString(3));
                            out.writeUTF(rsin.getString(4));
                            out.writeUTF(rsin.getString(5));
                        }
                        
                    }
                    else
                    out.writeUTF("0");
                    
                    }catch(Exception e)
                    {
                        System.out.println("Error in req all Videos of a particular channel");
                    }
                    System.out.println("querry ends");
                }   
                
                else if(line.equals("req@search"))
                {
                    try{
                        
                    String ch=in.readUTF();
                    String sql="SELECT count(videopath) from Videos where state='online' and videoname like '%"+ch+"%' or tags like '%"+ch+"%' or channelname like '%"+ch+"%'";
                    ResultSet rs=st.executeQuery(sql);
                    boolean res=rs.next();
                    if(res)
                    {
                        String s=rs.getString(1);
                        out.writeUTF(s);
                        
                        String sqlin="SELECT videoname, videopath, views,time,channelname from Videos where state='online' and videoname like '%"+ch+"%' or tags like '%"+ch+"%' or channelname like '%"+ch+"%'";
                        ResultSet rsin=st.executeQuery(sqlin);
                        while(rsin.next())
                        {
                            out.writeUTF(rsin.getString(1));
                            out.writeUTF(rsin.getString(2));
                            out.writeUTF(rsin.getString(3));
                            out.writeUTF(rsin.getString(4));
                            out.writeUTF(rsin.getString(5));
                        }
                        
                    }
                    else
                    out.writeUTF("0");
                    
                    }catch(Exception e)
                    {
                        System.out.println("Error in req search");
                    }
                } 
                
                else if(line.equals("req@videoIP"))
                {
                    String path=in.readUTF();
                   
                    String sql="SELECT username FROM Videos WHERE videopath= '"+path+"'";
                    ResultSet rs=st.executeQuery(sql);
                    boolean res=rs.next();
                    if(res)
                    {
                        String s=rs.getString(1);
                        Socket sco=tr.getSocketFromUname(s);
                        out.writeUTF("success");
                        out.writeUTF(""+sco.getInetAddress());
                        
                        String sqlin="UPDATE Videos SET views=views+1 WHERE videopath='"+path+"'";
                        st.executeUpdate(sqlin);
                        sqlin="UPDATE Videos SET trendview=trendview+1 WHERE videopath='"+path+"'";
                        st.executeUpdate(sqlin);
                        
                        
                    }
                    else
                    out.writeUTF("Error in req video IP");
                }
                
                else if(line.equals("req@uploadComment"))
                {
                    String vid=in.readUTF();
                    String com=in.readUTF();
                    
                    
                    try{
                    String sql="insert into Comments(videopath,username,comment) values('"+vid+"','"+uname+"','"+com+"')";
                    st.executeUpdate(sql);
                    out.writeUTF("success");
                    
                    }catch(Exception ex)
                    {
                        out.writeUTF("error in comment Uploading");
                    }
                    
                }
                
                else if(line.equals("req@getComments"))
                {
                    
                    try{
                        
                        String vid=in.readUTF();
                      
                    String sql="SELECT count(*) from Comments where videopath='"+vid+"'";
                      
                    ResultSet rs=st.executeQuery(sql);
                    
                    boolean res=rs.next();
                    if(res)
                    {
                        String s=rs.getString(1);
                        System.out.println(s);
                        out.writeUTF(s);
                        
                        String sqlin="SELECT username,comment,time from Comments where videopath='"+vid+"'";
                        ResultSet rsin=st.executeQuery(sqlin);
                        while(rsin.next())
                        {
                            out.writeUTF(rsin.getString(1));
                            out.writeUTF(rsin.getString(2));
                            out.writeUTF(rsin.getString(3));
                        }
                        
                    }
                    else
                    {
                        out.writeUTF("0");
                        System.out.println("Error in query");
                    }
                    
                    }catch(Exception e)
                    {
                        System.out.println("Error in req Comments");
                    }
                }
                
                
                
                else if(line.equals("req@subscribe"))
                {
                    //System.out.println("reaches in ");
                    String vid=in.readUTF();
                    //System.out.println(vid);                    
                    try{
                    String sql="insert into Subscription(channelname,username) values('"+vid+"','"+uname+"')";
                    st.executeUpdate(sql);
                    
                    }catch(Exception ex)
                    {
                        System.out.println("Error in subscribe request");
                    }
                }
                
                else if(line.equals("req@unsubscribe"))
                {
                    String vid=in.readUTF();
                                        
                    try{
                    String sql="DELETE from Subscription where channelname='"+vid+"' and username='"+uname+"'";
                    st.executeUpdate(sql);
                    
                    }catch(Exception ex)
                    {
                        System.out.println("Error in unsubscribe request");
                    }
                }
                
                else if(line.equals("req@isLiked"))
                {
                    try{
                        
                        String vid=in.readUTF();
                      
                    String sql="SELECT count(*) from Likes where videopath='"+vid+"' and username='"+uname+"'";
                      
                    ResultSet rs=st.executeQuery(sql);
                    System.out.println("like runs");
                    boolean res=rs.next();
                    if(res)
                    {
                        String s=rs.getString(1);
                        out.writeUTF(s);          
                    }
                    else
                    {
                        out.writeUTF("0");
                    }
                    
                    }catch(Exception e)
                    {
                        System.out.println("Error in req isLiked");
                    }
                }
                
                else if(line.equals("req@setLiked"))
                {
                    String vid=in.readUTF();
                                        
                    try{
                        
                    String sql="select channelname from Videos where videopath='"+vid+"'";
                    ResultSet rs=st.executeQuery(sql);
                    boolean res=rs.next();
                    String ch=rs.getString(1);
                        
                    sql="insert into Likes(videopath,username,channelname) values('"+vid+"','"+uname+"','"+ch+"')";
                    st.executeUpdate(sql);
                    
                    }catch(Exception ex)
                    {
                        System.out.println("Error in setLiked request");
                    }
                }
                
                else if(line.equals("req@setDisliked"))
                {
                    String vid=in.readUTF();
                                        
                    try{
                    String sql="DELETE from Likes where videopath='"+vid+"' and username='"+uname+"'";
                    st.executeUpdate(sql);
                    
                    }catch(Exception ex)
                    {
                        System.out.println("Error in setDisLiked request");
                    }
                }
                
                else if(line.equals("req@isWatchLater"))
                {
                    try{
                        
                        String vid=in.readUTF();
                      
                    String sql="SELECT count(*) from Watchlater where videopath='"+vid+"' and username='"+uname+"'";
                      
                    ResultSet rs=st.executeQuery(sql);
                    //System.out.println("like runs");
                    boolean res=rs.next();
                    if(res)
                    {
                        String s=rs.getString(1);
                        out.writeUTF(s);          
                    }
                    else
                    {
                        out.writeUTF("0");
                    }
                    
                    }catch(Exception e)
                    {
                        System.out.println("Error in req isWatchLater");
                    }
                }
                
                else if(line.equals("req@addToWatch"))
                {
                    String vid=in.readUTF();
                                        
                    try{
                    String sql="insert into Watchlater(videopath,username) values('"+vid+"','"+uname+"')";
                    st.executeUpdate(sql);
                    
                    }catch(Exception ex)
                    {
                        System.out.println("Error in adding to watch later request");
                    }
                }
                
                else if(line.equals("req@removeFromWatch"))
                {
                    String vid=in.readUTF();
                                        
                    try{
                    String sql="DELETE from Watchlater where videopath='"+vid+"' and username='"+uname+"'";
                    st.executeUpdate(sql);
                    
                    }catch(Exception ex)
                    {
                        System.out.println("Error in removing watchlater request");
                    }
                }
                
                else if(line.equals("req@getLikeCount"))
                {
                    try{
                        
                        String vid=in.readUTF();
                      
                    String sql="SELECT count(*) from Likes where videopath='"+vid+"'";
                      
                    ResultSet rs=st.executeQuery(sql);
                    System.out.println("like count runs");
                    
                    boolean res=rs.next();
                    if(res)
                    {
                        String s=rs.getString(1);
                        out.writeUTF(s);          
                    }
                    else
                    {
                        out.writeUTF("0");
                    }
                    
                    }catch(Exception e)
                    {
                        System.out.println("Error in req getLikeCount");
                    }
                }
                
                
                else if(line.equals("req@getViewsCount"))
                {
                    try{
                        
                        String vid=in.readUTF();
                      
                    String sql="SELECT views from Videos where videopath='"+vid+"'";
                      
                    ResultSet rs=st.executeQuery(sql);
                    System.out.println("view count runs");
                    
                    boolean res=rs.next();
                    if(res)
                    {
                        String s=rs.getString(1);
                        out.writeUTF(s);          
                    }
                    else
                    {
                        out.writeUTF("0");
                    }
                    
                    }catch(Exception e)
                    {
                        System.out.println("Error in req getViewCount");
                    }
                }
                
                else if(line.equals("req@broadcastMsg"))
                {
                    String str=in.readUTF();
                    str=uname+"requested "+str;
                    tr.broadcastRequest(str, uname);
                }
                
                else if(line.equals("req@allDetails"))
                {
                    try{
                        
                        String ch=in.readUTF();
                        
                        String sql="Select description from Channel where channelname='"+ch+"'";
                        ResultSet rs=st.executeQuery(sql);
                        boolean res=rs.next();
                        String s=rs.getString(1);
                        out.writeUTF(s);          
                       
                        sql="select sum(views) from Videos where channelname='"+ch+"'";
                        rs=st.executeQuery(sql);
                        res=rs.next();
                        if(res)
                        {
                            s=rs.getString(1);
                            if(s==null)
                            out.writeUTF("0");
                            else
                            out.writeUTF(s);    
                        }
                         else
                            out.writeUTF("0");
                        System.out.println("333333");
                        
                        sql="select count(*) from Likes where channelname='"+ch+"'";
                        rs=st.executeQuery(sql);
                        res=rs.next();
                        if(res)
                        {
                            s=rs.getString(1);
                            if(s==null)
                            out.writeUTF("0");
                            else
                            out.writeUTF(s);
                        }
                         else
                            out.writeUTF("0");
                        
                        sql="select count(*) from Subscription where channelname='"+ch+"'";
                        rs=st.executeQuery(sql);
                        res=rs.next();
                        if(res)
                        {
                            s=rs.getString(1);
                            if(s==null)
                            out.writeUTF("0");
                            else
                            out.writeUTF(s);
                        }
                         else
                            out.writeUTF("0");
                        
                        
                        
                    }catch(Exception e)
                    {
                        System.out.println("Error in sending all details");
                    }
                }
                
                else if(line.equals("req@exit"))
                {
                    String sqlin="UPDATE Videos SET state = 'offline' WHERE username ='"+uname+"'";
                    st.executeUpdate(sqlin);
                    sqlin="UPDATE Channel SET state = 'offline' WHERE username ='"+uname+"'";
                    tr.makeOffline(uname);
                    tr.broadcast();
                    st.executeUpdate(sqlin);
                    in.close();
                    out.close();
                    socket.close();
                    break;
                }
                
                
            } catch (Exception ex) {
                System.out.println("Error in listener of sockets");
            }
        }
    }
}

/*
    this class handle multiple client
    this is basically a echo server. One server an more than one client
*/

class MultiServer extends Thread
{
    private Tracker tr=null;
    private ServerSocket sr;
    private DataInputStream  in  = null;
    
    public MultiServer(Tracker tr)
    {
        try {
            this.tr=tr;
            sr=new ServerSocket(9004);
            this.start();
        } catch (IOException ex) {
           System.out.println("Error in constructor of MultiServer class");
        }
        
        
        
    }
    
    public void run()
    {
        while(true)
        {
            try {
                
                System.out.println("Waiting");
                Socket sc=sr.accept();
                tr.listUpdate(sc);
                Lis ls=new Lis();
                ls.listenM(tr,sc);
                
            } catch (Exception ex) {
                System.out.println("here");
            }
        }
    }
    
}

/*
   this is the class that create form 
   ui side of trakcer server
*/

public class Tracker extends javax.swing.JFrame {

    private MultiServer ms=null;
    private int conNum=0;
    private Socket[] socArr=new Socket[100];
    private DataOutputStream[] outArr=new DataOutputStream[100];
    private String[] uName=new String[100];
    private int[] status=new int[100];
    private DefaultListModel model=new DefaultListModel(); 
    private Connection con=null;
    
    public Tracker() {
        initComponents();
        Connect_db cob=new Connect_db();
        con=cob.still_connecting();
        if(con==null)
            System.out.println("Error in database connection");
        
        wait5min w5m=new wait5min();
        w5m.start();
    }
    
    Connection getConObj()
    {
        return con;
    }
    
    void broadcast()
    {
        try{
        for(int i=0;i<conNum;++i)
        {
            if(status[i]==0)
                outArr[i].writeUTF("req@userOffline");
        }
        }catch(Exception e)
        {
            System.out.println("Error in broadcasting");
        }
    }
    
    void broadcastRequest(String s,String un)
    {
        try{
            
          for(int i=0;i<conNum;++i)
          {
            if(status[i]==0  && !(uName[i].equals(un)))
            {
                outArr[i].writeUTF("req@request");
                outArr[i].writeUTF(s);
            }
                
          }
        }
        catch(Exception e)
        {
            System.out.println("Error in broadcasting request");
        }
    }
    
    void broadcastNotify(String s,String un,String ch)
    {
        try{
        for(int i=0;i<conNum;++i)
        {
            if(status[i]==0  && !(uName[i].equals(un)))
            {
                outArr[i].writeUTF("req@newVideo");
                outArr[i].writeUTF(s);
                outArr[i].writeUTF(ch);
            }
                
        }
        }catch(Exception e)
        {
            System.out.println("Error in broadcasting");
        }
    }
    
    void setOutStream(String un,DataOutputStream o)
    {
        outArr[this.getPortFromUname(un)]=o;
    }
    
    void makeOffline(String un)
    {
        status[this.getPortFromUname(un)]=1;
    }
    
    int getPortFromSocket(Socket sc)
    {
        for(int i=0;i<conNum;++i)
            if(socArr[i]==sc && status[i]==0)
                return i;
        System.out.println("Error in function get port socket");
        return -1;
    }
    
    int getPortFromUname(String un)
    {
        for(int i=0;i<conNum;++i)
            if(uName[i].equals(un) && status[i]==0 )
                return i;
        System.out.println("Error in function get port uname");
        return -1;
    }
    
    void setUnameFromSocket(Socket sc,String un)
    {
        int i=this.getPortFromSocket(sc);
        if(i!=-1)
        {
            uName[i]=un;
        }
        else
        {
            System.out.println("error in uname updation");
        }
    }
    
    Socket getSocketFromUname(String n)
    {
        int i=this.getPortFromUname(n);
        if(i!=-1)
        {
            return socArr[i];
        }
        else
        {
            System.out.println("Error in function returninng socket");
            return null;
        }
    }
    
    void listUpdate(Socket sc)
    {
        try{
        String str=sc+" is connected at port "+conNum;
        System.out.println(str);
        socArr[conNum]=sc;
        model.addElement(str);
        conNum++;
        }
        catch(Exception e)
        {
            System.out.println("error in list update");
        }
    }
    
    
    

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        list = new javax.swing.JList<>();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Server");
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setText("Start Tracker Server ");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 50, -1, -1));

        jScrollPane1.setViewportView(list);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 45, 0, 10));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/server.jpg"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(-30, -70, 1490, 930));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        try {
            ms=new MultiServer(this);
            list.setModel(model);
            jButton1.setVisible(false);
        } catch (Exception ex) {
           System.out.println("error in server starting");
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Tracker.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Tracker.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Tracker.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Tracker.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Tracker().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList<String> list;
    // End of variables declaration//GEN-END:variables
}
