package direct.thither.onamove;


import direct.thither.onamove.comm.Comm;

public class App extends android.app.Application {
    private static App appInstance;
    public static App getInstance(){
        return appInstance;
    }

    public Globals globals;
    public Comm comm;

    @Override
    public void onCreate() {
        super.onCreate();
        appInstance = this;
        appInstance.globals = new Globals();
        appInstance.comm = new Comm();

    }

}
