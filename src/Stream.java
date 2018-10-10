import com.sun.jna.Native;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.mrl.RtspMrl;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import com.sun.jna.NativeLibrary;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.*;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JSlider;
import javax.swing.text.StyledDocument;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.headless.HeadlessMediaPlayer;

//this is a listner that is used to toggle between full screen mode

class MyListener  extends MouseAdapter
{
    
   private Stream st=null;
   private int tog=0;
   
   MyListener(Stream s)
   {
       st=s;
   }
    
  public void mouseClicked (MouseEvent e) 
  {       			
    if (e.getModifiers() == MouseEvent.BUTTON3_MASK)
    {
        JSlider js=st.getJS();
        JPanel pan=st.getPan();
        JPanel cpan=st.getComPane();
        
        if(tog==0)
        {
            
            js.setVisible(false);
            pan.setVisible(false);
            cpan.setVisible(false);
            tog=1;
        }
        else
        {
            js.setVisible(true);
            pan.setVisible(true);
            cpan.setVisible(true);
            tog=0;
        }
    }
  }
  
}

/*
    this class plays media that is streamed on a requested client
*/

public class Stream extends javax.swing.JFrame implements Runnable{

    private DataOutputStream dout,dos,serout;
    private DataInputStream din,dis,serin;
    private final EmbeddedMediaPlayer mediaPlayerComponent;
    private static String name,ip;
    private static String stream,sub;
    private static Socket s,socket,sersoc;
    private static String video,author,date,course;
    private Timer timer;
    private double rate=0;
    private static UserSpace us;
    
    JSlider getJS()
    {
        return slider;
    }
    
    JPanel getPan()
    {
        return jPanel5;
    }
    
    JPanel getComPane()
    {
        return jPanel2;
    }
    

    
    public Stream(UserSpace us,Socket sersoc,Socket s,String stream,String vide,String ip)
    {
        initComponents();
        this.us=us;
        this.ip=ip;
        this.sersoc=sersoc;
        
        getContentPane().setBackground(new java.awt.Color(255,255,153));
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setTitle(vide);
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
            exitProcedure();
           }
        });
        
        this.s = s;
        this.video = vide;
        this.stream = stream;
        
        try {
            
            serout = new DataOutputStream(sersoc.getOutputStream());
            serin = new DataInputStream(sersoc.getInputStream());
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,"Error in server socket in Stream constructor");
        }
        
        
        this.commentListUpdate();
        this.isLiked();
        this.getLikesCount();
        this.getViewsCount();
        this.isWatchLater();
        
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "VLC");
        Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
        
        canvas.setBackground(Color.black);
        
        MediaPlayerFactory factory = new MediaPlayerFactory();
        mediaPlayerComponent = factory.newEmbeddedMediaPlayer();
        
        EmbeddedMediaPlayerComponent mPlayerComponent;
    
        String address;
        try{
            System.out.println(ip+" "+stream);
            socket = new Socket(ip,8554);
            dout = new DataOutputStream(socket.getOutputStream());
            din = new DataInputStream(socket.getInputStream());
            address = new RtspMrl().host("@"+ip).port(5555).path("/"+stream).value();
            System.out.println(address);
            
            mediaPlayerComponent.setVideoSurface(factory.newVideoSurface(canvas));
            mediaPlayerComponent.setFullScreen(true);
            
            mediaPlayerComponent.playMedia(address);
            
            canvas.addMouseListener(new MyListener(this));
            
            dout.writeUTF("change");
            dout.writeUTF("0.000000");
            slider.setMinimum(0);
            slider.setValue(0);
            
            dos = new DataOutputStream(s.getOutputStream());
            dis = new DataInputStream(s.getInputStream());
            
            Thread t = new Thread(this);
            t.start();
            
            System.out.println("tutorspoint_client.Stream.<init>()");
            
            timer = new Timer(100, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        dout.writeUTF("position");
                        float f = Float.parseFloat(din.readUTF());
            
                        slider.setValue(Math.round(f * 100));
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null,"Socket Closed");
                    }
                }
            });
            timer.start();
            
        }
        catch (Exception e){
            
            JOptionPane.showMessageDialog(null,"Excepitons as "+e.getMessage()+" in Stream class constructor");
        }
        
        
    }
    
    
    public void exitProcedure() {
        
        try {
            
            if(btn_play.getText().equals("Pause"))
            mediaPlayerComponent.pause();
            dout.writeUTF("change");
            dout.writeUTF(""+(1.0f));
            dout.writeUTF("exit");
            timer.stop();
            System.out.println("Socket s going to be closed");
            socket.close();
            System.out.println("Socket closed");
            this.setVisible(false);
            us.myChannelListUpdate();
            us.allVideoListUpdate();
            us.myChannelListUpdate();
            us.channelListUpdate();
            us.watchLaterUpdate();
            us.setVisible(true);
            us.watchHistoryUpdate();
        } 
        catch (IOException ex) {
            JOptionPane.showMessageDialog(null,ex.getMessage());
        }
    }
    
    void getViewsCount()
    {
        try{
            serout.writeUTF("req@getViewsCount");
            serout.writeUTF(video);
            lbl_view.setText(serin.readUTF());
        }
        catch(Exception e)
        {
            System.out.println("Error in fetching like count");
        }
    }
    
    void getLikesCount()
    {
        try{
            serout.writeUTF("req@getLikeCount");
            serout.writeUTF(video);
            lbl_like.setText(serin.readUTF());
        }
        catch(Exception e)
        {
            System.out.println("Error in fetching like count");
        }
    }
    
    void isLiked()
    {
        try{
            serout.writeUTF("req@isLiked");
            serout.writeUTF(video);
            int n=Integer.parseInt(serin.readUTF());
            if(n==1)
                btn_like.setText("Dislike");
            else
                btn_like.setText("Like");
            
        }
        catch(Exception e)
        {
            System.out.println("Error in fetching like option");
        }
    }
    
    void isWatchLater()
    {
        try{
            serout.writeUTF("req@isWatchLater");
            serout.writeUTF(video);
            int n=Integer.parseInt(serin.readUTF());
            if(n==1)
                btn_watch.setText("Remove from WatchLater");
            else
                btn_watch.setText("Add To WatchLater");
            
        }
        catch(Exception e)
        {
            System.out.println("Error in fetching like option");
        }
    }
    
    void commentListUpdate()
    {
        
        tp2.setText("");
        String un,com,time;
        try {
            serout.writeUTF("req@getComments");
            serout.writeUTF(video);
            int n=Integer.parseInt(serin.readUTF());
  
            for(int i=0;i<n;++i)
            {
                un=serin.readUTF();
                com=serin.readUTF();
                time=serin.readUTF();
                com=un+" :: "+com+"\n"+time+"\n\n";
                StyledDocument doc=tp2.getStyledDocument();
                doc.insertString(0,com, null);  
            }
            
            tp2.setCaretPosition(0);
            
        } catch (Exception ex) {
            System.out.println("error in fetching channel list");
        }
    }
    
    private static String formatRtspStream(String serverAddress, int serverPort, String id) {
        StringBuilder sb = new StringBuilder(60);
        sb.append(":sout=#rtp{sdp=rtsp://@");
        sb.append(serverAddress);
        sb.append(':');
        sb.append(serverPort);
        sb.append('/');
        sb.append(id);
        sb.append("mux=ts}");
        return sb.toString();
    }
    
 
    
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        canvas = new java.awt.Canvas();
        slider = new javax.swing.JSlider();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        btn_play = new javax.swing.JButton();
        btn_like = new javax.swing.JButton();
        btn_watch = new javax.swing.JButton();
        btn_comment = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tp1 = new javax.swing.JTextPane();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tp2 = new javax.swing.JTextPane();
        lbl_like = new javax.swing.JLabel();
        lbl_view = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 153));

        jPanel1.setBackground(new java.awt.Color(255, 255, 153));
        jPanel1.setMinimumSize(new java.awt.Dimension(472, 240));
        jPanel1.setPreferredSize(new java.awt.Dimension(677, 240));
        jPanel1.setLayout(new java.awt.BorderLayout());

        canvas.setMinimumSize(new java.awt.Dimension(472, 204));
        jPanel1.add(canvas, java.awt.BorderLayout.CENTER);

        slider.setBackground(new java.awt.Color(255, 255, 153));
        slider.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                sliderFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                sliderFocusLost(evt);
            }
        });
        slider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                sliderMouseReleased(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                sliderMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                sliderMouseEntered(evt);
            }
        });
        jPanel1.add(slider, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        btn_play.setText("Pause");
        btn_play.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        btn_play.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_playActionPerformed(evt);
            }
        });
        jPanel6.add(btn_play);

        btn_like.setText("Like");
        btn_like.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_likeActionPerformed(evt);
            }
        });
        jPanel6.add(btn_like);

        btn_watch.setText("Add To WatchLater");
        btn_watch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_watchActionPerformed(evt);
            }
        });
        jPanel6.add(btn_watch);

        btn_comment.setText("                Post Comment              ");
        btn_comment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_commentActionPerformed(evt);
            }
        });
        jPanel6.add(btn_comment);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 1234, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel5, java.awt.BorderLayout.PAGE_END);

        tp1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tp1FocusGained(evt);
            }
        });
        jScrollPane1.setViewportView(tp1);

        jLabel1.setText("Comments :");

        jLabel2.setText("Views:");

        jLabel3.setText("Likes:");

        jScrollPane3.setViewportView(tp2);

        lbl_like.setText("-1");

        lbl_view.setText("-1");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbl_like, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbl_view, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 365, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane3))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbl_like, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbl_view, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.LINE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void sliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sliderMouseReleased
        // Slider mouse released event
        try {
            dout.writeUTF("change");
            if (slider.getValue() / 100 < 1) {
                dout.writeUTF(""+((float) slider.getValue() / 100));
            }
            else{
                dout.writeUTF(""+1.0f);
                exitProcedure();
            }
            
        } 
        catch (IOException ex) {
            Logger.getLogger(Stream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_sliderMouseReleased

    private void sliderFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_sliderFocusGained
        // TODO add your handling code here:
        
        
    }//GEN-LAST:event_sliderFocusGained

    private void sliderFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_sliderFocusLost
        // TODO add your handling code here:
        
    }//GEN-LAST:event_sliderFocusLost

    private void sliderMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sliderMouseEntered
        // TODO add your handling code here:
       
    }//GEN-LAST:event_sliderMouseEntered

    private void sliderMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sliderMouseExited
 
    }//GEN-LAST:event_sliderMouseExited

    private void btn_playActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_playActionPerformed
       
        if(btn_play.getText().equals("Pause"))
        {
            mediaPlayerComponent.pause();
            btn_play.setText("Play");
        }
        else
        {
            mediaPlayerComponent.play();
            btn_play.setText("Pause");
        }
        
    }//GEN-LAST:event_btn_playActionPerformed

    private void btn_commentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_commentActionPerformed
        if(!tp1.getText().equals(""))
        try {
            
            serout.writeUTF("req@uploadComment");
            serout.writeUTF(video);
            serout.writeUTF(tp1.getText());    
            tp1.setText(serin.readUTF());
            
            this.commentListUpdate();
            
        } catch (IOException ex) {
            Logger.getLogger(Stream.class.getName()).log(Level.SEVERE, null, "errorr in btn_comment");
        }
        else
        {
            tp1.setText("Write something");
        }
        
    }//GEN-LAST:event_btn_commentActionPerformed

    private void tp1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tp1FocusGained
        
        tp1.setText("");
        
    }//GEN-LAST:event_tp1FocusGained

    private void btn_likeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_likeActionPerformed
       try{
        if(btn_like.getText().equals("Like"))
        {
            btn_like.setText("Dislike");
            serout.writeUTF("req@setLiked");
            serout.writeUTF(video);
        }
        else
        {
            btn_like.setText("Like");
            serout.writeUTF("req@setDisliked");
            serout.writeUTF(video);
        }
        
       }catch(Exception e)
       {
           System.out.println("error in like and dislike");
       }
       
       this.getLikesCount();
    }//GEN-LAST:event_btn_likeActionPerformed

    private void btn_watchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_watchActionPerformed
        try{
            
        if(btn_watch.getText().equals("Add To WatchLater"))
        {
            
            btn_watch.setText("Remove from WatchLater");
            serout.writeUTF("req@addToWatch");
            serout.writeUTF(video);
        }
        else
        {
            btn_watch.setText("Add To WatchLater");
            serout.writeUTF("req@removeFromWatch");
            serout.writeUTF(video);
        }
        
       }catch(Exception e)
       {
           System.out.println("error in watch Later");
       }

    }//GEN-LAST:event_btn_watchActionPerformed

    
    
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
            java.util.logging.Logger.getLogger(Stream.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Stream.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Stream.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Stream.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Stream(us,sersoc,s,stream,video,ip).setVisible(true);
               
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_comment;
    private javax.swing.JButton btn_like;
    private javax.swing.JButton btn_play;
    private javax.swing.JButton btn_watch;
    private java.awt.Canvas canvas;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lbl_like;
    private javax.swing.JLabel lbl_view;
    private javax.swing.JSlider slider;
    private javax.swing.JTextPane tp1;
    private javax.swing.JTextPane tp2;
    // End of variables declaration//GEN-END:variables

    @Override
    public void run() {
        try {
        
            
        } 
        catch (Exception ex) {
            JOptionPane.showMessageDialog(null,"Some exception occured "+ex.getMessage());
        }
    }
}
