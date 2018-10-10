
import java.io.BufferedReader;
import java.io.FileReader;   
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class History {
  
    
    BufferedReader br=null;
    FileReader fr=null;
    FileWriter fw=null;
    
   
    public static void main(String args[])
    {
        History h=new History();
        
        h.set("set top\n","/home/mojojojo/StarkHubChannels/history");
        
        String his=h.get("/home/mojojojo/StarkHubChannels/history");
        System.out.println(his);
    }
    
    void set(String str,String p)
    {
        try {
            str=str+get(p);
            fw=new FileWriter(p);
            fw.write(str);
            fw.close();
            
        } catch (Exception ex) {
            Logger.getLogger(History.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    String get(String p)
    {
        String s="";   
        try{
            
            fr=new FileReader(p);
            br=new BufferedReader(fr);
            
            String curr;
            
            while((curr=br.readLine())!=null)
            {
                s=s+curr;
                s=s+"\n";
            }
            
            if(br!=null)
                br.close();
            if(fr!=null)
                fr.close();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        return s;
    }
    
    
}

    

