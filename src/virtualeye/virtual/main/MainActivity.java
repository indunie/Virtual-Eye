package virtualeye.virtual.main;

import virtualeye.virtual.main.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
	//private String getURL=" ";
	private EditText et;
	public String getURL="";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        final ToggleButton on = (ToggleButton) findViewById(R.id.toggleButton);
        on.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                if (on.isChecked()) {
                    on.setBackgroundDrawable(getResources().getDrawable(R.drawable.mute));
                } else {
                    on.setBackgroundDrawable(getResources().getDrawable(R.drawable.spk));
                }
            }
        });
  
        ListView LW=(ListView)findViewById(R.id.mylist);
        
        String[] values = new String[] { "Telescope", "Vidusayura", "Credits", "Help", "Exit"};
        // Parameters 1-Context : 2-Layout for the row : 3-ID of the View to which the data is written : 4-the Array of data
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_2, android.R.id.text1, values);
        LW.setAdapter(adapter);
        
        LW.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				int c=position;
				switch(c){
				case 0:
					//set up dialog
	                final Dialog dialog2 = new Dialog(MainActivity.this);
	                dialog2.setContentView(R.layout.urldialog);
	                dialog2.setTitle("Enter Server IP Address");
	                dialog2.setCancelable(true);
	                	 
	                
	                Button button2 = (Button) dialog2.findViewById(R.id.cancelButton);
	                button2.setOnClickListener(new View.OnClickListener() {
					       public void onClick(View v) {
					    	   dialog2.dismiss();
					       }
	                });
	                Button buttonurl = (Button) dialog2.findViewById(R.id.connectButton);
	                buttonurl.setOnClickListener(new View.OnClickListener() {
					       public void onClick(View v) {
					    	   et = (EditText)dialog2.findViewById(R.id.enterURL);
					    	   getURL=et.getText().toString();
					    	   Intent intent = new Intent(MainActivity.this,MjpegStreamActivity.class);
					    	   intent.putExtra("myURL", getURL);
					    	   startActivity(intent); 
					       }
	                });
	                //now that the dialog is set up, it's time to show it    
	                dialog2.show();
	            					
					break;
				case 1:
					break;
				case 2:
					 //set up dialog
	                final Dialog dialog = new Dialog(MainActivity.this);
	                dialog.setContentView(R.layout.creditdialog);
	                dialog.setTitle("VEye Credits");
	                dialog.setCancelable(true);
	                
	                TextView text = (TextView) dialog.findViewById(R.id.TextView01);
	                text.setText(R.string.credits);
	 
	                
	                Button button = (Button) dialog.findViewById(R.id.cancelButton1);
	                button.setOnClickListener(new View.OnClickListener() {
					       public void onClick(View v) {
					    	   dialog.dismiss();
					       }
	                });
	                //now that the dialog is set up, it's time to show it    
	                dialog.show();
					break;
				case 3:
					//set up dialog
	                final Dialog dialog1 = new Dialog(MainActivity.this);
	                dialog1.setContentView(R.layout.helpdialog);
	                dialog1.setTitle("VEye Help");
	                dialog1.setCancelable(true);
	              
	                TextView text1 = (TextView) dialog1.findViewById(R.id.TextView01);
	                text1.setText(R.string.help);
	 
	               
	                Button button1 = (Button) dialog1.findViewById(R.id.cancelButton2);
	                button1.setOnClickListener(new View.OnClickListener() {
					       public void onClick(View v) {
					    	   dialog1.dismiss();
					       }
	                });
	                //now that the dialog is set up, it's time to show it    
	                dialog1.show();
					break;
				case 4:
					//set up dialog
	                final Dialog dialog3 = new Dialog(MainActivity.this);
	                dialog3.setContentView(R.layout.exitdialog);
	                dialog3.setTitle("Do you want to Exit now?");
	                dialog3.setCancelable(true);
	                	 
	                
	                Button buttonNo = (Button) dialog3.findViewById(R.id.noButton);
	                buttonNo.setOnClickListener(new View.OnClickListener() {
					       public void onClick(View v) {
					    	   dialog3.dismiss();
					       }
	                });
	                
	                Button buttonYes = (Button) dialog3.findViewById(R.id.yesButton );
	                buttonYes.setOnClickListener(new View.OnClickListener() {
					       public void onClick(View v) {
					    	   finish();
					    	   
					       }
	                });
	                //now that the dialog is set up, it's time to show it    
	                dialog3.show();
	                break;
					
				}				
			}
        	
        });
    }
    
    @Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//finish();
	}
    
}

