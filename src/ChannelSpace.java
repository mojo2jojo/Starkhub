
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


/*
    this class is used for following things
    1: Display information of channel
    2: Uploading of video on local space
    3: Channel level access for a logged in user
*/

public class ChannelSpace extends javax.swing.JFrame
{

    private String chname=null;
    private Socket sc;
    private DataOutputStream out=null;
    private DataInputStream in=null;
    private String head=null; //this variable stores path of the local directory where user uploaded video are saved
    private String uname=null;
    UserSpace us=null;
    private double rat=0.0;
    
    public ChannelSpace() 
    {
        initComponents();
    }
    
    public ChannelSpace(UserSpace us,String head,String chname,Socket sc,String uname) 
    {
        initComponents();
        this.us=us;
        this.chname=chname;
        this.sc=sc;
        this.head=head;
        this.uname=uname;
        
        this.setDefaultCloseOperation(this.DO_NOTHING_ON_CLOSE);
        
        try 
        {
            out = new DataOutputStream(sc.getOutputStream());
            in=new DataInputStream(sc.getInputStream());
            
        } catch (Exception ex) 
        {
            System.out.println("Error in ChannelSpace constructor");
        }
        
        this.updateDetails();
        
    }

    
    void updateDetails()
    {
        try{
        
        out.writeUTF("req@allDetails");
        out.writeUTF(chname);
        String des=in.readUTF();
        String views=in.readUTF();
        String like=in.readUTF();
        String sub=in.readUTF();
        
        //rating .6 sub   .3 likes     .1 views
        
        lbl_name.setText(chname);
        lbl_des.setText("About: "+des);
        lbl_view.setText("Total Views: "+views);
        lbl_likes.setText("Total Likes: "+like);
        lbl_sub.setText("Subscriber: "+sub);
        lbl_rat.setText("Rating :"+(0.6*Integer.parseInt(sub)+ 0.3 * Integer.parseInt(like)+0.1*Integer.parseInt(views)));
        
        }catch(Exception e)
        {
            System.out.println("Error in uploading all details");
        }
    }
    
    void exitProcedure()
    {
         this.setVisible(false);
            us.myChannelListUpdate();
            us.allVideoListUpdate();
            us.myChannelListUpdate();
            us.channelListUpdate();
            us.watchLaterUpdate();
            us.setVisible(true);
    }
    

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        tf1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        lbl_name = new javax.swing.JLabel();
        lbl_des = new javax.swing.JLabel();
        lbl_view = new javax.swing.JLabel();
        lbl_likes = new javax.swing.JLabel();
        lbl_sub = new javax.swing.JLabel();
        lbl_rat = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setText("Upload Video");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 475, -1, -1));
        getContentPane().add(tf1, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 293, 318, -1));

        jLabel1.setForeground(new java.awt.Color(251, 233, 233));
        jLabel1.setText("Enter Video Name: ");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 298, -1, -1));

        jButton2.setText("Home");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 40, 130, -1));

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 353, 318, 104));

        jLabel2.setForeground(new java.awt.Color(237, 218, 218));
        jLabel2.setText("Enter Tags :");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 353, 107, -1));

        lbl_name.setFont(new java.awt.Font("URW Gothic L", 0, 24)); // NOI18N
        lbl_name.setText("lbl_name");
        getContentPane().add(lbl_name, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 33, 256, -1));

        lbl_des.setForeground(new java.awt.Color(253, 239, 239));
        lbl_des.setText("lbl_des");
        getContentPane().add(lbl_des, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 88, 568, 18));

        lbl_view.setForeground(new java.awt.Color(250, 238, 238));
        lbl_view.setText("lbl_view");
        getContentPane().add(lbl_view, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 124, 324, -1));

        lbl_likes.setForeground(new java.awt.Color(253, 243, 243));
        lbl_likes.setText("lbl_likes");
        getContentPane().add(lbl_likes, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 159, 330, -1));

        lbl_sub.setForeground(new java.awt.Color(235, 221, 221));
        lbl_sub.setText("lbl_sub");
        getContentPane().add(lbl_sub, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 194, 400, -1));

        lbl_rat.setForeground(new java.awt.Color(254, 238, 238));
        lbl_rat.setText("lbl_rat");
        getContentPane().add(lbl_rat, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 229, 370, -1));

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cspace.jpg"))); // NOI18N
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(-350, 0, 1370, 530));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        /*
            action event of upload video button
            selected video gets saved in local space and path is send to tracker server
        */        
        
        String fpath=null;
        JFileChooser jf=new JFileChooser();
        jf.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jf.showDialog(this,"Select Video To UPLOAD!!");
        File file=jf.getSelectedFile();
        if(file!=null)
        {
            fpath=file.getParent();
            String d=head+"/"+chname+"";
            System.out.println(fpath);//+"\n"+d);
            String ff=fpath+"/"+file.getName()+"";
            System.out.println(ff);
        
        try{
            
                 //this process builder class copies video from selected path to specified folder 
            
                ProcessBuilder pb1=new ProcessBuilder("/bin/sh","-c","cp '"+ff+"' '"+d+"'");
                System.out.println(file);
                pb1.directory(new File(head));
                Process p1=pb1.start();
                while(p1.isAlive()); 
           
                out.writeUTF("req@uploadVideo");
                out.writeUTF(chname);
                out.writeUTF(head+"/"+chname+"/"+file.getName());
                out.writeUTF(tf1.getText());
                out.writeUTF(jTextArea1.getText());
                
                String status=in.readUTF();
            
                if(status.equals("success"))
                {   
                    JOptionPane.showMessageDialog(null,"uploading success");
                }
                else
                {
                    JOptionPane.showMessageDialog(null,status);
                }
                
                
            }catch(Exception e)
                {
                    System.out.println("Error in process Building");
                }
            }
        
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
  
           this.exitProcedure();
        
    }//GEN-LAST:event_jButton2ActionPerformed

    
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
            java.util.logging.Logger.getLogger(ChannelSpace.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ChannelSpace.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ChannelSpace.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ChannelSpace.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ChannelSpace().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JLabel lbl_des;
    private javax.swing.JLabel lbl_likes;
    private javax.swing.JLabel lbl_name;
    private javax.swing.JLabel lbl_rat;
    private javax.swing.JLabel lbl_sub;
    private javax.swing.JLabel lbl_view;
    private javax.swing.JTextField tf1;
    // End of variables declaration//GEN-END:variables
}
