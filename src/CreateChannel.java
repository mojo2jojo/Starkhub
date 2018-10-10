
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import javax.swing.JOptionPane;

/*
    this class is used by client to create new channel
    it will take many input and also update server database by sending request
*/

public class CreateChannel extends javax.swing.JFrame {

    private Socket sc;
    private DataOutputStream out=null;
    private DataInputStream in=null;
    private String head="/home/mojojojo/StarkHubChannels";
    private String uname="";
    UserSpace us=null;
    
    public CreateChannel() {
        initComponents();
    }

    public CreateChannel(UserSpace us,Socket sc,String uname) {
        
        initComponents();
        this.sc=sc;
        this.uname=uname;
        this.us=us;
        this.setDefaultCloseOperation(this.DO_NOTHING_ON_CLOSE);
        
        try {
            
            out = new DataOutputStream(sc.getOutputStream());
            in=new DataInputStream(sc.getInputStream());
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,ex+" Error in Create Channel constructor");
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

        jLabel1 = new javax.swing.JLabel();
        t1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        ta1 = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Create Channel");
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setForeground(new java.awt.Color(251, 229, 229));
        jLabel1.setText("Channel Name:");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 120, -1, -1));
        getContentPane().add(t1, new org.netbeans.lib.awtextra.AbsoluteConstraints(198, 116, 356, -1));

        jLabel2.setForeground(new java.awt.Color(254, 238, 238));
        jLabel2.setText("Description:");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 180, -1, -1));

        ta1.setColumns(20);
        ta1.setRows(5);
        jScrollPane1.setViewportView(ta1);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(198, 180, 356, 140));

        jButton1.setText("Create");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 350, 96, -1));

        jButton2.setText("Home");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 30, 92, -1));

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wallpaper2you_469217.jpg"))); // NOI18N
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(-120, -10, 1030, 440));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // this button create new channel and also notifies server 
        
        try{
                ProcessBuilder pb1=new ProcessBuilder("/bin/sh","-c","mkdir '"+t1.getText()+"'");
                pb1.directory(new File(head));
                Process p1=pb1.start();
                while(p1.isAlive()); 
                
                out.writeUTF("req@newChannel");
                out.writeUTF(t1.getText());
                out.writeUTF(ta1.getText());
                out.writeUTF(head);
                
                String status=in.readUTF();
                
                if(status.equals("success"))
                {   
                    this.setVisible(false);
                    us.allVideoListUpdate();
                    us.myChannelListUpdate();
                    us.channelListUpdate();
                    ChannelSpace cp=new ChannelSpace(us,head,t1.getText(),sc,uname);
                    cp.setVisible(true); 
                }
                else
                {
                    JOptionPane.showMessageDialog(null,status);
                }
                
           }
           catch(Exception e)
           {
                JOptionPane.showMessageDialog(null,e+" Error in Channel creation check jButton1 code");
                
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
            java.util.logging.Logger.getLogger(CreateChannel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CreateChannel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CreateChannel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CreateChannel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CreateChannel().setVisible(true);
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
    private javax.swing.JTextField t1;
    private javax.swing.JTextArea ta1;
    // End of variables declaration//GEN-END:variables
}
