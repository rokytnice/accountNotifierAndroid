package org.rochlitz.hbci.callback;

import org.rochlitz.gironotifier.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity 
		  {

	private HBCIBusiness kausz;
	 
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainlayout);
		addListenerOnButton();
		
		long id = Thread.currentThread().getId();
    	String name = Thread.currentThread().getName();
    	int hashcode = Thread.currentThread().hashCode();
    	Log.d("giro", id+" 1--- "+name+" --- "+hashcode);
    	
    	kausz = (HBCIBusiness) new HBCIBusiness(getApplicationContext().getFilesDir()) {  

			@Override
			protected void onPreExecute() {
			}
			
			@Override
			protected Void doInBackground(Void... params) {
//				getKontoauszuege();
				getUmsatz();
				return null;
			}
			
			@Override
			protected void onProgressUpdate(Void... values) {
				openDialog(kausz.getdBean());
				showUserDialog();
			}
		}.execute();
	}
	

	String mainDto="";

	Button button;
	public void addListenerOnButton() {
		button = (Button) findViewById(R.id.buttonTweet);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
					String b = "";
			}
		});
		
	}
	
	 

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// String result = getDialogInput(getApplicationContext());
//		TestKontoAuszug kausz = new TestKontoAuszug();
//		 kausz.kontoAuszug();
		return super.onTouchEvent(event);
	}


	AlertDialog actions;

	// Button action handled here (pop up the dialog)
	View.OnClickListener buttonListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			actions.show();
		}
	};
	
	public void showUserDialog(){
		actions.show();
	}
	
	public void openDialog(DialogBean dialogBean) {

		try {
			Button button = new Button(this);
			button.setText("Click for Options");
			button.setOnClickListener(buttonListener);

			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			final EditText input = new EditText(this);
			
			if(dialogBean.isInputDialog()){
				builder.setView(input);
			}
			builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				

				public void onClick(DialogInterface dialog, int whichButton) {
					String inputText = input.getText().toString().trim();
					Toast.makeText(getApplicationContext(), inputText,
							Toast.LENGTH_SHORT).show();
					kausz.getdBean().setInputFinished(true);
					kausz.getdBean().setUserInput(inputText);
				}
			});
			builder.setMessage(dialogBean.getDialogOutPutText());
			builder.setNegativeButton("Cancel", null);
			actions = builder.create();
			setContentView(button);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String brkpnt = "";
	}
	
	


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	  

 
}
