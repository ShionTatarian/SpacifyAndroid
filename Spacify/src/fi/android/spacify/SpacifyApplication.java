package fi.android.spacify;

import java.io.File;

import android.app.Application;
import fi.android.spacify.db.BubbleDatabase;
import fi.android.spacify.service.AccountService;
import fi.android.spacify.service.ContentManagementService;
import fi.qvik.android.util.ImageService;
import fi.qvik.android.util.WorkService;
import fi.qvik.service.http.QvikHttpService;
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
		QvikHttpService.init(getApplicationContext());
		BubbleDatabase.init(getApplicationContext());
		ContentManagementService.init(getApplicationContext());
		BaseSettings.init(getApplicationContext());
		AccountService.init(getApplicationContext());

		File images = new File(getFilesDir(), "images/");
		images.mkdirs();
		ImageService.init(images, QvikHttpService.getInstance());
		ImageService.getInstance().setDebug(true);
		ImageService.getInstance().useImageCache(true, 15);
		
		super.onCreate();
	}
	
}
