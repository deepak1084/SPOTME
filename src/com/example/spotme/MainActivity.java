package com.example.spotme;



import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class MainActivity extends Activity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks  {


	private static final String CARD_INTRO = "INTRO";
	private static final String CARD_PICKER = "PICKER";
	private static final String CARD_DETAIL = "DETAIL";
	private String placeData =null;
	public String getPlaceData() {
		return placeData;
	}


	public void setPlaceData(String placeData) {
		this.placeData = placeData;
	}

	private String url =null;
	static String TAG = "LOCATIFY";

	private TextView locationData = null;

	private static final int ACTION_PICK_PLACE = 1;

	private Bitmap imageData = null;

	public Bitmap getImageData() {
		return imageData;
	}


	public void setImageData(Bitmap imageData) {
		this.imageData = imageData;
	}

	private Button spotMe = null ;
	private Button shareLocation = null;
	private Button spotFriend = null;
	private Button GoogleMap = null;
	private ImageView image = null;
	private AutoCompleteTextView mAutocompleteView;

	protected GoogleApiClient mGoogleApiClient;

	private PlaceAutocompleteAdapter mAdapter;

	private static final LatLngBounds BOUNDS_INDIA = new LatLngBounds(
			new LatLng(10.204492,77.707691), new LatLng(13.204492,79.707691));

	/**
	 * Request code passed to the PlacePicker intent to identify its result when it returns.
	 */
	private static final int REQUEST_PLACE_PICKER = 1;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.home);

		Intent intent = getIntent();

		if(intent.getDataString()!=null) {
			handleExtraData(intent.getDataString());
		}

		spotMe = (Button) findViewById(R.id.spotMe);
		spotFriend = (Button)findViewById(R.id.spotFriend);
		locationData= (TextView) findViewById(R.id.data);
		shareLocation = (Button) findViewById(R.id.Share);
		GoogleMap = (Button) findViewById(R.id.GoogleMaps);
		image = (ImageView) findViewById(R.id.imageData);

		if (mGoogleApiClient == null) {
			rebuildGoogleApiClient();
			mGoogleApiClient.connect();
		}


		// Retrieve the AutoCompleteTextView that will display Place suggestions.
		mAutocompleteView = (AutoCompleteTextView)
				findViewById(R.id.autocomplete_places);

		// Register a listener that receives callbacks when a suggestion has been selected
		mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);

		// Retrieve the TextView that will display details of the selected place.
		//	        mPlaceDetailsText = (TextView) findViewById(R.id.place_details);

		// Set up the adapter that will retrieve suggestions from the Places Geo Data API that cover
		// the entire world.
		mAdapter = new PlaceAutocompleteAdapter(this, android.R.layout.simple_list_item_1,
				BOUNDS_INDIA, null);
		mAutocompleteView.setAdapter(mAdapter);

		// Set up the 'clear text' button that clears the text in the autocomplete view

		shareLocation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_TEXT, placeData+"\n"+url );
				sendIntent.setType("text/plain");
				startActivity(sendIntent);
			}
		});

		GoogleMap.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_VIEW);
				sendIntent.setData(Uri.parse(url));
				startActivity(sendIntent);
			}
		});

		spotMe.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
				Intent intent;
				try {
					intent = intentBuilder.build(getApplicationContext());
					startActivityForResult(intent, 1);

				} catch (GooglePlayServicesRepairableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (GooglePlayServicesNotAvailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});         // Start the Intent by requesting a result, identified by a request code.

	}

	private AdapterView.OnItemClickListener mAutocompleteClickListener
	= new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			/*
      Retrieve the place ID of the selected item from the Adapter.
      The adapter stores each Place suggestion in a PlaceAutocomplete object from which we
      read the place ID.
			 */
			final PlaceAutocompleteAdapter.PlaceAutocomplete item = mAdapter.getItem(position);
			final String placeId = String.valueOf(item.placeId);

			String callurl = "https://maps.googleapis.com/maps/api/place/details/json?placeid="+placeId+"&key=AIzaSyD1Jfyx-9MZ5SUXdOeDe9e8sg9HeLKuD44";
			new APICALL(callurl).execute();
			Log.i(TAG, "Autocomplete item selected: " + item.description);
			Log.i(TAG, "Called getPlaceById to get Place details for " + item.placeId);
		}
	};

	/**
	 * Callback for results from a Places Geo Data API query that shows the first place result in
	 * the details view on screen.
	 */
	private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
	= new ResultCallback<PlaceBuffer>() {
		@Override
		public void onResult(PlaceBuffer places) {
			if (!places.getStatus().isSuccess()) {
				// Request did not complete successfully
				Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());

				return;
			}
			// Get the Place object from the buffer.
			final Place place = places.get(0);

			Log.i(TAG, "Place details received: " + place.getName());
		}
	};

	private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
			CharSequence address, CharSequence phoneNumber, Uri websiteUri) {

		return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
				websiteUri));

	}


	/**
	 * Construct a GoogleApiClient for the {@link Places#GEO_DATA_API} using AutoManage
	 * functionality.
	 * This automatically sets up the API client to handle Activity lifecycle events.
	 */
	protected synchronized void rebuildGoogleApiClient() {

		mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext(), this, this).addApi(Places.GEO_DATA_API).build();
		if(mGoogleApiClient== null ) {
			Log.i("Deepak","It is still null");
		}

	}

	/**
	 * Called when the Activity could not connect to Google Play services and the auto manager
	 * could resolve the error automatically.
	 * In this case the API is not available and notify the user.
	 *
	 * @param connectionResult can be inspected to determine the cause of the failure
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

		Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
				+ connectionResult.getErrorCode());

		// TODO(Developer): Check error code and notify the user of error state and resolution.
		Toast.makeText(this,
				"Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
				Toast.LENGTH_SHORT).show();

		// Disable API access in the adapter because the client was not initialised correctly.
		mAdapter.setGoogleApiClient(null);

	}


	@Override
	public void onConnected(Bundle bundle) {
		// Successfully connected to the API client. Pass it to the adapter to enable API access.
		Log.i(TAG, "GoogleApiClient connected.");
		mAdapter.setGoogleApiClient(mGoogleApiClient);


	}

	@Override
	public void onConnectionSuspended(int i) {
		// Connection to the API client has been suspended. Disable API access in the client.
		mAdapter.setGoogleApiClient(null);
		Log.e(TAG, "GoogleApiClient connection suspended.");
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("Deepak","Onresult");
		// BEGIN_INCLUDE(activity_result)
		if (requestCode == REQUEST_PLACE_PICKER) {
			// This result is from the PlacePicker dialog.

			// Enable the picker option


			if (resultCode == Activity.RESULT_OK) {
				/* User has picked a place, extract data.
                   Data is extracted from the returned intent by retrieving a Place object from
                   the PlacePicker.
				 */
				final Place place = PlacePicker.getPlace(data, getApplicationContext());

				/* A Place object contains details about that place, such as its name, address
                and phone number. Extract the name, address, phone number, place ID and place types.
				 */
				final CharSequence name = place.getName();
				final CharSequence address = place.getAddress();
				final CharSequence phone = place.getPhoneNumber();
				final String placeId = place.getId();
				String attribution = PlacePicker.getAttributions(data);
				if(attribution == null){
					attribution = "";
				}

				new imaageDownload(placeId).execute();

				url = "http://maps.google.com/maps?q=loc:" + place.getLatLng().latitude + "," +place.getLatLng().longitude ;
				placeData  = name + " "+address+ " "+phone+" \n ";
				locationData.setText(placeData);
				locationData.setVisibility(View.VISIBLE);
				
				spotMe.setVisibility(View.INVISIBLE);
				spotFriend.setVisibility(View.INVISIBLE);
				mAutocompleteView.setVisibility(View.INVISIBLE);
				shareLocation.setVisibility(View.VISIBLE);
				GoogleMap.setVisibility(View.VISIBLE);
				image.setVisibility(View.VISIBLE);
				// Update data on card.

			}
		}
	}

	public void handleExtraData (String data) {
		Log.i(TAG,data);
		String longi,lati;
		int i;
		longi = ""+data.charAt(34);
		for( i=35;data.charAt(i)!= ',';i++) { 
			longi+=data.charAt(i);
		}
		i++;
		lati = ""+data.charAt(i);
		i++;
		for(;i<data.length();i++) {
			lati+=data.charAt(i);
		}
		Log.i(TAG,"longi"+longi+" "+lati);
		openIntentByLatlng(lati, longi);
	}

	public void openIntentByLatlng (String longi,String lati) {
		Double lo,la;
		la= Double.parseDouble(lati);
		lo=Double.parseDouble(longi);

		LatLng obj1= new LatLng(la, lo);
		LatLng obj2= new LatLng(la+0.005, lo+0.005);
		LatLngBounds BOUNDS_Home = new LatLngBounds(
				obj1,obj2);

		PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder().setLatLngBounds(BOUNDS_Home);
		Intent intent;
		try {
			intent = intentBuilder.build(getApplicationContext());
			startActivityForResult(intent, 1);

		} catch (GooglePlayServicesRepairableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GooglePlayServicesNotAvailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public class APICALL extends AsyncTask<Void, Void, Void>{
		String url= null;
		String response=null;
		public APICALL(String url) {
			this.url = url;
		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			// string buffers the url
			StringBuffer buffer_string = new StringBuffer(url);
			String replyString = "";

			// instanciate an HttpClient
			HttpClient httpclient = new DefaultHttpClient();
			// instanciate an HttpGet
			HttpGet httpget = new HttpGet(buffer_string.toString());

			try {
				// get the responce of the httpclient execution of the url
				HttpResponse response = httpclient.execute(httpget);
				InputStream is = response.getEntity().getContent();

				// buffer input stream the result
				BufferedInputStream bis = new BufferedInputStream(is);
				ByteArrayBuffer baf = new ByteArrayBuffer(20);
				int current = 0;
				while ((current = bis.read()) != -1) {
					baf.append((byte) current);
				}
				// the result as a string is ready for parsing
				replyString = new String(baf.toByteArray());
			} catch (Exception e) {
				e.printStackTrace();
			}

			response = replyString.trim();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			try {
				JSONObject jsonObject = new JSONObject(response);
				Log.i("Deepak",jsonObject.toString());

				JSONObject ob = jsonObject.getJSONObject("result").getJSONObject("geometry");

				String longi =ob.getJSONObject("location").optString("lng");
				String lati =ob.getJSONObject("location").optString("lat");
				openIntentByLatlng(longi,lati);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public class imaageDownload extends AsyncTask<Void, Void, Void> {
		String placeId;
		String response;
		public imaageDownload(String placeId) {
			this.placeId = placeId;
		}
		@Override
		protected Void doInBackground(Void... params) {
			String url  = "https://maps.googleapis.com/maps/api/place/details/json?placeid="+placeId+"&key=AIzaSyD1Jfyx-9MZ5SUXdOeDe9e8sg9HeLKuD44";
			StringBuffer buffer_string = new StringBuffer(url);
			String replyString = "";
			
			// instanciate an HttpClient
			HttpClient httpclient = new DefaultHttpClient();
			// instanciate an HttpGet
			HttpGet httpget = new HttpGet(buffer_string.toString());

			try {
				// get the responce of the httpclient execution of the url
				HttpResponse response = httpclient.execute(httpget);
				InputStream is = response.getEntity().getContent();

				// buffer input stream the result
				BufferedInputStream bis = new BufferedInputStream(is);
				ByteArrayBuffer baf = new ByteArrayBuffer(20);
				int current = 0;
				while ((current = bis.read()) != -1) {
					baf.append((byte) current);
				}
				// the result as a string is ready for parsing
				replyString = new String(baf.toByteArray());
			} catch (Exception e) {
				e.printStackTrace();
			}

			response = replyString.trim();

			String picreference=null;
			try {
				JSONObject json = new JSONObject(response);
				String jsonA = json.getString("result");
				
				JSONObject temm= new JSONObject(jsonA);
				if(temm.has("photos")) {
				JSONArray picarray = temm.getJSONArray("photos");
				
				
				for(int i=0;i<picarray.length() && i<=1;i++) {
				picreference = (picarray.getJSONObject(i).getString("photo_reference"));	
				}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(picreference == null ) {
				imageData=null;
				return null;
			}
			Log.i(TAG,picreference);
			URL sURL;
			try {
				String picimageUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference="+picreference+"&key=AIzaSyD1Jfyx-9MZ5SUXdOeDe9e8sg9HeLKuD44";
				sURL = new URL(picimageUrl);
				HttpURLConnection conn = (HttpURLConnection) sURL.openConnection();
				imageData = BitmapFactory.decodeStream(conn.getInputStream());
				if(imageData==null) {
					Log.i(TAG,"IMAGE data is null");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			/*Image Styling here */
			image.setImageBitmap(imageData);
		}

	}

}