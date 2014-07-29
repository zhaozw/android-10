package cz.vutbr.fit.iha.activity.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.TrackDialogFragment;

/**
 * About dialog
 * 
 * @author Martin Doudera
 * 
 */
public class InfoDialogFragment extends TrackDialogFragment {

	// ///////////////////////////////////////////////
	// COMPONENTS
	TextView version;

	// ///////////////////////////////////////////////

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		LayoutInflater factory = LayoutInflater.from(getSherlockActivity());
		final View view = factory.inflate(R.layout.fragment_info, null);

		version = (TextView) view.findViewById(R.id.version);
		try {
			version.setText(getString(R.string.version)
					+ " "
					+ getActivity().getPackageManager().getPackageInfo(
							getActivity().getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			version.setText("0.0");
		}

		return new AlertDialog.Builder(getActivity())
				.setView(view)
				.setNegativeButton(R.string.rate, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Uri uri = Uri.parse(getActivity().getResources()
								.getString(R.string.play_link));
						Intent myAppLinkToMarket = new Intent(
								Intent.ACTION_VIEW, uri);
						startActivity(myAppLinkToMarket);
					}
				})
				.setPositiveButton(getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

							}
						})
				.setNeutralButton(getString(R.string.website),
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent companyLink = new Intent(
										Intent.ACTION_VIEW,
										Uri.parse(getString(R.string.company_web)));
								startActivity(companyLink);
							}
						}).create();
	}
}