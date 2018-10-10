
import java.awt.*;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;


class Listner extends Thread{
    
    UserSpace us;
    private Socket sc;
    private DataOutputStream out,sout=null;
    private DataInputStream in,sin=null;
    private String ip;
    
    Listner(UserSpace us,String ip,DataOutputStream out,DataInputStream in)
    {
        this.us=us;
        this.ip=ip;
        this.out=out;
        this.in=in;
        this.start();
    }
    
    public void run()
    {
        try {
            
            out.writeUTF("req@makeListner");
            System.out.println("Listner starting");
            int portnum=Integer.parseInt(in.readUTF());
            sc=new Socket(ip,portnum);
            System.out.println("Listner started");
            
            sout=new DataOutputStream(sc.getOutputStream());
            sin=new DataInputStream(sc.getInputStream());
            
            while(true)
            {
                String line;
                line = sin.readUTF();
                System.out.println(line);
                
                if(line.equals("req@userOffline"))
                {
                    us.allVideoListUpdate();
                }
                
                else if(line.equals("req@newVideo"))
                {
                    String sr=sin.readUTF();
                    String ch=sin.readUTF();
                    if(us.isSubscribed(ch)==1)
                    us.setNotification(sr);
                }
                
                else if(line.equals("req@request"))
                {
                    String sr=sin.readUTF();
                    us.setNotification(sr);
                }
                
            }
            
            
        } catch (IOException ex) {
            System.out.println("Error in Listner Thread");
        }
    }
    
}


/*
    this is a userspace class
    here user gets list of all video and channel
    user can play videos and also create new channel
    many more functionality will be added in the future
*/

public class UserSpace extends javax.swing.JFrame {

    private Socket sc;
    private DataOutputStream out=null;
    private DataInputStream in=null;
    private DefaultListModel mymodel=new DefaultListModel();
    private DefaultListModel cmodel=new DefaultListModel();
    private DefaultListModel submodel=new DefaultListModel();
    private DefaultTableModel tmodel =null; 
    private String uname;
    private String head="/home/mojojojo/StarkHubChannels";
    
    
    
    public UserSpace() {
        
        initComponents();
       
        myChannelList.setModel(mymodel);
        channelList.setModel(cmodel);
         
    }
    
    
    public UserSpace(Socket sc,String uname)
    {
       
        initComponents();
        this.uname=uname;
        myChannelList.setModel(mymodel);
        channelList.setModel(cmodel);
        subsChannel.setModel(submodel);
        tmodel=(DefaultTableModel) jTable1.getModel();
        this.sc=sc;
        lbl_name.setText("Welcome: "+uname);
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
            exitProcedure();
           }
        });
        
         try {
             
            out = new DataOutputStream(sc.getOutputStream());
            in=new DataInputStream(sc.getInputStream());
            
        } catch (Exception ex) {
            System.out.println("Error in UserSpace construct");
        }
        
        jTable1.addMouseListener(new MouseAdapter(){
        public void mousePressed(MouseEvent me)
        {
            JTable jt=(JTable)me.getSource();
            int row=jt.getSelectedRow();
            int col=1;
            String path=(String)jt.getValueAt(row, col);
            if(me.getClickCount()==2)
            {
                playVideo(path);
            }
        }
    }
    ); 
        
        
        this.myChannelListUpdate();
        this.channelListUpdate();
        this.allVideoListUpdate();
        this.allSubscribedChannel();
        this.watchLaterUpdate();
        this.watchHistoryUpdate();
        
        InetAddress ia= sc.getInetAddress();
        String ip=ia.getHostAddress();
        
        Listner l=new Listner(this,ip,out,in);
        
    }
    
    
    void exitProcedure()
    {
        try {
            out.writeUTF("req@exit");
            sc.close();
        } catch (IOException ex) {
            System.out.println("Error in exiting");
        }
    }
    
    
    void playVideo(String path)
    {
        Socket s_stream;
        
        History h=new History();
        h.set(path+"\n", head+"/history");
        this.watchHistoryUpdate();
        
        DataOutputStream out_stream=null;
        DataInputStream in_stream=null;
          
        String ip;
        if(path==null)
            return;
        try {
            out.writeUTF("req@videoIP");
            out.writeUTF(path);
            
            String status=in.readUTF();
            if(status.equals("success"))
            {
                ip=in.readUTF(); 
                ip=ip.substring(1,ip.length());
                //JOptionPane.showMessageDialog(null,ip);
                
                try {
                    s_stream = new Socket(ip,10000);
                    out_stream = new DataOutputStream(s_stream.getOutputStream());
                    in_stream = new DataInputStream(s_stream.getInputStream());
                    //JOptionPane.showMessageDialog(null,"successful connection");
                    try{
                
                       out_stream.writeUTF("play");
                       //String video="/home/mojojojo/Desktop/despacito.mp4";
                       out_stream.writeUTF(path);
                       String streamName = in_stream.readUTF();
                       //System.out.println("Yessssss");
                       this.setVisible(false);
                       Stream stream = new Stream(this,sc,s_stream,streamName,path,ip);
                       stream.setVisible(true);
                       }
                       catch(Exception e){
                           JOptionPane.showMessageDialog(null,"Some exceiton occured : "+e.getMessage());
                       }
                } 
                catch (Exception ex) 
                {
                JOptionPane.showMessageDialog(null,"Error in Stream creation");
                }

                
            }
            else
            {
                JOptionPane.showMessageDialog(null,status);
            }    
            
        } catch (IOException ex) {
            System.out.println("error in Playing video");
        }
    }
    
    
    int isSubscribed(String ch)
    {
        int n=subsChannel.getModel().getSize();
        for(int i=0;i<n;++i)
        {
            if(ch.equals((String)submodel.getElementAt(i)))
                return 1;
        }
        return 0;
    }
    
    void watchHistoryUpdate()
    {
        History h=new History();
        String str=h.get(head+"/history");
        hisarea.setText(str);
    }
    
    void watchLaterUpdate()
    {
        watcharea.setText("");
        try {
            out.writeUTF("req@getWatchLater");
            int n=Integer.parseInt(in.readUTF());
            for(int i=1;i<=n;++i)
            {
                watcharea.append(i+": "+in.readUTF()+"\n");
            }
            
        } catch (IOException ex) {
            System.out.println("error in fetching watch later list");
        }
    }
    
    void channelListUpdate()
    {
        cmodel.removeAllElements();
        try {
            out.writeUTF("req@channelList");
            int n=Integer.parseInt(in.readUTF());
            //cmodel.addElement(n);
            for(int i=0;i<n;++i)
            {
                cmodel.addElement(in.readUTF());
            }
            
        } catch (IOException ex) {
            System.out.println("error in fetching channel list");
        }
    }
    
    void myChannelListUpdate()
    {
        mymodel.removeAllElements();
        try {
            out.writeUTF("req@myChannelList");
            int n=Integer.parseInt(in.readUTF());
            //cmodel.addElement(n);
            for(int i=0;i<n;++i)
            {
                mymodel.addElement(in.readUTF());
            }
            
        } catch (IOException ex) {
            System.out.println("error in fetching channel list");
        }
    }
    
    void allVideoListUpdate()
    {
        this.channelListUpdate();
        this.allSubscribedChannel();
        
        tmodel.setRowCount(0);
        String d1,d2,d3,d4,d5;
        try{
            out.writeUTF("req@getAllVideos");
            int n=Integer.parseInt(in.readUTF());
            for(int i=0;i<n;++i)
            {
                //videoname, videopath, views,time,channelname
                d1=in.readUTF();
                d2=in.readUTF();
                d3=in.readUTF();
                d4=in.readUTF();
                d5=in.readUTF();
                
                Object[] row = { d1,d2,d3,d4,d5};
                tmodel.addRow(row);
            }
                    
        }
        catch(Exception e)
        {
            System.out.println("Error while inserting data in table");
        }
    }
    
    void allSubscribedChannel()
    {
        submodel.removeAllElements();
        try {
            out.writeUTF("req@subChannelList");
            int n=Integer.parseInt(in.readUTF());
            for(int i=0;i<n;++i)
            {
                submodel.addElement(in.readUTF());
            }
            
        } catch (IOException ex) {
            System.out.println("error in fetching subscribed channel list");
        }
    }
    
    
    void allVideoOfChannel(String str)
    {
        tmodel.setRowCount(0);
        String d1,d2,d3,d4,d5;
        try{
            out.writeUTF("req@getAllVideosOfChannel");
            out.writeUTF(str);
            int n=Integer.parseInt(in.readUTF());
            for(int i=0;i<n;++i)
            {
                //videoname, videopath, views,time,channelname
                d1=in.readUTF();
                d2=in.readUTF();
                d3=in.readUTF();
                d4=in.readUTF();
                d5=in.readUTF();
                
                Object[] row = { d1,d2,d3,d4,d5};
                tmodel.addRow(row);
            }
                    
        }
        catch(Exception e)
        {
            System.out.println("Error while inserting data in table");
        }
    }
    
    void allTrending()
    {
        tmodel.setRowCount(0);
        String d1,d2,d3,d4,d5;
        try{
            out.writeUTF("req@getTrending");
            int n=Integer.parseInt(in.readUTF());
            for(int i=0;i<n;++i)
            {
                //videoname, videopath, views,time,channelname
                d1=in.readUTF();
                d2=in.readUTF();
                d3=in.readUTF();
                d4=in.readUTF();
                d5=in.readUTF();
                
                Object[] row = { d1,d2,d3,d4,d5};
                tmodel.addRow(row);
            }
                    
        }
        catch(Exception e)
        {
            System.out.println("Error while inserting data in table");
        }
    }
    
    void setNotification(String str)
    {
        notpane.setText(str);
    }
    
    
    

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        channelList = new javax.swing.JList<>();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton2 = new javax.swing.JButton();
        sField = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        myChannelList = new javax.swing.JList<>();
        jLabel2 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        notpane = new javax.swing.JTextPane();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        subsChannel = new javax.swing.JList<>();
        jLabel4 = new javax.swing.JLabel();
        subBut = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        watcharea = new javax.swing.JTextArea();
        jScrollPane6 = new javax.swing.JScrollPane();
        hisarea = new javax.swing.JTextArea();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        reqarea = new javax.swing.JTextArea();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        lbl_name = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("User Space");
        setBackground(new java.awt.Color(9, 4, 4));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setText("Create Channel");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1432, 13, -1, -1));

        channelList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        channelList.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                channelListFocusGained(evt);
            }
        });
        channelList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                channelListValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(channelList);

        getContentPane().add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1260, 180, 270, 200));

        jLabel1.setForeground(new java.awt.Color(249, 239, 239));
        jLabel1.setText("All Channels :");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1450, 150, -1, -1));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name ", "VideoPath", "Views", "Date Time", "Channel"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(2).setMaxWidth(500);
        }

        getContentPane().add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 180, 942, 510));

        jButton2.setText("All Videos");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 60, 137, -1));
        getContentPane().add(sField, new org.netbeans.lib.awtextra.AbsoluteConstraints(307, 64, 394, -1));

        jButton3.setText("search");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(748, 62, 85, -1));

        myChannelList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        myChannelList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                myChannelListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(myChannelList);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1260, 420, 270, 202));

        jLabel2.setForeground(new java.awt.Color(251, 246, 246));
        jLabel2.setText("Your Channels:");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1440, 390, -1, 20));

        jButton4.setText("Go To Channel");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(1260, 630, 270, -1));

        notpane.setEditable(false);
        notpane.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                notpaneFocusGained(evt);
            }
        });
        jScrollPane4.setViewportView(notpane);

        getContentPane().add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 440, 280, 220));

        jLabel3.setForeground(new java.awt.Color(254, 238, 238));
        jLabel3.setText("Notification: ");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 400, -1, -1));

        subsChannel.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        subsChannel.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                subsChannelValueChanged(evt);
            }
        });
        jScrollPane5.setViewportView(subsChannel);

        getContentPane().add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, 280, 200));

        jLabel4.setForeground(new java.awt.Color(249, 239, 239));
        jLabel4.setText("Subscribed Channel: ");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 150, -1, -1));

        subBut.setText("Subscribe");
        subBut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subButActionPerformed(evt);
            }
        });
        getContentPane().add(subBut, new org.netbeans.lib.awtextra.AbsoluteConstraints(1390, 60, 138, -1));

        jLabel5.setForeground(new java.awt.Color(253, 239, 239));
        jLabel5.setText("Watch Later List: ");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 690, -1, -1));

        watcharea.setEditable(false);
        watcharea.setColumns(20);
        watcharea.setRows(5);
        jScrollPane7.setViewportView(watcharea);

        getContentPane().add(jScrollPane7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 730, 475, 237));

        hisarea.setEditable(false);
        hisarea.setColumns(20);
        hisarea.setRows(5);
        jScrollPane6.setViewportView(hisarea);

        getContentPane().add(jScrollPane6, new org.netbeans.lib.awtextra.AbsoluteConstraints(1056, 741, 470, 240));

        jLabel6.setForeground(new java.awt.Color(251, 241, 241));
        jLabel6.setText("Watch History:");
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(1440, 690, -1, -1));

        jLabel7.setForeground(new java.awt.Color(251, 241, 241));
        jLabel7.setText("Write a request of video and it will be send to every node ");
        getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 740, -1, -1));

        reqarea.setColumns(20);
        reqarea.setRows(5);
        jScrollPane8.setViewportView(reqarea);

        getContentPane().add(jScrollPane8, new org.netbeans.lib.awtextra.AbsoluteConstraints(533, 787, 494, 130));

        jButton5.setText("Send Request ");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(727, 947, -1, -1));

        jButton6.setText("Trending");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(1130, 60, 84, -1));

        lbl_name.setFont(new java.awt.Font("Noto Sans", 0, 18)); // NOI18N
        lbl_name.setForeground(new java.awt.Color(246, 242, 242));
        lbl_name.setText("Welcome :");
        getContentPane().add(lbl_name, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 13, 677, 31));

        jLabel9.setBackground(new java.awt.Color(16, 6, 6));
        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Awesome-Black-4K-HD-Wallpaper.jpg"))); // NOI18N
        jLabel9.setText(" ");
        jLabel9.setOpaque(true);
        getContentPane().add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 140, 1570, 910));

        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wallpaper2you_469217.jpg"))); // NOI18N
        getContentPane().add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1570, 140));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
       
        CreateChannel cc=new CreateChannel(this,sc,uname);
        cc.setVisible(true);
        this.setVisible(false);
        
    }//GEN-LAST:event_jButton1ActionPerformed

    private void channelListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_channelListValueChanged
        String str=channelList.getSelectedValue();
        if(str==null)
            return;
        this.allVideoOfChannel(str);
        int i=this.isSubscribed(str);
        if(i==1)
        subBut.setText("Unsubscribe");
        else
        subBut.setText("Subscribe");
    }//GEN-LAST:event_channelListValueChanged

    private void channelListFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_channelListFocusGained
    }//GEN-LAST:event_channelListFocusGained

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
       this.allVideoListUpdate();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
    tmodel.setRowCount(0);
        String d1,d2,d3,d4,d5;
        try{    
        if(!sField.getText().equals(""))
        {
            out.writeUTF("req@search");
            out.writeUTF(sField.getText());
            int n=Integer.parseInt(in.readUTF());
            //JOptionPane.showMessageDialog(null,""+n);
            for(int i=0;i<n;++i)
            {
                //videoname, videopath, views,time,channelname
                d1=in.readUTF();
                d2=in.readUTF();
                d3=in.readUTF();
                d4=in.readUTF();
                d5=in.readUTF();
                
                Object[] row = { d1,d2,d3,d4,d5};
                tmodel.addRow(row);
            }        
        }
        else
        {
            sField.setText("type something here");
        }
    }catch(Exception e)
    {
        System.out.println("Error in search");
    }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void myChannelListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_myChannelListValueChanged
      
        
    }//GEN-LAST:event_myChannelListValueChanged

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
    
    if(myChannelList.isSelectionEmpty())
    {
        JOptionPane.showMessageDialog(null, "Select Channel First");
        return;
    }
    String str=myChannelList.getSelectedValue();
    ChannelSpace cp=new ChannelSpace(this,head,str,sc,uname);
    cp.setVisible(true);
    this.setVisible(false);
    
    }//GEN-LAST:event_jButton4ActionPerformed

    private void subButActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subButActionPerformed
       String str=channelList.getSelectedValue();
       if(str==null)
           return ;
       
        try{
        if(subBut.getText().equals("Subscribe"))
        {
            //System.out.println("reaches with "+str);
            out.writeUTF("req@subscribe");
            out.writeUTF(str);
            subBut.setText("Unsubscribe");
        }
        else
        {
            out.writeUTF("req@unsubscribe");
            out.writeUTF(str);
            subBut.setText("Subscribe");
        }
       }catch(Exception e)
       {
           System.out.println("Error in subscribing and unsubscribing");
       }
        this.allSubscribedChannel();
    }//GEN-LAST:event_subButActionPerformed

    private void subsChannelValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_subsChannelValueChanged
        String str=subsChannel.getSelectedValue();
        if(str==null)
            return;
        this.allVideoOfChannel(str);
    }//GEN-LAST:event_subsChannelValueChanged

    private void notpaneFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_notpaneFocusGained
        notpane.setText("Waiting for new NOTIFICATION ::::");
    }//GEN-LAST:event_notpaneFocusGained

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        String str=reqarea.getText();
        reqarea.setText("");
        
        if(str.equals(""))
        {
            reqarea.setText("Enter some message ");
            return;
        }
        
        try{
            out.writeUTF("req@broadcastMsg");
            out.writeUTF(str);
            
        }
        catch(Exception e)
        {
            System.out.println("Error in request broadcast");
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed

        this.allTrending();
    }//GEN-LAST:event_jButton6ActionPerformed

    
    
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
            java.util.logging.Logger.getLogger(UserSpace.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UserSpace.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UserSpace.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UserSpace.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new UserSpace().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<String> channelList;
    private javax.swing.JTextArea hisarea;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lbl_name;
    private javax.swing.JList<String> myChannelList;
    private javax.swing.JTextPane notpane;
    private javax.swing.JTextArea reqarea;
    private javax.swing.JTextField sField;
    private javax.swing.JButton subBut;
    private javax.swing.JList<String> subsChannel;
    private javax.swing.JTextArea watcharea;
    // End of variables declaration//GEN-END:variables
}
