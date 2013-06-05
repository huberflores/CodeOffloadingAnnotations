package MC.NetClasses;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.lang.reflect.Method;


public class NetworkManagerServer {
    int portnum;
    Socket mysocket = null;
    InputStream in = null;
    OutputStream out = null;
    ObjectInputStream ois = null;
    ObjectOutputStream oos = null;
    ServerSocket serversoc = null;
    byte[] serveraddress = new byte[4];
    

    public NetworkManagerServer(/*byte []serveraddress, */int port) {
        //this.serveraddress = serveraddress;
        portnum = port;
    }


    public boolean makeconnection() {

        if (serversoc == null || serversoc.isClosed()) {
            try {
                serversoc = new ServerSocket(portnum);
                //serversoc.bind(new InetSocketAddress(Inet4Address.getByAddress(serveraddress), portnum));
                serversoc.setSoTimeout(0);
            } catch (IOException ex) {
            }
        }


        try {
            System.out.println("server waiting");
            mysocket = serversoc.accept();
            
            in = mysocket.getInputStream();
            out = mysocket.getOutputStream();

            oos = new ObjectOutputStream(out);
            ois = new ObjectInputStream(in);

            System.out.println("connection established");

            waitforreceivingdata();
            return true;
        } catch (SocketException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        } catch (Exception ex) {
            return false;
        }

    }


    private void waitforreceivingdata() {
        try {
            new Receiving().waitforreceivingdata();
        } catch (Exception ex) {
        }
    }


    class Receiving implements Runnable {
        String functionName = null;
        Class[] paramTypes = null;
        Object[] paramValues = null;
        Object state = null;
        Class stateDType = null;
        Pack myPack = null;

        public Receiving() {
        }

        public void waitforreceivingdata() {
            Thread t = new Thread(this);
            System.out.println("Thread Starting ");
            t.start();
        }

        @Override
        public void run() {
            try {
                myPack = (Pack) ois.readObject();
                functionName = myPack.getfunctionName();
                paramTypes = myPack.getparamTypes();
                paramValues = myPack.getparamValues();
                state = myPack.getstate();
                stateDType = myPack.getstateType();
                if (functionName != null && functionName.length() > 0) {
                    try {

                        Class cls = Class.forName(stateDType.getName());
                        
                        Method method = cls.getDeclaredMethod(functionName, paramTypes);
                        
                        try{
                            Object result = method.invoke(state, paramValues);
                            ResultPack rp = new ResultPack(result, state);
                            oos.writeObject(rp);
                            oos.flush();
                        } catch (IllegalAccessException ex) {
                            returnnull(oos);
                        } catch (InvocationTargetException ex) {
                            returnnull(oos);
                        } catch(Exception ex){
                            ResultPack rp = new ResultPack(null, state);
                            oos.writeObject(rp);
                            oos.flush();
                        }
                        
                    } catch (ClassNotFoundException ex) {
                        returnnull(oos);
                    }  catch (IllegalArgumentException ex) {
                        returnnull(oos);
                    } catch (NoSuchMethodException ex) {
                        returnnull(oos);
                    } catch (SecurityException ex) {
                        returnnull(oos);
                    } finally {

                        oos.close();
                        ois.close();

                        in.close();
                        out.close();

                        mysocket.close();

                        oos = null;
                        ois = null;

                        in = null;
                        out = null;
                        mysocket = null;

                    }
                } else {
                    returnnull(oos);
                }
            } catch (IOException ex) {
                returnnull(oos);
            } catch (ClassNotFoundException ex) {
                returnnull(oos);
            } finally {
                makeconnection();
            }
        }
    }

    void returnnull(ObjectOutputStream oos){
        if(oos != null)
            try {
                oos.writeObject(null);
                oos.flush();
            } catch (IOException ex1) {

            }
    }


}

