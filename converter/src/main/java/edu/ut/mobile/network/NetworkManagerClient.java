package edu.ut.mobile.network;



import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Logger;


public class NetworkManagerClient {
    final static Logger logger = Logger.getLogger(NetworkManagerClient.class.getName());
    int portnum;
    Socket mysocket = null;
    InputStream in = null;
    OutputStream out = null;
    ObjectInputStream ois = null;
    ObjectOutputStream oos = null;
    byte []serverAddress =new byte[4];
    CloudController callingparent = null;
    long startTime = 0;

    public NetworkManagerClient(byte[] serverAddress, int port) {

        this.serverAddress = serverAddress;
        portnum = port;
    }

    public void setNmf(CloudController callingparent) {
        this.callingparent = callingparent;
    }


    public boolean connect(){
        mysocket = new Socket();
        try {
            mysocket.connect(new InetSocketAddress(Inet4Address.getByAddress(serverAddress), portnum), NetInfo.waitTime);

            startTime = System.nanoTime();
            in = mysocket.getInputStream();
            out = mysocket.getOutputStream();

            oos = new ObjectOutputStream(out);

            ois = new ObjectInputStream(in);
            return true;
        } catch (IOException ex) {
            logger.warning(ex.getMessage());
            callingparent.setResult(null, null);
            return false;
        }
    }



    public void send(String functionName, Class[] paramTypes, Object[] funcArgValues, Object state, Class stateDType){
        try{
            new Sending(new Pack(functionName, stateDType, state, funcArgValues, paramTypes)).send();
        }catch(Exception ex){
            logger.warning(ex.getMessage());
            callingparent.setResult(null, null);
        }
    }



    class Sending implements  Runnable{
        Pack MyPack = null;
        ResultPack result = null;

        public Sending(Pack MyPack) {
            this.MyPack = MyPack;
        }


        public void send(){
            Thread t = new Thread(this);
            t.start();
        }

        @Override
        public void run() {
            try {

                oos.writeObject( MyPack );
                oos.flush();

                result = (ResultPack) ois.readObject();

                if((System.nanoTime() - startTime)/1000000 < NetInfo.waitTime){
                    if(result == null)
                        callingparent.setResult(null, null);
                    else
                        callingparent.setResult(result.getresult(), result.getstate());
                }

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

            } catch (IOException ex) {
                logger.warning(ex.getMessage());
                callingparent.setResult(null, null);
            } catch (ClassNotFoundException ex) {
                logger.warning(ex.getMessage());
                callingparent.setResult(null, null);
            }
        }

    }


}
