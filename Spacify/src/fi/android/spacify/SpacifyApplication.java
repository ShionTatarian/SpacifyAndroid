package fi.android.spacify;

import android.app.Application;
import fi.android.service.web.WebService;
import fi.android.spacify.service.WorkService;

public class SpacifyApplication extends Application {

	@Override
	public void onCreate() {
		WorkService.init(5);
		WebService.init(this);
		
		super.onCreate();
	}
	
}
