/*
 * CIDEApp.java
 */

package cide;

import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.*;
public class CIDEApp extends javax.swing.JFrame{




    class GUIfinal extends javax.swing.JFrame {

    HashMap<InetAddress, String> users;
    final String BROADCASTCODE = "0",  SINGLECODE = "1",  REMOVECODE = "2";

    class Broadcaster implements Runnable {

        private InetAddress group;
        private byte[] buf;
        private MulticastSocket socket = null;
        private int port;
        String code;
        public Broadcaster(String nick, String code, String groupAdd, int port) {
            synchronized (users) {
             //   users.clear();
            }
            this.code=code;
            try {
                socket = new MulticastSocket();
                socket.setTimeToLive(255);
            } catch (IOException ex) {
                System.out.println("Error Forming Socket");
            }
            this.port = port;
            buf = new byte[256];
            buf = (code + nick).getBytes();
            try {
                group = InetAddress.getByName(groupAdd);
                socket.joinGroup(group);
            } catch (UnknownHostException ex) {
                System.out.println("Error Forming Group");
            } catch (IOException ex) {
                System.out.println("Error Forming Group");
            }
        }

        public void run() {
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, port);
            try {
                socket.send(packet);
                users.clear();

                System.out.println("Sent Packet");
            } catch (IOException ex) {
                System.out.println("Error Sending Packet");
            }
        }

        void close() {
            try {
                socket.leaveGroup(group);
            } catch (IOException ex) {
                System.out.println("Error Leaving Group");
            }
            socket.close();
        }
    }

    class MultiRecieve implements Runnable {

        private MulticastSocket socket,  sendSocket;
        private InetAddress group;
        byte[] bufr = new byte[256];
        byte[] bufs = new byte[256];
        int port = 0;
        String received;

        public MultiRecieve(String nick, String code, String groupAdd, int port) {
            this.port = port;
            bufs = (code + nick).getBytes();
            try {
                socket = new MulticastSocket(port);
                sendSocket = new MulticastSocket();
                socket.setTimeToLive(255);
                sendSocket.setTimeToLive(255);
            } catch (IOException ex) {
                System.out.println("Error Forming Socket");
            }
            try {
                group = InetAddress.getByName(groupAdd);
            } catch (UnknownHostException ex) {
                System.out.println("Error Forming Group");
            }
            try {
                socket.joinGroup(group);
                sendSocket.joinGroup(group);
            } catch (IOException ex) {
                System.out.println("Error Forming Group");
            }
        }

        public void run() {

            System.out.println("Receiving Multicasts...");
            for (;;) {
                DatagramPacket rec = new DatagramPacket(bufr, bufr.length);
                DatagramPacket send;
                InetAddress sender = null;
                try {
                    socket.receive(rec);
                    System.out.println("received");
                    sender = rec.getAddress();
                    received = new String(rec.getData(), 0, rec.getLength());
                    synchronized (users) {
                        if (received.startsWith(REMOVECODE) && users.containsKey(sender))
                        {
                            System.out.println("hi");
                            int i,j=0;
                            boolean b = true;
                            String m[] =new String[50];
                            for (i = 0; i < users.size(); i++) {
                                String s = ((String) msglab.getModel().getElementAt(i)).substring(10);

                                if (s.compareTo(sender.getHostAddress()) == 0) {
                                    b = false;
                                    c[i] = null;
                                    users.remove(sender);
                                }
                                else
                                {
                                    m[j]=new String();
                                    m[j]=((String) msglab.getModel().getElementAt(i));
                                    j++;
                                }
                            }
                                final String list[] = m;
                            msglab.setModel(new javax.swing.AbstractListModel() {

                                String[] strings = list;

                                public int getSize() {
                                    return strings.length;
                                }

                                public Object getElementAt(int i) {
                                    return strings[i];
                                }
                            });



                        }
                        else if (received.startsWith(BROADCASTCODE) || !users.containsKey(sender))
                        {
                            users.put(sender, received);
                            System.out.println(users.size());
                            String s[] = new String[users.size()];
                            int i;
                            for (i = 0; i < users.size() - 1; i++) {
                                s[i] = (String) msglab.getModel().getElementAt(i);
                            }
                            int j = 10 - received.substring(1).length();
                            String m = "";
                            for (int k = 0; k < j; k++) {
                                m = m + " ";
                            }
                            s[i] = received.substring(1) + m + sender.getHostAddress();
                            final String list[] = s;
                            msglab.setModel(new javax.swing.AbstractListModel() {

                                String[] strings = list;

                                public int getSize() {
                                    return strings.length;
                                }

                                public Object getElementAt(int i) {
                                    return strings[i];
                                }
                            });


                            send = new DatagramPacket(bufs, bufs.length, sender, port);
                            sendSocket.send(send);
                            System.out.println(received.substring(1) + sender);
                        }

                    }
                } catch (IOException ex) {
                    System.out.println("Error Recieing Packet");
                }
            }
        }

        void close() {
            try {
                socket.leaveGroup(group);
                sendSocket.leaveGroup(group);
            } catch (IOException ex) {
                System.out.println("Error Leaving Group");
            }
            socket.close();
        }
    }

    public GUIfinal() {

        s = new ServerThread(8089);
        (new Thread(s)).start();
        Properties props = System.getProperties();
        props.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperties(props);
        users = new HashMap<InetAddress, String>(50);
        String str;
         java.util.Scanner scan=null;
                    try{
                         String file = getClass().getResource("/cide/resources/nick.txt").toString();
                         scan = new java.util.Scanner(new java.io.FileInputStream(file.substring(5)));
                    } catch (Exception ex) {
                         System.out.println(ex);
                    }
                      str=scan.nextLine();
        (new Thread(new MultiRecieve(str, SINGLECODE, "230.0.0.1", 9090))).start();
        (new Thread(new Broadcaster(str, BROADCASTCODE, "230.0.0.1", 9090))).start();

    }



private void msglabValueChanged(javax.swing.event.ListSelectionEvent evt) {
    if (msglab.getSelectedIndex() != -1) {
        if (c[msglab.getSelectedIndex()] == null || c[msglab.getSelectedIndex()].isVisible() == false) {
            c[msglab.getSelectedIndex()] = new chat(((String) msglab.getModel().getElementAt(msglab.getSelectedIndex())).substring(10));
            c[msglab.getSelectedIndex()].setVisible(true);

        }
        msglab.clearSelection();
    }
}



    class ServerThread implements Runnable {

        private ServerSocket server;

        public ServerThread(int port) {
            try {
                server = new ServerSocket(port);
            } catch (UnknownHostException ex) {
                System.out.println("Unknown Host");
            } catch (IOException ex) {
                System.out.println("I/O Problem");
            }
        }

        public void run() {
            try {
                while (true) {
                    RecieveThread rt = new RecieveThread(server.accept());
                    System.out.println("New Client Connected");
                    (new Thread(rt)).start();
                }
            } catch (IOException ex) {
                System.out.println("I/O Problem");
            }
        }
    }

    class RecieveThread implements Runnable {

        Socket incoming;
        InputStream inStream;
        String nick;

        public RecieveThread(Socket incoming) {
            this.incoming = incoming;
            try {
                inStream = incoming.getInputStream();
            } catch (IOException ex) {
                System.out.println("I/O Problem");
            }
        }

        public void run() {
            System.out.println("Recieving");
            nick = incoming.getInetAddress().getHostName();
            Scanner in;
            in = new Scanner(inStream);
            while (in.hasNextLine()) {
                String line = in.nextLine();
                System.out.println(incoming.getInetAddress().getHostAddress());
                int i;
                boolean b = true;
                for (i = 0; i < users.size(); i++) {
                    String s = ((String) msglab.getModel().getElementAt(i)).substring(10);
                    if (s.compareTo(incoming.getInetAddress().getHostAddress()) == 0) {
                        b = false;
                        if (c[i] == null || c[i].isVisible() == false) {
                            c[i] = new chat(s);
                            c[i].setVisible(true);
                        }
                        synchronized (c[i].displaymsg) {
                            c[i].displaymsg.setText(c[i].displaymsg.getText() + nick + ": " + line + "\n");
                        }
                    }
                }
                System.out.println(b);
                if (b == true) {
                    System.out.println("hi");
                    InetAddress ink = incoming.getInetAddress();
                    String nick = "hi";
                    users.put(ink, nick);
                    System.out.println(users.size());
                    String s[] = new String[users.size()];
                    for (i = 0; i < users.size() - 1; i++) {
                        s[i] = (String) msglab.getModel().getElementAt(i);
                    }
                    int j = 10 - nick.length();
                    String m = "";
                    for (int k = 0; k < j; k++) {
                        m = m + " ";
                    }
                    s[i] = nick + m + ink.getHostAddress();
                    final String list[] = s;
                    msglab.setModel(new javax.swing.AbstractListModel() {

                        String[] strings = list;

                        public int getSize() {
                            return strings.length;
                        }

                        public Object getElementAt(int i) {
                            return strings[i];
                        }
                    });
                    if (c[i] == null || c[i].isVisible() == false) {
                        c[i] = new chat(incoming.getInetAddress().getHostAddress());
                        c[i].setVisible(true);
                    }
                    synchronized (c[i].displaymsg) {
                        c[i].displaymsg.setText(c[i].displaymsg.getText() + nick + ": " + line + "\n");
                    }
                }
                line = null;
            }
            System.out.println("Recieving Over");
        }
    }
// SendThread st[]=new SendThread[50];
    ServerThread s = null;
    chat c[] = new chat[50];
    // Variables declaration - do not modify

    // End of variables declaration
}
    private class UndoOp {

        private UndoManager undo;
        public UndoOp() {

        undo = new UndoManager();
        Document doc = ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getDocument();
        doc.addUndoableEditListener(new UndoableEditListener() {
        public void undoableEditHappened(UndoableEditEvent evt) {
        undo.addEdit(evt.getEdit());
        }
        });
        jButton10.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent h)
  {
  try {
  if (undo.canUndo()) {
  undo.undo();
  }
  }
  catch (CannotUndoException e) {
  }
  }
  });

         undoMenuItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent h)
  {
  try {
  if (undo.canUndo()) {
  undo.undo();
  }
  }
  catch (CannotUndoException e) {
  }
  }
  });
  }
  }

    public CIDEApp() {
        initComponents();
         syntaxcreator();
        b=false;
        flag = false;
        UndoOp appl = new UndoOp();
        lib();
        console.setFont(new java.awt.Font("Courier New", 0, 14));
    }


    private void lib()
    {
         liburl=getClass().getResource("/cide/resources/DPL").toString().substring(5);
         //liburl="/home/saket/DPL";
          File f1=new File(liburl);
          File s[]=f1.listFiles();

          for(int i=0,j=0;i<s.length;i++)
         {
           int l= s[i].getName().indexOf(".cpp");
               int k= s[i].getName().indexOf(".c");
            if(k!=-1||l!=-1)
            {

                m[j]=s[i].getName();
                j++;
            }
          }
          final String l[]=m;
          liblist.setModel(new javax.swing.AbstractListModel() {
            String[] strings =l;
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
    }

    private void initComponents() {

        setTitle("C-IDE");
        jScrollPane4 = new javax.swing.JScrollPane();
        cndisp = new javax.swing.JLabel();
        v = -1;
         msg = new javax.swing.JPanel();
        lndisp = new javax.swing.JLabel();
        listModel = new DefaultListModel();
        cblist = new javax.swing.JList(listModel);
        cblist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cbdisp = new javax.swing.JLabel();
        libScrollPane = new javax.swing.JScrollPane();
        liblist = new javax.swing.JList();
         jPanel1 = new javax.swing.JPanel();
    jPanel2 = new javax.swing.JPanel();
        libadd = new javax.swing.JButton();
        libdisp = new javax.swing.JLabel();
        console = new javax.swing.JTextPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        Framesave = new javax.swing.JFrame();
        Framefind = new javax.swing.JFrame();
        Framereplace = new javax.swing.JFrame();
        jFrame3 = new javax.swing.JFrame();
        jScrollPane1 = new javax.swing.JScrollPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        TextEdit[0] = new javax.swing.JTextPane();
        jScrollPane[0]=new javax.swing.JScrollPane();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
         newmenu = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        undoMenuItem = new javax.swing.JMenuItem();
        findMenuItem = new javax.swing.JMenuItem();
        findNextMenuItem = new javax.swing.JMenuItem();
        replaceMenuItem = new javax.swing.JMenuItem();
        selectAllMenuItem = new javax.swing.JMenuItem();
        buildMenu = new javax.swing.JMenu();
        compileMenuItem = new javax.swing.JMenuItem();
        runMenuItem = new javax.swing.JMenuItem();
        bookMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        contentsMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();
         FindText = new javax.swing.JTextField();
        findlabel = new javax.swing.JLabel();
        FindNext = new javax.swing.JButton();
        findcancel = new javax.swing.JButton();
        open=new javax.swing.JFileChooser();
        save=new javax.swing.JFileChooser();
              gotolineedit = new javax.swing.JMenuItem();

        replacelab1 = new javax.swing.JLabel();
        Replacefindlabel = new javax.swing.JTextField();
        Replacefind = new javax.swing.JButton();
        replacereplaceall = new javax.swing.JButton();
        Replacelabel = new javax.swing.JTextField();
        replacelab2 = new javax.swing.JLabel();
        replacereplace = new javax.swing.JButton();
        replacecancel = new javax.swing.JButton();


        savelabel = new javax.swing.JLabel();
        saveyes = new javax.swing.JButton();
        saveno = new javax.swing.JButton();

        tabp = new javax.swing.JTabbedPane();
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
//CCPHandler handler = new CCPHandler();

         msg = new javax.swing.JPanel();
        msgsc = new javax.swing.JScrollPane();
        msglab = new javax.swing.JList();
        nicklab = new javax.swing.JLabel();
        chnick = new javax.swing.JButton();
   StatisticsDisplayer = new javax.swing.JTextPane();
     functionBookmarker = new javax.swing.JComboBox(items);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        msglab.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                g.msglabValueChanged(evt);
            }
        });
        msgsc.setViewportView(msglab);
jScrollPane4.setViewportView(StatisticsDisplayer);
        nicklab.setBackground(new java.awt.Color(204, 255, 255));
        nicklab.setText("Nick             IP Address              ");

        chnick.setText("Change Nick");

        javax.swing.GroupLayout msgLayout = new javax.swing.GroupLayout(msg);
        msg.setLayout(msgLayout);
        msgLayout.setHorizontalGroup(
            msgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(msgLayout.createSequentialGroup()
                .addGroup(msgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(msgLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(msgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)

                             .addComponent(msgsc, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                            .addComponent(nicklab, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)))
                    .addGroup(msgLayout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addComponent(chnick)))
                .addContainerGap())
        );
        msgLayout.setVerticalGroup(
            msgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(msgLayout.createSequentialGroup()
                .addComponent(nicklab, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(msgsc, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chnick)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );


        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jToolBar1.setRollover(true);

        liblist.setBorder(new javax.swing.border.MatteBorder(null));
        liblist.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
              liblistValueChanged(evt);
            }
        });

        cblist.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
              cblistValueChanged(evt);
            }
        });

        libScrollPane.setViewportView(liblist);
        ItemListener itemListener = new ItemListener() {
          public void itemStateChanged(ItemEvent itemEvent) {
              BookmarkValueChanged();
         }
        };
        functionBookmarker.addItemListener(itemListener);

        jButton11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cide/resources/newfile.jpg")));
        jButton11.setFocusable(false);
        jButton11.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton11.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton11.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jButton11MouseMoved(evt);
            }
        });
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton11);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cide/resources/toolbar_file_open.png"))); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jButton2MouseMoved(evt);
            }
        });
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton2);

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cide/resources/toolbar_file_save.png"))); // NOI18N
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
      //  jButton3.
        jButton3.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jButton3MouseMoved(evt);
            }
        });
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton3);

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cide/resources/toolbar_delete.gif"))); // NOI18N
        jButton4.setFocusable(false);
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton4.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jButton4MouseMoved(evt);
            }
        });
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton4);
        jToolBar1.add(jSeparator1);

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cide/resources/cut_vista.png"))); // NOI18N
        jButton5.setFocusable(false);
        //jButton5.
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton5.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jButton5MouseMoved(evt);
            }
        });
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton5);

        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cide/resources/copy_vista.png"))); // NOI18N
        jButton6.setFocusable(false);
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        jButton6.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jButton6MouseMoved(evt);
            }
        });
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton6);

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cide/resources/paste_vista.png"))); // NOI18N
        jButton7.setFocusable(false);
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton7.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jButton7MouseMoved(evt);
            }
        });
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton7);
        jToolBar1.add(jSeparator2);

        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cide/resources/button_toolbar_find.gif"))); // NOI18N
        jButton8.setFocusable(false);
        jButton8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton8.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton8.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jButton8MouseMoved(evt);
            }
        });
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton8);

        jButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cide/resources/imagesCAC16DTO.jpg"))); // NOI18N
        jButton9.setFocusable(false);
        jButton9.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton9.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton9.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jButton9MouseMoved(evt);
            }
        });
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton9);

        jButton10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cide/resources/toolbar_edit_undo.png"))); // NOI18N
        jButton10.setFocusable(false);
        jButton10.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton10.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton10.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jButton10MouseMoved(evt);
            }
        });
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton10);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
//        syntaxcreator();
        savelabel.setText("Do You Want To Save The Changes Made?");

        saveyes.setText("Yes");
        saveyes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveyesActionPerformed(evt);
            }
        });

        saveno.setText("No");
        saveno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                savenoActionPerformed(evt);
            }
        });

        Framesave.setSize(450,200);
        javax.swing.GroupLayout Framesavelayout = new javax.swing.GroupLayout( Framesave.getContentPane());
         Framesave.getContentPane().setLayout(Framesavelayout);
        Framesavelayout.setHorizontalGroup(
            Framesavelayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Framesavelayout.createSequentialGroup()
                .addGap(54, 54, 54)
                .addComponent(savelabel, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(49, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, Framesavelayout.createSequentialGroup()
                .addContainerGap(74, Short.MAX_VALUE)
                .addComponent(saveyes)
                .addGap(47, 47, 47)
                .addComponent(saveno)
                .addGap(101, 101, 101))
        );
        Framesavelayout.setVerticalGroup(
            Framesavelayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Framesavelayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(savelabel, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(Framesavelayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveyes)
                    .addComponent(saveno))
                .addContainerGap())

        );

        Framefind.setTitle("Find");

        findlabel.setText("Find What:");

        FindNext.setText("FindNext");

        findcancel.setText("Cancel");
          FindNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FindNextActionPerformed(evt);
            }
        });
 TextEdit[0].addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TextEditKeyTyped(evt);
            }

        });
     TextEdit[0].setFont(new java.awt.Font("Trebuchet MS", 0, 14));
        findcancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findcancelActionPerformed(evt);
            }
        });
Framefind.setSize(530, 150);
        javax.swing.GroupLayout FramefindLayout = new javax.swing.GroupLayout(Framefind.getContentPane());
        Framefind.getContentPane().setLayout(FramefindLayout);
        FramefindLayout.setHorizontalGroup(
            FramefindLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, FramefindLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(findlabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(FindText, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46)
                .addGroup(FramefindLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(findcancel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(FindNext, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(81, 81, 81))
        );
        FramefindLayout.setVerticalGroup(
            FramefindLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FramefindLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(FramefindLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(FindNext)
                    .addComponent(FindText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(findlabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(findcancel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))


        );


        // Framereplace.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        Framereplace.setTitle("Replace");

        replacelab1.setText("Find What:");


        Replacefind.setText("Find Next");
        Replacefind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ReplacefindActionPerformed(evt);
            }
        });

        replacereplaceall.setText("Replace All");
        replacereplaceall.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replacereplaceallActionPerformed(evt);
            }
        });


        replacelab2.setText("Replace With:");

        replacereplace.setText("Replace");
        replacereplace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replacereplaceActionPerformed(evt);
            }
        });

        replacecancel.setText("Cancel");
        replacecancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replacecancelActionPerformed(evt);
            }
        });

        Framereplace.setSize(550, 180);
        javax.swing.GroupLayout  FramereplaceLayout = new javax.swing.GroupLayout(Framereplace.getContentPane());
        Framereplace.getContentPane().setLayout( FramereplaceLayout);
         FramereplaceLayout.setHorizontalGroup(
             FramereplaceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,  FramereplaceLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup( FramereplaceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(replacelab1, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(replacelab2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addGroup( FramereplaceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(Replacelabel)
                    .addComponent(Replacefindlabel, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addGroup( FramereplaceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(replacecancel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(replacereplace, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Replacefind, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(replacereplaceall, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(37, 37, 37))
        );
         FramereplaceLayout.setVerticalGroup(
             FramereplaceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup( FramereplaceLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup( FramereplaceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(replacelab1)
                    .addComponent(Replacefindlabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Replacefind))
                .addGap(3, 3, 3)
                .addComponent(replacereplace)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup( FramereplaceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Replacelabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(replacelab2)
                    .addComponent(replacereplaceall))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(replacecancel)
                .addContainerGap(21, Short.MAX_VALUE))
        );





        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jScrollPane[0].setViewportView(TextEdit[0]);
     // jScrollPane2.setViewportView(linedisplayer);
    //    linedisplayer.setEditable(false);
     //   linedisplayer.setText("1");
        fileMenu.setText("File");


        newmenu.setText("New");
        newmenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newmenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newmenuActionPerformed(evt);
            }
        });
        fileMenu.add(newmenu);

        openMenuItem.setText("Open");

        openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);

        saveMenuItem.setText("Save");
        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setText("Save As ...");
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveAsMenuItem);

        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);

        editMenu.setText("Edit");

        cutMenuItem.setText("Cut");
     cutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        cutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cutMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(cutMenuItem);

        copyMenuItem.setText("Copy");
       copyMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        copyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(copyMenuItem);

        pasteMenuItem.setText("Paste");
       pasteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        pasteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pasteMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(pasteMenuItem);

        deleteMenuItem.setText("Delete");
      deleteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        deleteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(deleteMenuItem);

        undoMenuItem.setText("Undo");
        undoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        undoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                undoMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(undoMenuItem);

        findMenuItem.setText("Find...");
        findMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        findMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(findMenuItem);

        findNextMenuItem.setText("Find Next");
        findNextMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        findNextMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findNextMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(findNextMenuItem);

        replaceMenuItem.setText("Replace...");
        replaceMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_MASK));
        replaceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(replaceMenuItem);

        selectAllMenuItem.setText("Select All");
      selectAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        selectAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(selectAllMenuItem);

        menuBar.add(editMenu);
         gotolineedit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
        gotolineedit.setText("Goto Line");
        gotolineedit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gotolineeditActionPerformed(evt);
            }
        });
           jScrollPane3.setViewportView(console);
         editMenu.add(gotolineedit);
        buildMenu.setText("Build");
        compileMenuItem.setText("Compile");
        buildMenu.add(compileMenuItem);
        compileMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F9, java.awt.event.InputEvent.CTRL_MASK));
        compileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compileMenuItemActionPerformed(evt);
            }
        });
        runMenuItem.setText("Run");
        buildMenu.add(runMenuItem);
        runMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F10, java.awt.event.InputEvent.CTRL_MASK));
        runMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runMenuItemActionPerformed(evt);
            }
        });

        bookMenuItem.setText("Bookmark");
        buildMenu.add(bookMenuItem);
        bookMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bookMenuItemActionPerformed(evt);
            }
        });

        menuBar.add(buildMenu);
        helpMenu.setText("Help");

        contentsMenuItem.setText("Contents");
        helpMenu.add(contentsMenuItem);
        contentsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, java.awt.event.InputEvent.CTRL_MASK));
        contentsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contentsMenuItemActionPerformed(evt);
            }
        });

        aboutMenuItem.setText("About");
        helpMenu.add(aboutMenuItem);
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);


         tabp.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabpStateChanged(evt);
            }
        });

        libadd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                libaddActionPerformed(evt);
            }
        });

         jScrollPane[0].setViewportView(TextEdit[0]);

        tabp.addTab("tab1", jScrollPane[0]);
        setBackground(new java.awt.Color(204, 204, 255));

        jToolBar1.setRollover(true);

        console.setBorder(new javax.swing.border.MatteBorder(null));
        jScrollPane1.setViewportView(console);

        cndisp.setText("CONSOLE");
        cndisp.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(51, 51, 51)));

        //lndisp.setText("LINE NUMBER:");
        //lndisp.setBorder(new javax.swing.border.MatteBorder(null));

        jScrollPane2.setViewportView(liblist);

        libadd.setText("add");

        libdisp.setText("LIBRARY");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(libdisp, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(libadd)
                .addContainerGap(68, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(libdisp, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE,200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(libadd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE))
        );
        jScrollPane3.setViewportView(cblist);

        cbdisp.setText("CODE BACKUP");

       javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbdisp, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(msg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(cbdisp, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(msg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

   javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE)
            .addComponent(cndisp, javax.swing.GroupLayout.DEFAULT_SIZE,1000, Short.MAX_VALUE)
            //.addComponent(lndisp, javax.swing.GroupLayout.DEFAULT_SIZE,1000, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(functionBookmarker, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tabp, javax.swing.GroupLayout.PREFERRED_SIZE, 700, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(functionBookmarker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tabp, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                //.addComponent(lndisp)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cndisp)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        pack();
           g=new GUIfinal();
    }
private void tabpStateChanged(javax.swing.event.ChangeEvent evt) {
// TODO add your handling code here:

}


private void liblistValueChanged(javax.swing.event.ListSelectionEvent evt) {

    if(b==true)
    {
        b=false;
   int ind= liblist.getSelectedIndex();

    String str="";
                TextEdit[number_of_tabs] = new javax.swing.JTextPane();
        jScrollPane[number_of_tabs]=new javax.swing.JScrollPane();
                 jScrollPane[number_of_tabs].setViewportView(TextEdit[number_of_tabs]);

      java.util.Scanner scan=null;

          String url=liburl+"/"+m[ind];
       taburl[number_of_tabs]=url;
    try{
           scan = new java.util.Scanner(new java.io.FileInputStream(url));

                    } catch (Exception ex) {
                         System.out.println(ex);
                    }
                      str=scan.nextLine();
                                str=str+"\n";
                       while (scan.hasNext())
                         {
                          str= str+scan.nextLine();
                              str=  str+"\n";
                         }
        tabp.addTab(m[ind], jScrollPane[number_of_tabs]);

         TextEdit[number_of_tabs].setFont(new java.awt.Font("Trebuchet MS", 0, 14));
                   tabp.setSelectedComponent(jScrollPane[number_of_tabs]);
                   syntaxcreator();
                           UndoOp appl = new UndoOp();
                    TextEdit[number_of_tabs].setText(str);
                     TextEdit[number_of_tabs].addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TextEditKeyTyped(evt);
            }

        });

                number_of_tabs++;
                b=false;
    }
    else if(b==false)
        b=true;

}
private void cblistValueChanged(javax.swing.event.ListSelectionEvent evt) {
    if (flag == false) {
    int ind= cblist.getSelectedIndex();

    String str="";
                TextEdit[number_of_tabs] = new javax.swing.JTextPane();
        jScrollPane[number_of_tabs]=new javax.swing.JScrollPane();
                 jScrollPane[number_of_tabs].setViewportView(TextEdit[number_of_tabs]);

      java.util.Scanner scan=null;

          String url = listModel.getElementAt(ind).toString();
       taburl[number_of_tabs]=url;
    try{
           scan = new java.util.Scanner(new java.io.FileInputStream(url));

                    } catch (Exception ex) {
                         System.out.println(ex);
                    }
                      str=scan.nextLine();
                                str=str+"\n";
                       while (scan.hasNext())
                         {
                          str= str+scan.nextLine();
                              str=  str+"\n";
                         }
          int lain = url.lastIndexOf("/");
        tabp.addTab(url.substring(lain+1, url.length()), jScrollPane[number_of_tabs]);

         TextEdit[number_of_tabs].setFont(new java.awt.Font("Trebuchet MS", 0, 14));
                   tabp.setSelectedComponent(jScrollPane[number_of_tabs]);
                   syntaxcreator();
                           UndoOp appl = new UndoOp();
                    TextEdit[number_of_tabs].setText(str);
                     TextEdit[number_of_tabs].addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TextEditKeyTyped(evt);
            }

        });

                number_of_tabs++;
    }
}

private void BookmarkValueChanged()
{
    String selected = functionBookmarker.getSelectedItem().toString();
    if (!selected.equals("select"))
    {
        BookHighlight(selected);
    }
}

private void BookHighlight(String search)
{
String str=null;
int texpos=0;
if(search==null||search.compareTo("")==0)
    return;
try
{
    str=((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getText();
}
catch(NullPointerException e)
{
}

int i=str.indexOf(search,texpos);
 System.out.println(texpos+" "+i);
 if(i==texpos&&i!=0)
 {
    i=str.indexOf(search,texpos+1);
    if(i==-1)
    {
    i=str.indexOf(search);
    }
   System.out.println(texpos+" "+i);
 }
if(i==-1)
{
    ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).select(0,0);
    return;
}
int ctr=0,pos=0;
while(pos<=i)
{
    ctr++;
    pos=str.indexOf("\r",pos);
    if(pos==-1)
        break;
    pos++;
    //System.out.println(pos);

}
i=i-ctr+1;
int r=i+search.length();
if(i!=-1)
{((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).setSelectionStart(i);
((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).setSelectionEnd(r);
}
}

private void libaddActionPerformed(java.awt.event.ActionEvent evt) {
if (taburl[tabp.getSelectedIndex()]!= null) {
        java.io.BufferedWriter out=null;
                try {
                     int ind = taburl[tabp.getSelectedIndex()].lastIndexOf("/");
                      out = new java.io.BufferedWriter(new java.io.FileWriter(getClass().getResource("/cide/resources/DPL").toString().substring(5)+taburl[tabp.getSelectedIndex()].substring(ind,taburl[tabp.getSelectedIndex()].length())));
                     lib();
                    }
                    catch (Exception ex) {
                    }
                   try{


                                          out.write(((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getText());
                         out.close();
                   }
                   catch(Exception ec) {
                        System.out.println(ec+"hi");
                   }
    }

}

private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
    System.exit(0);
    }

private void bookMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
    functionBookmarker.addItem(((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getSelectedText());
}

private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {
 newmenuActionPerformed(evt);
}

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
openMenuItemActionPerformed(evt);
}

private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {
saveMenuItemActionPerformed(evt);
}

private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {
  exitMenuItemActionPerformed(evt);
}

private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {
   cutMenuItemActionPerformed(evt);
}

private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {
 copyMenuItemActionPerformed(evt);
}

private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {
   pasteMenuItemActionPerformed(evt);
}

private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {
   findMenuItemActionPerformed(evt);
}

private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {
     replaceMenuItemActionPerformed(evt);

}

private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {
//      undoMenuItemActionPerformed(evt);
}

private void jButton11MouseMoved(java.awt.event.MouseEvent evt) {

}

private void jButton2MouseMoved(java.awt.event.MouseEvent evt) {
// TODO add your handling code here:
}

private void jButton3MouseMoved(java.awt.event.MouseEvent evt) {
// TODO add your handling code here:
}

private void jButton4MouseMoved(java.awt.event.MouseEvent evt) {
// TODO add your handling code here:
}

private void jButton5MouseMoved(java.awt.event.MouseEvent evt) {
// TODO add your handling code here:
}

private void jButton6MouseMoved(java.awt.event.MouseEvent evt) {
// TODO add your handling code here:
}

private void jButton7MouseMoved(java.awt.event.MouseEvent evt) {
// TODO add your handling code here:
}

private void jButton8MouseMoved(java.awt.event.MouseEvent evt) {
// TODO add your handling code here:
}

private void jButton9MouseMoved(java.awt.event.MouseEvent evt) {
// TODO add your handling code here:
}

private void jButton10MouseMoved(java.awt.event.MouseEvent evt) {
// TODO add your handling code here:
}


private void saveyesActionPerformed(java.awt.event.ActionEvent evt) {

}

private void savenoActionPerformed(java.awt.event.ActionEvent evt) {

}



private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
int option=open.showOpenDialog(tabp);
        if (option == open.APPROVE_OPTION) {
                     TextEdit[number_of_tabs] = new javax.swing.JTextPane();
        jScrollPane[number_of_tabs]=new javax.swing.JScrollPane();
                 jScrollPane[number_of_tabs].setViewportView(TextEdit[number_of_tabs]);
                 TextEdit[number_of_tabs].setFont(new java.awt.Font("Trebuchet MS", 0, 14));

               String str="";
               taburl[number_of_tabs]=open.getSelectedFile().getPath();
                     java.util.Scanner scan=null;
                    try{
                         scan = new java.util.Scanner(new java.io.FileInputStream(open.getSelectedFile().getPath()));
                    } catch (Exception ex) {
                         System.out.println(ex);
                    }
                      str=scan.nextLine();
                                str=str+"\n";
                       while (scan.hasNext())
                         {
                          str= str+scan.nextLine();
                              str=  str+"\n";
                         }
                            tabp.addTab(open.getSelectedFile().getName(), jScrollPane[number_of_tabs]);
                  tabp.setSelectedComponent(jScrollPane[number_of_tabs]);
                          syntaxcreator();
                                  UndoOp appl = new UndoOp();
                    TextEdit[number_of_tabs].setText(str);
                     TextEdit[number_of_tabs].addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TextEditKeyTyped(evt);
            }

        });

                number_of_tabs++;
               }

    }


private void gotolineeditActionPerformed(java.awt.event.ActionEvent evt) {

}

private void contentsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
    try {
    String command = "xchm "+getClass().getResource("/cide/resources/reference.chm").toString().substring(5);
    Process child = Runtime.getRuntime().exec(command);
    }
    catch(IOException e)
    {
    }
}

private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
if (taburl[tabp.getSelectedIndex()]== null) {
        saveAsMenuItemActionPerformed(evt);
    }
    else {
        java.io.BufferedWriter out=null;
                    try {

                      out = new java.io.BufferedWriter(new java.io.FileWriter(taburl[tabp.getSelectedIndex()]));
                    } catch (Exception ex) {

                         System.out.println(ex);
                    }
                   try{


                                          out.write(((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getText());
                         out.close();
                   }
                   catch(Exception ec) {
                        System.out.println(ec+"hi");
                   }
    }

               }



private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
int option=save.showSaveDialog(tabp);

        if (option == open.APPROVE_OPTION) {

                   java.io.BufferedWriter out=null;
                    System.out.println( save.getSelectedFile().getPath());
                 //   filepath = save.getSelectedFile().getPath();
                    taburl[tabp.getSelectedIndex()] = save.getSelectedFile().getPath();
                                   try {

                      out = new java.io.BufferedWriter(new java.io.FileWriter(save.getSelectedFile().getPath()));
                    tabp.setTitleAt(tabp.getSelectedIndex(),save.getSelectedFile().getName());
                    } catch (Exception ex) {

                         System.out.println(ex);
                    }
                   try{
                  out.write(((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getText());
                         out.close();

                   }
                   catch(Exception ec) {
                        System.out.println(ec+"hi");
                   }
               }
}

private void newmenuActionPerformed(java.awt.event.ActionEvent evt) {
 TextEdit[number_of_tabs] = new javax.swing.JTextPane();
        jScrollPane[number_of_tabs]=new javax.swing.JScrollPane();
                 jScrollPane[number_of_tabs].setViewportView(TextEdit[number_of_tabs]);
                 TextEdit[number_of_tabs].setFont(new java.awt.Font("Trebuchet MS", 0, 14));

 tabp.addTab("", jScrollPane[number_of_tabs]);
  tabp.setSelectedComponent(jScrollPane[number_of_tabs]);
      saveAsMenuItemActionPerformed(evt);


                          syntaxcreator();
                                  UndoOp appl = new UndoOp();
                     TextEdit[number_of_tabs].addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TextEditKeyTyped(evt);
            }

        });

                number_of_tabs++;
}

private void cutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).cut();
}

private void copyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).copy();
}

private void pasteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).paste();
}

private void selectAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).selectAll();
}



private void deleteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).replaceSelection("");
}
//class FindAndRaplace

public void findMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
Framefind.setVisible(true);
}

private void findNextMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
FindNextActionPerformed(evt);
}

private void replaceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
Framereplace.setVisible(true);
}

private void FindNextActionPerformed(java.awt.event.ActionEvent evt) {
String str=null,search=null;
texpos=0;
try
{
    search=FindText.getText();
}
catch(NullPointerException e)
{

}
if(search==null||search.compareTo("")==0)
    return;
try
{
    str=((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getSelectedText();
}
catch(NullPointerException e)
{

}
if(str!=null)
{
  if(str.compareTo(search)==0)
  {
      texpos=((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getSelectionEnd();
         str=((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getText();
  }
  else
  {   texpos=0;
  }
}
if(str==null)
{
   try
{
    str=((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getText();
}
catch(NullPointerException e)
{

}
}

int i=str.indexOf(search,texpos);
 System.out.println(texpos+" "+i);
 if(i==texpos&&i!=0)
 {
    i=str.indexOf(search,texpos+1);
    if(i==-1)
    {
    i=str.indexOf(search);
    }
   System.out.println(texpos+" "+i);
 }
if(i==-1)
{
    ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).select(0,0);
    return;
}
int ctr=0,pos=0;
while(pos<=i)
{
    ctr++;
    pos=str.indexOf("\r",pos);
    if(pos==-1)
        break;
    pos++;
    //System.out.println(pos);

}
i=i-ctr+1;
int r=i+search.length();
if(i!=-1)
{((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).setSelectionStart(i);
((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).setSelectionEnd(r);
}
Replacefindlabel.setText(search);
}

private void findcancelActionPerformed(java.awt.event.ActionEvent evt) {
Framefind.setVisible(false);
}

void Backup() {
     v++;
    File f = new File(taburl[tabp.getSelectedIndex()]);
     String x = taburl[tabp.getSelectedIndex()] + "." + v;

     listModel.insertElementAt(x,v);
     cblist.setSelectedIndex(v);
     f.renameTo(new File(x));
     java.io.BufferedWriter out=null;
     try {
           out = new java.io.BufferedWriter(new java.io.FileWriter(taburl[tabp.getSelectedIndex()]));
     } catch (Exception ex) {

           System.out.println(ex);
      }
      try{


           out.write(((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getText());
           out.close();
        }
       catch(Exception ec) {
       }

}


private void Compile(String file) throws IOException{
                console.selectAll();
                String ct = console.getSelectedText();
                if (ct == null)
                  console.setText("Compiling "+file+"...\n");
                else
                  console.setText(ct+"Compiling "+file+"...\n");
		String command = getClass().getResource("/cide/resources/compile").toString().substring(5);
                command += " "+file;
                System.out.println(command);
          Process child = Runtime.getRuntime().exec(command);
          try {
          	String s = new String();
                int ind = file.lastIndexOf("/");
                s = file.substring(0, ind+1);
                FileReader f = new FileReader(s+"err");
          	BufferedReader br = new BufferedReader(f);
          	while((s = br.readLine())!=null) {
                        console.selectAll();
                        console.setText(console.getSelectedText()+s+"\n");
          	}
          	f.close();
                console.selectAll();
                console.setText(console.getSelectedText()+"Compilation Failed...\n");
          }catch(FileNotFoundException e) {
                console.selectAll();
                console.setText(console.getSelectedText()+"Compilation Success...\n");
                flag = true;
                Backup();
                flag = false;
          }
     }

public void Run(String file) throws Exception {
     	try {
     		String runfile=new String();
                int endi = file.lastIndexOf(".");
                runfile = file.substring(0,endi);
                FileReader f = new FileReader(runfile);
          	f.close();
          	console.selectAll();
                String ct = console.getSelectedText();
                if (ct != null)
                   console.setText(ct+"Running "+file+"...\n");
                else
                    console.setText("Running "+file+"...\n");
                endi = file.lastIndexOf("/");
                runfile = file.substring(0,endi+1);
                runfile = runfile + "./";
                int si = endi+1;
                endi = file.lastIndexOf(".");
                runfile = runfile + file.substring(si, endi);
                String command = "gnome-terminal -e "+runfile;
     		Process child = Runtime.getRuntime().exec(command);
                console.selectAll();
                console.setText(console.getSelectedText()+"Build Success...\n");
     	}catch(FileNotFoundException e) {
                console.selectAll();
                String ct = console.getSelectedText();
                if (ct == null)
                    console.setText("Run Failed...\n");
                else
                    console.setText(ct+"Run Failed...\n");
     	}
     }

private void compileMenuItemActionPerformed (java.awt.event.ActionEvent evt) {
try {
    saveMenuItemActionPerformed(evt);
    if (taburl[tabp.getSelectedIndex()] != null) {
        Compile(taburl[tabp.getSelectedIndex()]);
    }
    else {
    console.selectAll();
    String s = console.getSelectedText();
    if (s != null)
     console.setText(s+"Compilation Failed...\n");
    else
     console.setText("Compilation Failed...\n");
    }
}catch(IOException e) {
    console.selectAll();
    String s = console.getSelectedText();
    if (s != null)
     console.setText(s+"Compilation Failed...\n");
    else
     console.setText("Compilation Failed...\n");
}
}

private void runMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
try {
    Run(taburl[tabp.getSelectedIndex()]);
}catch(Exception e) {
}
}

private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt)
{
    cide.About x = new cide.About();
    x.setVisible(true);
}

private void ReplacefindActionPerformed(java.awt.event.ActionEvent evt) {
    String search=null;
try
{
    search=Replacefindlabel.getText();
}
catch(NullPointerException e){}
if(search!=null)
{
FindText.setText(search);
FindNextActionPerformed(evt);
}
}

private void replacereplaceActionPerformed(java.awt.event.ActionEvent evt) {
String str=null,st=null;
ReplacefindActionPerformed(evt);
str=Replacelabel.getText();
try
{
    st=((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getSelectedText();
}
catch(NullPointerException e)
{

}
if(st!=null&&st!="")
{
 //   System.out.println(search);
((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).replaceSelection(str);
}

}

private void replacereplaceallActionPerformed(java.awt.event.ActionEvent evt) {
String str=null,st=null;

do{
ReplacefindActionPerformed(evt);
str=null;
str=Replacelabel.getText();
try
{
    st=((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getSelectedText();
}
catch(NullPointerException e)
{

}
if(st!=null && st!="")
{
((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).replaceSelection(str);
}
}while(st!=null && st!="");
}

private void replacecancelActionPerformed(java.awt.event.ActionEvent evt) {
  Framereplace.setVisible(false);
}

private void TextEditKeyTyped(java.awt.event.KeyEvent evt) {
/*char c=evt.getKeyChar();
switch(c)
{
    case '[':
    {

     ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView())
      .setText(((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getText().substring( 0, ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getCaretPosition())+"]"+
              ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getText().substring( ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getCaretPosition()));
      ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).setCaretPosition(((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getCaretPosition()-1);
      break;
    }
    case '{':
    {
     ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView())
      .setText(((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getText().substring( 0, ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getCaretPosition())+"}"+
              ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getText().substring( ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getCaretPosition()));
      ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).setCaretPosition(((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getCaretPosition()-1);
      break;
    }
    case '\'':
    {

      ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView())
      .setText(((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getText().substring( 0, ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getCaretPosition())+"\'"+
              ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getText().substring( ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getCaretPosition()));
      ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).setCaretPosition(((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getCaretPosition()-1);
      break;
    }
    case '\"':
    {
            ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView())
      .setText(((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getText().substring( 0, ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getCaretPosition())+"\""+
              ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getText().substring( ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getCaretPosition()));
      ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).setCaretPosition(((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getCaretPosition()-1);
      break;
    }
    case '(':
    {

            ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView())
      .setText(((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getText().substring( 0, ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getCaretPosition())+")"+
              ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getText().substring( ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getCaretPosition()));
      ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).setCaretPosition(((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).getCaretPosition()-1);
      break;
    }
}*/

}

private void syntaxcreator(){
  //JTextPane editor = new JTextPane();
    CodeDocument doc = new CodeDocument();
    Vector keywords = new Vector();
     keywords.addElement("asm");
    keywords.addElement("auto");
    keywords.addElement("bool");
    keywords.addElement("byte");
    keywords.addElement("break");
    keywords.addElement("case");
    keywords.addElement("char");
    keywords.addElement("catch");
    keywords.addElement("char");
    keywords.addElement("class");
    keywords.addElement("const");
      keywords.addElement("const_cast");
    keywords.addElement("continue");
    keywords.addElement("default");
    keywords.addElement("delete");
    keywords.addElement("do");
    keywords.addElement("double");
    keywords.addElement("dynamic_cast");
    // keywords.addElement("enum");
    keywords.addElement("else");
    keywords.addElement("export");
    keywords.addElement("enum");
    keywords.addElement("explicit");
    keywords.addElement("extern");
    keywords.addElement("false");
    keywords.addElement("float");
    keywords.addElement("for");
    keywords.addElement("friend");
    keywords.addElement("goto");
    keywords.addElement("if");
    keywords.addElement("inline");
    keywords.addElement("int");
    keywords.addElement("long");
    keywords.addElement("mutable");
    keywords.addElement("namespace");
    keywords.addElement("new");
    keywords.addElement("operator");
   // keywords.addElement("outer");
   // keywords.addElement("package");
    keywords.addElement("private");
    keywords.addElement("protected");
    keywords.addElement("public");
    keywords.addElement("register");
    keywords.addElement("reinterpret_cast");
    keywords.addElement("return");
    keywords.addElement("short");
    keywords.addElement("signed");
    keywords.addElement("sizeof");
    keywords.addElement("static");
     keywords.addElement("static_cast");
    keywords.addElement("struct");
    keywords.addElement("switch");
    keywords.addElement("synchronized");
    keywords.addElement("this");
    keywords.addElement("throw");
    keywords.addElement("template");
   keywords.addElement("typedef");
   keywords.addElement("typeid");
   keywords.addElement("typename");
    keywords.addElement("true");
    keywords.addElement("try");
    keywords.addElement("union");
    keywords.addElement("unsigned");
    keywords.addElement("using");
    keywords.addElement("virtual");
    keywords.addElement("var");
    keywords.addElement("void");
    keywords.addElement("volatile");
    keywords.addElement("wchar_t");
    keywords.addElement("while");
  //  System.out.println("hi");
    doc.setKeywords(keywords);
    ((JTextPane)((JScrollPane)tabp.getSelectedComponent()).getViewport().getView()).setDocument(doc);
}

  class CodeDocument extends DefaultStyledDocument{

  private String word = "";
  private SimpleAttributeSet keyword = new SimpleAttributeSet();
  private SimpleAttributeSet string = new SimpleAttributeSet();
  private SimpleAttributeSet normal = new SimpleAttributeSet();
  private SimpleAttributeSet number = new SimpleAttributeSet();
  private SimpleAttributeSet comments = new SimpleAttributeSet();
  private SimpleAttributeSet punc = new SimpleAttributeSet();
  private SimpleAttributeSet header = new SimpleAttributeSet();
  private int currentPos = 0;
  private Vector keywords = new Vector();
  public int cmt=0;
  public int HEADER_MODE=15;
  public int PUNC_MODE=14;
  public int STRING_MODE = 10;
  public int TEXT_MODE = 11;
  public int NUMBER_MODE = 12;
  public int COMMENT_MODE = 13;
  private int mode = TEXT_MODE;


  public CodeDocument() {
  // StyleConstants.setBold(keyword, true);
   StyleConstants.setForeground(keyword,new Color(0,150,0));
   StyleConstants.setForeground(string, Color.orange);
   StyleConstants.setForeground(number, Color.MAGENTA);
   //   StyleConstants.setBold(punc, true);
   StyleConstants.setForeground(punc, Color.RED);
   StyleConstants.setForeground(header, Color.blue);
   StyleConstants.setForeground(comments, Color.LIGHT_GRAY);
   StyleConstants.setItalic(comments, true);
  }



  private void insertKeyword(String str, int pos){
    try{
      this.remove(pos - str.length(), str.length());
      super.insertString(pos - str.length(), str, keyword);
    }
    catch (Exception ex){
      ex.printStackTrace();
    }
  }


  private void insertTextString(String str, int pos){
    try{
      this.remove(pos,str.length());
      super.insertString(pos, str, string);
    }
    catch (Exception ex){
      ex.printStackTrace();
    }
  }


  private void insertNumberString(String str, int pos){
    try{
      this.remove(pos,str.length());
      super.insertString(pos, str, number);
    }
    catch (Exception ex){
      ex.printStackTrace();
    }
  }
   private void insertpuncString(String str, int pos){
    try{
      this.remove(pos,str.length());
      super.insertString(pos, str, punc);
    }
    catch (Exception ex){
      ex.printStackTrace();
    }
  }


     private void insertCommentString(String str, int pos){
    try{
      //remove the old word and formatting
      this.remove(pos,str.length());
      super.insertString(pos, str, comments);
    }
    catch (Exception ex){
      ex.printStackTrace();
    }
  }

  private void insertHeaderString(String str, int pos){
    try{
      //remove the old word and formatting
      this.remove(pos,str.length());
      super.insertString(pos, str,header);
    }
    catch (Exception ex){
      ex.printStackTrace();
    }
  }


  private void checkForString(){
    int offs = this.currentPos;
    Element element = this.getParagraphElement(offs);
    String elementText = "";
    try{
      //this gets our chuck of current text for the element we're on
      elementText = this.getText(element.getStartOffset(),
                                 element.getEndOffset() -
element.getStartOffset());
    }
    catch(Exception ex){
      //whoops!
      System.out.println("no text");
    }
    int strLen = elementText.length();
    if (strLen == 0) {return;}
    int i = 0;


    if (element.getStartOffset() > 0){
      //translates backward if neccessary
      offs = offs - element.getStartOffset();
    }
    int quoteCount = 0;
    if ((offs >= 0) && (offs <= strLen-1)){
      i = offs;
      while (i >0){
      //the while loop walks back until we hit a delimiter


        char charAt = elementText.charAt(i);
        if ((charAt == '"')){
         quoteCount ++;
        }
        i--;
      }
      int rem = quoteCount % 2;
      //System.out.println(rem);
      mode = (rem == 0) ? TEXT_MODE: STRING_MODE;
    }
  }


  private void checkForKeyword(){
    if (mode != TEXT_MODE) {
      return;
    }
    int offs = this.currentPos;
    Element element = this.getParagraphElement(offs);
    String elementText = "";
    try{
      //this gets our chuck of current text for the element we're on
      elementText = this.getText(element.getStartOffset(),
element.getEndOffset() - element.getStartOffset());
    }
    catch(Exception ex){
      //whoops!
      System.out.println("no text");
    }
    int strLen = elementText.length();
    if (strLen == 0) {return;}
    int i = 0;


    if (element.getStartOffset() > 0){
      //translates backward if neccessary
      offs = offs - element.getStartOffset();
    }
    if ((offs >= 0) && (offs <= strLen-1)){
      i = offs;
      while (i >0){
      //the while loop walks back until we hit a delimiter
        i--;
        char charAt = elementText.charAt(i);
        if ((charAt == ' ') | (i == 0) | (charAt =='(') | (charAt ==')') |
            (charAt == '{') | (charAt == '}')){ //if i == 0 then we're at the begininng
          if(i != 0){
            i++;
          }
          word = elementText.substring(i, offs);//skip the period


          String s = word.trim().toLowerCase();
          //this is what actually checks for a matching keyword
          if (keywords.contains(s)){
            insertKeyword(word, currentPos);
          }
          break;
        }
      }
    }
  }


   private void checkForPunc(){
    int offs = this.currentPos;
    Element element = this.getParagraphElement(offs);
    String elementText = "";
    try{
      //this gets our chuck of current text for the element we're on
      elementText = this.getText(element.getStartOffset(),
element.getEndOffset() - element.getStartOffset());
    }
    catch(Exception ex){
      //whoops!
      System.out.println("no text");
    }
    int strLen = elementText.length();
    if (strLen == 0) {return;}
    int i = 0;


    if (element.getStartOffset() > 0){
      //translates backward if neccessary
      offs = offs - element.getStartOffset();
    }
    mode = TEXT_MODE;
    if ((offs >= 0) && (offs <= strLen-1)){
      i = offs;
       char charAt = elementText.charAt(i);
       if(charAt=='{'||charAt=='}'||charAt=='['||charAt==']'||charAt=='\''||charAt==';'||charAt=='+'||charAt=='-'||charAt=='/'||charAt=='*'||charAt=='&'||charAt=='|'||charAt=='{')
       {
           String s="";
           s=s+charAt;
           this.insertpuncString(s, currentPos);
    }
    }
  }

  private void checkForNumber(){
    int offs = this.currentPos;
    Element element = this.getParagraphElement(offs);
    String elementText = "";
    try{
      //this gets our chuck of current text for the element we're on
      elementText = this.getText(element.getStartOffset(),
element.getEndOffset() - element.getStartOffset());
    }
    catch(Exception ex){
      //whoops!
      System.out.println("no text");
    }
    int strLen = elementText.length();
    if (strLen == 0) {return;}
    int i = 0;


    if (element.getStartOffset() > 0){
      //translates backward if neccessary
      offs = offs - element.getStartOffset();
    }
   // mode = TEXT_MODE;
    if ((offs >= 0) && (offs <= strLen-1)){
      i = offs;
        char charAt = elementText.charAt(i);
        if (mode != COMMENT_MODE||mode!=HEADER_MODE||mode!=STRING_MODE){
          mode = NUMBER_MODE;
      }
    }
  }
   private void checkForHeader(){
    int offs = this.currentPos;
    Element element = this.getParagraphElement(offs);
    String elementText = "";
    try{
      //this gets our chuck of current text for the element we're on
      elementText = this.getText(element.getStartOffset(),
element.getEndOffset() - element.getStartOffset());
    }
    catch(Exception ex){
      //whoops!
      System.out.println("no text");
    }
    int strLen = elementText.length();
    if (strLen == 0) {return;}
    int i = 0;


    if (element.getStartOffset() > 0){
      //translates backward if neccessary
      offs = offs - element.getStartOffset();
    }
    if ((offs >= 1) && (offs <= strLen-1)){
      i = offs;
      String s="";
     char headerChar1=elementText.charAt(i);
     if(headerChar1=='<')
      for(int j=8;i-j>=0&&j>=0;j--)
      {
      s=s+elementText.charAt(i-j);
      }
    // System.out.println(s);
      if (s.compareTo("#include<")==0){
          mode = HEADER_MODE;
          this.insertHeaderString("#include<", currentPos-8);
      }
      else if (headerChar1 == '>'){
          mode = TEXT_MODE;
          cmt=1;
          this.insertHeaderString(">", currentPos);
          int j;
          for(j=1;i-j>=0&&elementText.charAt(i-j)!='<';j++)
          { s=elementText.charAt(i-j)+s;
          }
           this.insertHeaderString(s,currentPos-j+1);
      }
    }
  }

  private void checkForComment(){
    int offs = this.currentPos;
    Element element = this.getParagraphElement(offs);
    String elementText = "";
    try{
      //this gets our chuck of current text for the element we're on
      elementText = this.getText(element.getStartOffset(),
element.getEndOffset() - element.getStartOffset());
    }
    catch(Exception ex){
      //whoops!
      System.out.println("no text");
    }
    int strLen = elementText.length();
    if (strLen == 0) {return;}
    int i = 0;


    if (element.getStartOffset() > 0){
      //translates backward if neccessary
      offs = offs - element.getStartOffset();
    }
    if ((offs >= 1) && (offs <= strLen-1)){
      i = offs;
      char commentStartChar1 = elementText.charAt(i-1);
      char commentStartChar2 = elementText.charAt(i);
      if (commentStartChar1 == '/' && commentStartChar2 == '*'){
          mode = COMMENT_MODE;
          this.insertCommentString("/*", currentPos-1);
      }
      else if (commentStartChar1 == '*' && commentStartChar2 == '/'){
          mode = TEXT_MODE;
          cmt=1;
          this.insertCommentString("*//*", currentPos-1);
      }
    }
  }


  private void processChar(String str){
    char strChar = str.charAt(0);
    if (mode != this.COMMENT_MODE){
      mode = TEXT_MODE;
    }
      switch (strChar){
        case ('{'):case ('}'):case (' '): case('\n'):
        case ('('):case (')'):case (';'):case ('.'):{
          checkForKeyword();
          if (mode == STRING_MODE && strChar == '\n'){
            mode = TEXT_MODE;
          }
        }
        break;
        case ('"'):{
          insertTextString(str, currentPos);
          this.checkForString();
        }
        break;
        case ('0'):case ('1'):case ('2'):case ('3'):case ('4'):
        case ('5'):case ('6'):case ('7'):case ('8'):case ('9'):{
          checkForNumber();
        }
        break;
        case ('*'):case ('/'):{
          checkForComment();
        }
        break;
          case ('#'):case ('i'):   case ('n'):case ('c'):   case ('l'):case ('u'):   case ('d'):case ('e'):   case ('<'):case ('>'):{
          checkForHeader();
        }
        break;
      }

      if (mode == this.TEXT_MODE){
        this.checkForString();
      }
      if (mode == this.STRING_MODE){
        insertTextString(str, this.currentPos);
      }
      else if (mode == this.NUMBER_MODE){
        insertNumberString(str, this.currentPos);
      }
      else if (mode == this.COMMENT_MODE){
        insertCommentString(str, this.currentPos);
      }
      else if (mode == this.HEADER_MODE){
        insertHeaderString(str, this.currentPos);
      }
      else if(strChar=='{'||strChar=='}'||strChar=='['||strChar==']'||strChar=='\''||strChar==';'||strChar=='+'||strChar=='-'||strChar=='/'||strChar=='*'||strChar=='&'||strChar=='|'&&mode!=this.STRING_MODE)
    {
 if(cmt!=1)
   this.checkForPunc();
 else
     cmt=0;
    }



  }


  private void processChar(char strChar){
      char[] chrstr = new char[1];
      chrstr[0] = strChar;
      String str = new String(chrstr);
      processChar(str);
  }


  public void insertString(int offs,
                          String str,
                          AttributeSet a) throws BadLocationException{
    super.insertString(offs, str, normal);


    int strLen = str.length();
    int endpos = offs + strLen;
    int strpos;
    for (int i=offs;i<endpos;i++){
      currentPos = i;
      strpos = i - offs;
      processChar(str.charAt(strpos));
    }
    currentPos = offs;
  }


  public Vector getKeywords(){
    return this.keywords;
  }


  public void setKeywords(Vector aKeywordList){
    if (aKeywordList != null){
      this.keywords = aKeywordList;
    }
  }
  }




    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CIDEApp().setVisible(true);

            }

        });
    }
    private GUIfinal g;
       private javax.swing.JButton chnick;

    private javax.swing.JScrollPane msgsc;

    private javax.swing.JList msglab;
    private javax.swing.JLabel nicklab;

    boolean b;
    private int v;
    boolean flag;
    private String liburl;
    private String items [] = {"select"};
   private String m[] =new String[100];
   private String taburl[]=new String[50];
    private int texpos=0;
    private int linenumber=1;
    int number_of_tabs =1;
     private javax.swing.JTextPane StatisticsDisplayer;
      private javax.swing.JComboBox functionBookmarker;
       private javax.swing.JPanel msg;
        private javax.swing.JLabel cbdisp;
    private javax.swing.JList cblist;
    private DefaultListModel listModel;
     private javax.swing.JLabel lndisp;
    private javax.swing.JLabel cndisp;
        private javax.swing.JButton libadd;
    private javax.swing.JLabel libdisp;
    private javax.swing.JScrollPane libScrollPane;
    private javax.swing.JList liblist;
    private javax.swing.JScrollPane jScrollPane[]=new JScrollPane[50];
    private javax.swing.JTextPane TextEdit[]=new JTextPane[50];
    private javax.swing.JTabbedPane tabp;
    private javax.swing.JTextPane console;
     private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JMenuItem gotolineedit;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JToolBar.Separator jSeparator1;
        private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar jToolBar1;
        private javax.swing.JFileChooser open;
        private javax.swing.JFileChooser save;
//    private javax.swing.JTextPane TextEdit;
   //  private javax.swing.JTextPane linedisplayer;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem contentsMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenuItem compileMenuItem;
    private javax.swing.JMenuItem runMenuItem;
    private javax.swing.JMenuItem bookMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem findMenuItem;
    private javax.swing.JMenuItem findNextMenuItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenu buildMenu;
    private javax.swing.JFrame Framefind;
    private javax.swing.JFrame Framereplace;
    private javax.swing.JFrame Framesave;
    private javax.swing.JFrame jFrame3;
    private javax.swing.JMenuItem newmenu;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JMenuItem replaceMenuItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JMenuItem selectAllMenuItem;
    private javax.swing.JMenuItem undoMenuItem;

    private javax.swing.JButton FindNext;
    private javax.swing.JTextField FindText;
    private javax.swing.JButton findcancel;
    private javax.swing.JLabel findlabel;


    private javax.swing.JButton Replacefind;
    private javax.swing.JTextField Replacefindlabel;
    private javax.swing.JTextField Replacelabel;
    private javax.swing.JButton replacecancel;
    private javax.swing.JLabel replacelab1;
    private javax.swing.JLabel replacelab2;
    private javax.swing.JButton replacereplace;
    private javax.swing.JButton replacereplaceall;
    private javax.swing.JScrollPane jScrollPane4;

    private javax.swing.JLabel savelabel;
    private javax.swing.JButton saveno;
    private javax.swing.JButton saveyes;
}