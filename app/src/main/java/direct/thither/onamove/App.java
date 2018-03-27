package direct.thither.onamove;
import direct.thither.onamove.comm.Comm;
import direct.thither.onamove.properties.PropsHolder;


public class App extends android.app.Application {
    private static App appInstance;
    public static App getInstance(){
        return appInstance;
    }

    public PropsHolder props;
    public Comm comm;

    @Override
    public void onCreate() {
        super.onCreate();
        appInstance = this;
        appInstance.props = new PropsHolder();
        appInstance.comm = new Comm();
    }

}
