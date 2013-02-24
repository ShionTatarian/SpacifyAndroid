package fi.android.spacify.fragment;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import fi.android.spacify.R;
import fi.android.spacify.activity.BubbleActivity;
import fi.android.spacify.service.AccountService;
import fi.android.spacify.view.BubbleView;
import fi.android.spacify.view.BubbleView.BubbleJSON;

public class ControlAdapter extends ArrayAdapter<Integer> {

	public class COMMANDS {
		public static final int SHOW_CONTENT = 1000;
		public static final int IMAGE = 1001;
		public static final int LINK_COUNT = 1002;
		public static final int EDIT = 1003;
		public static final int FAVORITE = 1004;
		public static final int WEB = 1005;
		public static final int VIDEO = 1006;
		public static final int CALL_TO_SCREEN = 1007;
		public static final int LOGOUT = 1008;
	}

	private BubbleView bv;
	private BubbleActivity act;
	private final AccountService account = AccountService.getInstance();

	public ControlAdapter(Context context, BubbleView bv, BubbleActivity act) {
		super(context, 0);
		this.bv = bv;
		this.act = act;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.control_bubble, parent, false);
			convertView.setTag(new ViewHolder(convertView));
		}
		ViewHolder holder = (ViewHolder) convertView.getTag();
		int item = getItem(position);

		switch (item) {
			default:
			case COMMANDS.SHOW_CONTENT:
				holder.text.setVisibility(View.GONE);
				holder.image.setVisibility(View.VISIBLE);
				holder.image.setOnClickListener(onContentClick);
				holder.image.setImageResource(android.R.drawable.ic_menu_help);
				break;
			case COMMANDS.IMAGE:
				holder.text.setVisibility(View.GONE);
				holder.image.setVisibility(View.VISIBLE);
				holder.image.setOnClickListener(onImageClick);
				holder.image.setImageResource(android.R.drawable.ic_menu_gallery);
				break;
			case COMMANDS.LINK_COUNT:
				holder.text.setVisibility(View.VISIBLE);
				holder.text.setText("" + bv.getLinks().size());
				holder.image.setVisibility(View.VISIBLE);
				holder.image.setBackgroundResource(R.drawable.greenball);
				break;
			case COMMANDS.WEB:
				holder.text.setVisibility(View.VISIBLE);
				holder.text.setText(R.string.web_command_bubble);
				holder.image.setVisibility(View.VISIBLE);
				holder.image.setBackgroundResource(R.drawable.lightblueball);
				holder.image.setOnClickListener(onWebClick);
				break;
			case COMMANDS.FAVORITE:
				holder.text.setVisibility(View.GONE);
				holder.image.setVisibility(View.VISIBLE);
				holder.image.setOnClickListener(onFavoriteClick);
				if(account.isFavorite(bv)) {
					holder.image.setImageResource(android.R.drawable.btn_star_big_on);
				} else {
					holder.image.setImageResource(android.R.drawable.btn_star_big_off);
				}
				break;
			case COMMANDS.CALL_TO_SCREEN:
				holder.image.setVisibility(View.VISIBLE);
				holder.image.setImageResource(android.R.drawable.ic_menu_mylocation);
				holder.image.setOnClickListener(onCallToScreenClick);
				break;
			case COMMANDS.LOGOUT:
				holder.image.setVisibility(View.VISIBLE);
			holder.image.setImageResource(android.R.drawable.ic_lock_power_off);
				holder.image.setOnClickListener(onLogoutClick);
				break;
		}

		return convertView;
	}

	private OnClickListener onLogoutClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			AlertDialog.Builder alert = new AlertDialog.Builder(act);
			alert.setTitle(act.getString(R.string.logout));
			alert.setMessage(act.getString(R.string.logout_are_you_sure));
			alert.setPositiveButton(R.string.logout_ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					account.logout();
					act.onMeClick(null);
				}
			});
			alert.setNegativeButton(R.string.logout_cancel, null);
			alert.show();

		}
	};

	private OnClickListener onWebClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String url = null;
			try {
				JSONObject third = bv.getStyleOverrides().getJSONObject(BubbleJSON.thirdLayer);
				if(third != null) {
					url = third.getString(BubbleJSON.webSiteUrl);
				}
			} catch(JSONException e) {
			}

			if(!TextUtils.isEmpty(url)) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				act.startActivity(intent);
			}
		}
	};

	private OnClickListener onCallToScreenClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			act.openCallToScreenPopup(v);
		}
	};

	private OnClickListener onFavoriteClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			account.toggleFavorite(bv);
			if(v instanceof ImageView) {
				ImageView image = (ImageView) v;
				if(account.isFavorite(bv)) {
					image.setImageResource(android.R.drawable.btn_star_big_on);
				} else {
					image.setImageResource(android.R.drawable.btn_star_big_off);
				}
			}
		}
	};

	private OnClickListener onContentClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// act.setContent(bv);
		}
	};

	private OnClickListener onImageClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			act.onImageClick(bv);
		}
	};

	private OnClickListener onEditClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
		}
	};

	private class ViewHolder {

		TextView text;
		ImageView image;

		public ViewHolder(View v) {
			text = (TextView) v.findViewById(R.id.control_bubble_text);
			image = (ImageView) v.findViewById(R.id.control_bubble_image);
		}
	}

}
