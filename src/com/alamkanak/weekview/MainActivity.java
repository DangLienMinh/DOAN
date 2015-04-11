package com.alamkanak.weekview;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by Raquib-ul-Alam Kanak on 7/21/2014. Website:
 * http://april-shower.com
 */
public class MainActivity extends ActionBarActivity implements
		WeekView.MonthChangeListener, WeekView.EventClickListener,
		WeekView.EventLongPressListener {

	private static final int TYPE_DAY_VIEW = 1;
	private static final int TYPE_THREE_DAY_VIEW = 2;
	private static final int TYPE_WEEK_VIEW = 3;
	private int mWeekViewType = TYPE_THREE_DAY_VIEW;
	private WeekView mWeekView;
	List<String> cuocHen = new ArrayList<String>();
	List<String> day = new ArrayList<String>();
	List<String> start = new ArrayList<String>();
	List<String> end = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);	
		invokeWS();

		// Get a reference for the week view in the layout.
		mWeekView = (WeekView) findViewById(R.id.weekView);

		// Show a toast message about the touched event.
		mWeekView.setOnEventClickListener(this);

		// The week view has infinite scrolling horizontally. We have to provide
		// the events of a
		// month every time the month changes on the week view.
		mWeekView.setMonthChangeListener(this);

		// Set long press listener for events.
		mWeekView.setEventLongPressListener(this);
		//mWeekView.goToToday();
		mWeekView.refreshView();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.action_today:
			mWeekView.goToToday();
			return true;
		case R.id.action_day_view:
			if (mWeekViewType != TYPE_DAY_VIEW) {
				item.setChecked(!item.isChecked());
				mWeekViewType = TYPE_DAY_VIEW;
				mWeekView.setNumberOfVisibleDays(1);

				// Lets change some dimensions to best fit the view.
				mWeekView.setColumnGap((int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, 8, getResources()
								.getDisplayMetrics()));
				mWeekView.setTextSize((int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_SP, 12, getResources()
								.getDisplayMetrics()));
				mWeekView.setEventTextSize((int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_SP, 12, getResources()
								.getDisplayMetrics()));
			}
			return true;
		case R.id.action_three_day_view:
			if (mWeekViewType != TYPE_THREE_DAY_VIEW) {
				item.setChecked(!item.isChecked());
				mWeekViewType = TYPE_THREE_DAY_VIEW;
				mWeekView.setNumberOfVisibleDays(3);

				// Lets change some dimensions to best fit the view.
				mWeekView.setColumnGap((int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, 8, getResources()
								.getDisplayMetrics()));
				mWeekView.setTextSize((int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_SP, 12, getResources()
								.getDisplayMetrics()));
				mWeekView.setEventTextSize((int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_SP, 12, getResources()
								.getDisplayMetrics()));
			}
			return true;
		case R.id.action_week_view:
			if (mWeekViewType != TYPE_WEEK_VIEW) {
				item.setChecked(!item.isChecked());
				mWeekViewType = TYPE_WEEK_VIEW;
				mWeekView.setNumberOfVisibleDays(7);

				// Lets change some dimensions to best fit the view.
				mWeekView.setColumnGap((int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, 2, getResources()
								.getDisplayMetrics()));
				mWeekView.setTextSize((int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_SP, 10, getResources()
								.getDisplayMetrics()));
				mWeekView.setEventTextSize((int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_SP, 10, getResources()
								.getDisplayMetrics()));
			}
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {

		// Populate the week view with some events.
		List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();
		int dayOfWeek = 0, startHours = 0, endHours = 0;

		for (int i = 1; i < day.size(); i++) {

			for (int count = 0; count < 4; count++) {

				Calendar startTime = Calendar.getInstance();
				dayOfWeek = Integer.parseInt(day.get(i));
				startHours = Integer.parseInt(start.get(i));
				endHours = Integer.parseInt(end.get(i));

				if (count != 0) {
					startTime.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
					int daysInMonth = startTime.getActualMaximum(Calendar.DAY_OF_MONTH);
					if((count*7)>daysInMonth) break;
					startTime.add(Calendar.DATE, count * 7);
				}
				
				startTime.set(Calendar.DAY_OF_WEEK, dayOfWeek);
				startTime.set(Calendar.HOUR_OF_DAY, startHours);
				startTime.set(Calendar.MINUTE, 0);
				startTime.set(Calendar.MONTH, newMonth - 1);
				startTime.set(Calendar.YEAR, newYear);
				Calendar endTime = (Calendar) startTime.clone();
				endTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(end.get(i)));
				endTime.set(Calendar.MINUTE, 0);
				endTime.set(Calendar.MONTH, newMonth - 1);
				WeekViewEvent event = new WeekViewEvent(1,
						getEventTitle(startTime), startTime, endTime);
				// event = new WeekViewEvent(10, getEventTitle(startTime),
				// startTime, endTime);
				event.setColor(getResources().getColor(R.color.event_color_01));
				events.add(event);
				
			}

		}
		for(int j=0;j<cuocHen.size();j++){
			String x=cuocHen.get(j);
			String timePart=x.substring(x.lastIndexOf(' ')+1);
			Date date = Date.valueOf(x.substring(0, x.indexOf(" ")));
			int hour=Integer.parseInt(timePart.substring(0,2));
			
			Calendar calStart = new GregorianCalendar();
			calStart.setTime(date);
			calStart.set(Calendar.HOUR_OF_DAY, hour);
			calStart.set(Calendar.MINUTE, 0);
	        Calendar calEnd=(Calendar) calStart.clone();
	        calEnd.set(Calendar.HOUR_OF_DAY,hour+1);
	        WeekViewEvent event = new WeekViewEvent(11,
					getEventTitle(calStart), calStart, calEnd);
			event.setColor(getResources().getColor(R.color.event_color_02));

			for(int k=0;k<events.size();k++){
				int hour1=events.get(k).getStartTime().get(Calendar.HOUR_OF_DAY)+7;
				int day1=events.get(k).getStartTime().get(Calendar.DATE);
				int month1=events.get(k).getStartTime().get(Calendar.MONTH);
				int year1=events.get(k).getStartTime().get(Calendar.YEAR);
				if(hour1==hour&&day1==calStart.get(Calendar.DATE)&&month1==calStart.get(Calendar.MONTH)&&year1==calStart.get(Calendar.YEAR)){
					//events.remove(k);
					//events.add(k,event);
					events.get(k).setColor(getResources().getColor(R.color.event_color_02));
					//events.set(k,event);
					
				}
			}
		}

		return events;
	}

	private String getEventTitle(Calendar time) {
		return String.format("Cuộc hẹn từ %02d:%02d đến %02d:%02d",
				time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE),
				time.get(Calendar.HOUR_OF_DAY)+1, time.get(Calendar.MINUTE));
	}

	@Override
	public void onEventClick(WeekViewEvent event, RectF eventRect) {
		if(event.getColor()==getResources().getColor(R.color.event_color_02)){
			Toast.makeText(MainActivity.this,
					"Full", Toast.LENGTH_SHORT)
					.show();
		}else{
			int startHour=event.getStartTime().get(Calendar.HOUR_OF_DAY)+7;
			//int endHour=event.getEndTime().get(Calendar.HOUR_OF_DAY)+7;
			int theMonth=event.getStartTime().get(Calendar.MONTH) + 1; 
			int theDay=event.getStartTime().get(Calendar.DAY_OF_MONTH);
			int theYear=event.getStartTime().get(Calendar.YEAR);
			RequestParams params = new RequestParams();
			params.put("MaCK", "CC");
		    params.put("NgayGio", theYear+"-"+theMonth+"-"+theDay+" "+startHour+":00:00");
		    params.put("Email", "danglienminh93@gmail.com");
		    params.put("TrieuChung", "si da roi");
		    AsyncHttpClient client = new AsyncHttpClient();
			
			client.post("http://minhhunglaw.com/webservice/khambenh/them",params,
					new AsyncHttpResponseHandler() {
						// When the response returned by REST has Http response code
						// '200'
						@Override
						public void onSuccess(String response) {
							// Hide Progress Dialog
							Toast.makeText(getApplicationContext(),
									 response,
									 Toast.LENGTH_LONG).show();
							}

						// When the response returned by REST has Http response code
						// other than '200'
						@Override
						public void onFailure(int statusCode, Throwable error,
								String content) {
							// Hide Progress Dialog

							// When Http response code is '404'
							if (statusCode == 404) {
								Toast.makeText(getApplicationContext(),
										"Requested resource not found",
										Toast.LENGTH_LONG).show();
							}
							// When Http response code is '500'
							else if (statusCode == 500) {
								Toast.makeText(getApplicationContext(),
										"Something went wrong at server end",
										Toast.LENGTH_LONG).show();
							}
							// When Http response code other than 404, 500
							else {
								Toast.makeText(
										getApplicationContext(),
										"Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]",
										Toast.LENGTH_LONG).show();
							}
						}
					});
		}
		
		/*Toast.makeText(MainActivity.this, "Clicked " +startHour +":"+endHour+" "+theDay+"/"+theMonth,
				Toast.LENGTH_SHORT).show();*/
	}

	@Override
	public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
		Toast.makeText(MainActivity.this,
				"Long pressed event: " + event.getName(), Toast.LENGTH_SHORT)
				.show();
	}

	public void invokeWS() {
		// Make RESTful webservice call using AsyncHttpClient object
		AsyncHttpClient client = new AsyncHttpClient();
		
		client.get("http://minhhunglaw.com/webservice/bacsi/llv/BS001",
				new AsyncHttpResponseHandler() {
					// When the response returned by REST has Http response code
					// '200'
					@Override
					public void onSuccess(String response) {
						// Hide Progress Dialog

						try {
							// JSON Object
							JSONArray array = new JSONArray(response);
					        JSONArray llv = array.getJSONArray(0);
					        JSONArray ngayDat = array.getJSONArray(1);
					        
							//JSONObject jObject = new JSONObject(response);
							//JSONArray jArray = jObject.getJSONArray("llv");
							/*JSONObject object1=jArray.getJSONObject(0);
							JSONArray llv=object1.getJSONArray("llv");
							JSONArray ngayDat=jArray.getJSONArray(1);*/
							
							for (int i = 0; i < llv.length(); i++) {
								JSONObject mJsonObj = llv.getJSONObject(i);
								day.add(mJsonObj.getString("Thu"));
								String time = mJsonObj.getString("Gio");
								start.add(time.substring(0,
										Math.min(time.length(), 2)));
								end.add(time.substring(Math.max(
										time.length() - 2, 0)));
							}
							for (int j = 0; j < ngayDat.length(); j++) {
								JSONObject mJsonObj = ngayDat.getJSONObject(j);
								cuocHen.add(mJsonObj.getString("NgayGio"));
								/*String x=mJsonObj.getString("NgayGio");
								String timePart=x.substring(x.lastIndexOf(' ')+1);
								Date date = Date.valueOf(x.substring(0, x.indexOf(" ")));
								int hour=Integer.parseInt(timePart.substring(0,2));
								
								Calendar cal = new GregorianCalendar();
						        cal.setTime(date);
						        cal.set(Calendar.HOUR_OF_DAY, hour);
						        Toast.makeText(getApplicationContext(),
						        		Integer.toString(cal.get(Calendar.HOUR_OF_DAY)),
										 Toast.LENGTH_LONG).show();*/
							}
							
							
							/* Toast.makeText(getApplicationContext(),
							 Integer.toString(day.size()),
							 Toast.LENGTH_LONG).show();
							 */

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							Toast.makeText(
									getApplicationContext(),
									"Error Occured [Server's JSON response might be invalid]!",
									Toast.LENGTH_LONG).show();
							e.printStackTrace();

						}
					}

					// When the response returned by REST has Http response code
					// other than '200'
					@Override
					public void onFailure(int statusCode, Throwable error,
							String content) {
						// Hide Progress Dialog

						// When Http response code is '404'
						if (statusCode == 404) {
							Toast.makeText(getApplicationContext(),
									"Requested resource not found",
									Toast.LENGTH_LONG).show();
						}
						// When Http response code is '500'
						else if (statusCode == 500) {
							Toast.makeText(getApplicationContext(),
									"Something went wrong at server end",
									Toast.LENGTH_LONG).show();
						}
						// When Http response code other than 404, 500
						else {
							Toast.makeText(
									getApplicationContext(),
									"Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]",
									Toast.LENGTH_LONG).show();
						}
					}
				});
	}
}
