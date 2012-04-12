package cide;


import java.io.*;
import java.net.*;




public class chat extends javax.swing.JFrame {

    public chat(String add) {
        this.add = add;
        initComponents();
    }
 public static void main(String args[])
 {

 }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane2 = new javax.swing.JTextPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        displaymsg = new javax.swing.JTextPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        usermsg = new javax.swing.JTextPane();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        sendbutton = new javax.swing.JButton();

        jScrollPane2.setViewportView(jTextPane2);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jScrollPane1.setViewportView(displaymsg);

        jScrollPane3.setViewportView(usermsg);

        jButton1.setText("Send File");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("File Transfer");

        sendbutton.setText("send");
        sendbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendbuttonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGap(58,58,58)
                .addComponent(sendbutton)

                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()

                .addGap(48,48,48)
                .addComponent(jButton1)
                .addContainerGap(75, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sendbutton)
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                  )
                .addContainerGap(29, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
// TODO add your handling code here:

}

private void sendbuttonActionPerformed(java.awt.event.ActionEvent evt) {
// TODO add your handling code here:
    if (st == null) {
        st = new SendThread(add, 8089);
    }
    new Thread(st).run();
    synchronized (displaymsg)
    {
    displaymsg.setText(displaymsg.getText() + "\n" + "me:" + usermsg.getText()+"\n");
    }
        usermsg.setText("");
}


 class SendThread implements Runnable
 {
  Socket outgoing;
  OutputStream outStream;
  public SendThread(String add, int port)
  {
   try
   {
    outgoing = new Socket(add, port);
   }
   catch (UnknownHostException ex)
   {
    System.out.println("User Not Online");
   }
   catch (IOException ex)
   {
    System.out.println("Connection Problem");
   }
   try
   {
    outStream = outgoing.getOutputStream();
   }
   catch (IOException ex)
   {
    System.out.println("Connection Problem");
   }
  }

  public void run()
  {

   System.out.println("In Send Thread");
   PrintWriter out = new PrintWriter(outStream, true);
   String msg=usermsg.getText();
  out.println(msg);
   out.flush();
   System.out.println("Leaving Send Thread");
  }
 }
    SendThread st = null;

    String add;
    // Variables declaration - do not modify
    javax.swing.JTextPane displaymsg;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextPane jTextPane2;
    private javax.swing.JButton sendbutton;
    private javax.swing.JTextPane usermsg;
    // End of variables declaration
}
