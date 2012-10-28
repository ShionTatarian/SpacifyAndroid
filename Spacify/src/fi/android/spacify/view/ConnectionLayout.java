package fi.android.spacify.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import fi.android.spacify.R;

public class ConnectionLayout extends FrameLayout {

	private int[][] connections;
	private Paint paint;

	private void initPaint() {
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.GREEN);
		paint.setStrokeWidth(getResources().getDimension(R.dimen.margin_half));

	}

	public ConnectionLayout(Context context) {
		super(context);
		initPaint();
	}

	public ConnectionLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPaint();
	}

	public ConnectionLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initPaint();
	}

	public void setConnections(int[][] cons) {
		if(cons == null) {
			connections = cons;
			return;
		}
		if(connections != null && cons.length > 0) {
			synchronized(connections) {
				connections = cons;
			}
			postInvalidate();
		} else if(cons.length > 0) {
			connections = cons;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(connections != null) {
			int[][] con;
			synchronized(connections) {
				con = connections.clone();
			}
			for(int i = 0; i < con.length; i++) {
				canvas.drawLine(con[i][0], con[i][1], con[i][2], con[i][3], paint);
			}
		}
	}

}
