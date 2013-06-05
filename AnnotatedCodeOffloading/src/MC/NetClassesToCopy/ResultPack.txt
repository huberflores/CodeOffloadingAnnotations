package MC.NetClasses;

import java.io.Serializable;


public class ResultPack implements Serializable{
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
