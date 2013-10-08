package edu.ut.mobile.network;


import java.io.Serializable;
import java.util.logging.Logger;

public abstract class CloudRemotable implements Serializable  {
    private static final long serialVersionUID = 3;
    final static Logger logger = Logger.getLogger(CloudRemotable.class.getName());
    transient protected CloudController cloudController = new CloudController();

    protected CloudRemotable() {
    }

    public CloudRemotable(CloudController cc) {
        cloudController = cc;
    }

    public CloudController getCloudController() {
        return cloudController;
    }

    public void setCloudController(CloudController cloudController) {
        this.cloudController = cloudController;
    }

}
