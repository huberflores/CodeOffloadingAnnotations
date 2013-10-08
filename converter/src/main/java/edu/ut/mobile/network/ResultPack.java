package edu.ut.mobile.network;
import java.io.Serializable;
import java.util.logging.Logger;


public class ResultPack implements Serializable{
    final static Logger logger = Logger.getLogger(ResultPack.class.getName());
    private static final long serialVersionUID = 2;
    Object result = null;
    Object state = null;

    public ResultPack(Object result, Object state) {
        this.result = result;
        this.state = state;
    }

    public Object getresult(){
        return result;
    }

    public Object getstate(){
        return state;
    }

}
