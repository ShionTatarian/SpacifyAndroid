package fi.android.spacify;

import android.app.Application;
import fi.android.service.WorkService;
import fi.android.service.web.WebService;
import fi.android.spacify.db.BubbleDatabase;
import fi.android.spacify.service.ContentManagementService;
import fi.spacify.android.util.BaseSettings;

/**
 * Spacify Application.
 * 
 * @author Tommy
 * 
 */
public class SpacifyApplication extends Application {

	@Override
	public void onCreate() {
		WorkService.init(5);
		WebService.init(getApplicationContext());
		BubbleDatabase.init(getApplicationContext());
		ContentManagementService.init(getApplicationContext());
		BaseSettings.init(getApplicationContext());
		
		super.onCreate();
	}
	
}
