package controllers;

import controllers.util.Utils;
import models.DataFetcher;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Arrays;

public class Application extends Controller {
    private String appId;
    private String timeLength;
    private long timeLengthValue = 1;

    public static final long HOUR = 60 * 60 * 1000;
    public static final long DAY = 24 * HOUR;
    public static final long MONTH = 31 * DAY;

    public Result index() throws Exception {
        if (appId == null || timeLength == null || timeLength.length() == 0 || timeLengthValue < 0 || appId.length() == 0) {
            Utils.eraseFileContent();
        } else {
            try {
                //DataFetcher.getInstance().insertData(100);
                long startingFrom = 0;
                switch (timeLength) {
                    case "hour" : startingFrom = Application.HOUR * timeLengthValue; break;
                    case "day" : startingFrom = Application.DAY * timeLengthValue; break;
                    case "month" : startingFrom = Application.MONTH * timeLengthValue; break;
                }

                DataFetcher.getInstance().fetchData(this.appId, startingFrom);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ok(views.html.index.render(DataFetcher.getInstance().getAppsFromDB(), Arrays.asList("hour", "day", "month"), appId, timeLength, timeLengthValue, Arrays.asList(DataFetcher.KEYS_TO_PARSE)));
    }

    /**
     * Changes {@code this.appId, this.timeLength, this.timeLengthValue} and redirects to {@code #index} method.
     * @return
     * @throws Exception
     */
    public Result updateSettings() throws Exception {
        DynamicForm dForm = Form.form().bindFromRequest();
        String appId = dForm.get("appId");
        String timeLengthValue = dForm.get("timeLengthValue");
        String timeLength = dForm.get("timeLength");

        try {
            this.timeLengthValue = Long.valueOf(timeLengthValue);
        } catch (NumberFormatException e) {
            //doesn't matter
            //this.timeLengthValue = 1;
        }
        this.appId = appId;
        this.timeLength = timeLength;

        return redirect("/");
    }

    /**
     *  Returns the needed resource.
     * @param file file name
     * @return the resource
     */
    public Result getResource(String file) {
        return ok(play.Play.application().getFile(Utils.DEFAULT_RESOURCE_FOLDER + file));
    }
}
