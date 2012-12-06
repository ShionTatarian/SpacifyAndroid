package fi.android.spacify.view;

import android.content.Context;
import fi.android.spacify.R;

public class ThirdLayer extends BaseBubbleView {

	public ThirdLayer(Context context) {
		super(context);

		bubble = findViewById(R.id.third_layer_image);
		zoom(2);
		diameter = 300;
	}

	@Override
	protected int getLayout() {
		return R.layout.third_layer_image;
	}

	@Override
	public void move(int x, int y) {
		int radius = getRadius();
		this.x = (x + offsetX - radius);
		this.y = (y + offsetY - radius);

		LayoutParams params = (LayoutParams) getLayoutParams();
		params.leftMargin = this.x;
		params.topMargin = this.y;
		setLayoutParams(params);
	}

}
