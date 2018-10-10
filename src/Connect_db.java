import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/*
    this class is used at server side 
    it establishes connection to mysql database and return connection object
*/

public class Connect_db {
    
    //this block is used to check various query  
    
    public static void main(String args[])
    {
        int i;
        try {
            Connect_db cb=new Connect_db();
            Connection con=cb.still_connecting();
            Statement st=con.createStatement();
            
            String sqlin="SELECT channelname from Channel";
            ResultSet rsin=st.executeQuery(sqlin);
            while(rsin.next())
            {
                System.out.println(rsin.getString("channelname"));
                //out.writeUTF(rs.getString(1));
            }
            
        } catch (SQLException ex) {
            i=10;
            JOptionPane.showMessageDialog(null,"Error in Connect_db main funtion");
        }
        
    }
    
    // function establishes connection and return connection object
    
    public Connection still_connecting(){ 
        
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/tracker","root","ankitverma");
            System.out.println(con);
            return con;
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, e);
            return null;
        }
    }
}