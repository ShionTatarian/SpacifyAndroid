package fi.android.spacify.activity;

import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;
import fi.android.spacify.R;

public class VideoActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		VideoView videoHolder = new VideoView(this);
		// if you want the controls to appear
		videoHolder.setMediaController(new MediaController(this));
		Uri video = Uri
				.parse("android.resource://" + getPackageName() + "/" + R.raw.axe_commercial);

		videoHolder.setVideoURI(video);
		setContentView(videoHolder);
		videoHolder.start();
	}

}
