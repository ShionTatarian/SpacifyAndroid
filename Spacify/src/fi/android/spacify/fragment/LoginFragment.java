package fi.android.spacify.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import fi.android.spacify.R;
import fi.android.spacify.service.AccountService;

public class LoginFragment extends BaseFragment {
	
	private final AccountService account = AccountService.getInstance();
	
	private View layout;
	private Button loginButton;
	private EditText nickEdit;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		layout = inflater.inflate(R.layout.login_layout, container, false);
		loginButton = (Button) layout.findViewById(R.id.login_button);
		loginButton.setOnClickListener(onLoginClick);
		nickEdit = (EditText) layout.findViewById(R.id.login_edit_text);

		return layout;
	}
	
	private OnClickListener onLoginClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Activity act = getActivity();
			if(act != null) {
				InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(nickEdit.getWindowToken(),InputMethodManager.RESULT_UNCHANGED_SHOWN);
			}
			account.login(nickEdit.getText().toString());
		}
	};
	
}
