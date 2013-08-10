package edu.ut.netclasses;


public class Main {


    public static void main(String[] args) {
        NetworkManagerServer nm = new NetworkManagerServer(NetInfo.port);
        nm.makeconnection();
    }

}