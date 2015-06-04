/**
 * Created by qb-user on 29.05.15.
 */
import models.DataFetcher;
import play.*;

public class Global extends GlobalSettings {
    public void onStop(Application app) {
        try {
            DataFetcher.getInstance().invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop(app);
    }
}
