package com.hydrasoftworks.diablo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hydrasoftworks.diablo.model.BattleTag;
import com.hydrasoftworks.diablo.model.CareerProfile;
import com.hydrasoftworks.diablo.model.exceptions.CareerProfileParsingException;

public class DiabloHeroesBrowserActivity extends Activity {
	@SuppressWarnings("unused")
	private static final String TAG = DiabloHeroesBrowserActivity.class
			.getSimpleName();
	
	private BattleTagsDataSource dataSource;
	private ListView listView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dataSource = new BattleTagsDataSource(this);
		dataSource.open();

		setContentView(R.layout.main);

		List<BattleTag> tags = dataSource.getAllComments();
		BattleTag[] elements = tags.toArray(new BattleTag[tags.size()]);
		ArrayAdapter<BattleTag> adapter = new ArrayAdapter<BattleTag>(this,
				android.R.layout.simple_dropdown_item_1line, elements);
		final AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.battletag_textview);
		textView.setAdapter(adapter);

		listView = (ListView) findViewById(R.id.battletags_listview);
		listView.setAdapter(new BattleTagAdapter(this,
				R.layout.battletag_one_result_details_row, elements));

		((Button) findViewById(R.id.battletag_find_button))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						String tagText = textView.getText().toString();
						Context ctx = v.getContext();

						if (tagText.matches("[^ $&#!%\t]{3,12}#[0-9]{4}")) {
							BattleTag tag = dataSource
									.createOrGetBattleTag(tagText);
							if (!testConnection()) {
								Toast.makeText(
										ctx,
										ctx.getResources()
												.getString(
														R.string.internt_connection_error),
										Toast.LENGTH_SHORT).show();
							} else {
								new CareerProfileDataDownload().execute(tag);
							}
						} else {
							Toast.makeText(
									ctx,
									ctx.getResources().getString(
											R.string.battletag_wrong),
									Toast.LENGTH_SHORT).show();
						}

					}
				});
	}

	@Override
	protected void onResume() {
		dataSource.open();
		super.onResume();
	}

	@Override
	protected void onPause() {
		dataSource.close();
		super.onPause();
	}

	public boolean testConnection() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connMgr.getActiveNetworkInfo();

		return (info != null && info.isConnected());
	}

	class CareerProfileDataDownload extends
			AsyncTask<BattleTag, Integer, String> {

		@Override
		protected String doInBackground(BattleTag... params) {
			StringBuilder sb = new StringBuilder();
			try {
				URL url = CareerProfile.createUrl(params[0]);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						url.openStream()));
				String inputLine;

				while ((inputLine = in.readLine()) != null)
					sb.append(inputLine);

				in.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
				Log.e(TAG, e.getLocalizedMessage());
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, e.getLocalizedMessage());
			}
			// TODO: Uncomment this //return sb.toString();

			return getTextFromInputStream(getResources().openRawResource(
					R.raw.career_profile));
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				CareerProfile.parseCareerProfile(result);
				Toast.makeText(getBaseContext(), "dzia�a", Toast.LENGTH_SHORT)
						.show();
			} catch (CareerProfileParsingException e) {
				// TODO: show message about error
			}

			super.onPostExecute(result);
		}

	}

	class BattleTagAdapter extends ArrayAdapter<BattleTag> {

		public BattleTagAdapter(Context context, int textViewResourceId,
				BattleTag[] objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			View row = getLayoutInflater().inflate(
					R.layout.battletag_one_result_details_row, parent, false);

			TextView textView = (TextView) row
					.findViewById(R.id.battletag_text);
			textView.setText(getItem(position).getBattleTag());
			Button deleteButton = (Button) row
					.findViewById(R.id.delete_entry_button);

			deleteButton.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					dataSource.deleteBattleTag(getItem(position));
					List<BattleTag> tags = dataSource.getAllComments();
					BattleTag[] elements = tags.toArray(new BattleTag[tags.size()]);
					listView.setAdapter(new BattleTagAdapter(v.getContext(),
							R.layout.battletag_one_result_details_row, elements));
				}
			});

			return row;
		}
	}

	// TODO: remove this method when ready
	private static String getTextFromInputStream(InputStream is) {
		Writer writer = new StringWriter();
		char[] buffer = new char[1024];
		try {
			Reader reader = new BufferedReader(new InputStreamReader(is,
					"UTF-8"));
			int n;
			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}

		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
		}
		return writer.toString();
	}
}