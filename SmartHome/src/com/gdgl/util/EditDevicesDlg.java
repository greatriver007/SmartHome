package com.gdgl.util;

import com.gdgl.model.SimpleDevicesModel;
import com.gdgl.smarthome.R;
import com.gdgl.util.MyOkCancleDlg.Dialogcallback;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class EditDevicesDlg {
	private Context mContext;
	private SimpleDevicesModel mSimpleDevicesModel;

	EditDialogcallback dialogcallback;
	Dialog dialog;
	Button save;
	Button cancle;
	TextView textView;

	EditText mName, mRegion;

	public EditDevicesDlg(Context c, SimpleDevicesModel s) {
		mContext = c;
		mSimpleDevicesModel = s;

		dialog = new Dialog(mContext, R.style.MyDialog);
		dialog.setContentView(R.layout.edit_devices_dlg);
		textView = (TextView) dialog.findViewById(R.id.txt_title);

		mName = (EditText) dialog.findViewById(R.id.edit_name);
		mRegion = (EditText) dialog.findViewById(R.id.edit_region);

		final String name = mSimpleDevicesModel.getmNodeENNAme();
		final String region = mSimpleDevicesModel.getmDeviceRegion();

		mName.setText(name);
		mRegion.setText(region);

		save = (Button) dialog.findViewById(R.id.btn_save);
		save.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String mN = mName.getText().toString();
				String mR = mRegion.getText().toString();
				if ((!name.equals(mN)) || (!mRegion.equals(mR))) {
					dialogcallback.saveedit(mN, mR);
				}
				dismiss();

			}
		});

		cancle = (Button) dialog.findViewById(R.id.btn_cancle);
		cancle.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

			}
		});
	}

	public interface EditDialogcallback {
		public void saveedit(String name, String region);
	}

	public void setDialogCallback(EditDialogcallback dialogcallback) {
		this.dialogcallback = dialogcallback;
	}

	public void setContent(String content) {
		textView.setText(content);
	}

	public void show() {
		dialog.show();
	}

	public void hide() {
		dialog.hide();
	}

	public void dismiss() {
		dialog.dismiss();
	}

}
