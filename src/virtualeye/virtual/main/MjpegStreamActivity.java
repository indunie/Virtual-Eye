//Recent updated on 17th of August 2012 at 11:16 PM ~ WLR
package virtualeye.virtual.main;

import android.app.Activity;
import android.os.Bundle;

import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

//**from Pop-up
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import virtualeye.virtual.main.ActionItem;
import virtualeye.virtual.main.QuickAction;

import virtualeye.virtual.main.LowPassFilter;
import virtualeye.virtual.main.MjpegInputStream;
import virtualeye.virtual.main.MjpegView;
import virtualeye.virtual.main.R;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ZoomControls;
import android.os.Build;

public class MjpegStreamActivity extends Activity implements SensorEventListener{

	private QuickAction quickAction1;
    private QuickAction quickAction2;
    private QuickAction quickAction3;
    private QuickAction quickAction4;
	
    private MjpegView mv;
    private static final int MENU_QUIT = 1;
    private ZoomControls zoombtn;
    private Button rb,ub,lb,db;
	private Button b1,b2,b3,b4,b5,b6,b7,b8,b9,bl,br;
    private static String URL;
    //Motion sensor variables
  	private SensorManager mSensorManager;
  	private Sensor mAccelerometer, mField;
  	private float[] mGravity = new float[3];
  	private float[] mMagnetic = new float[3];
  	private float[] values = new float[3];
  	private float[] smooth = new float[3];
  	//Socket control variables
  	public String inetaddr;
  	private InetAddress addr;
  	private Socket socket = null;
  	//private Socket socket1 = null;
  	private PrintWriter out=null;
  	private BufferedReader in=null;
  	//private PrintWriter out1;
  	private int check=0, check2=0;
  	private float data_arx[]={0,0,0,0,0}; //stores 5 sensor data history for pitch
  	private float data_ary[]={0,0,0,0,0}; //stores 5 sensor data history for azimuth
  	int pos=0; //keeps track of the position of data_ar
    float avgx = 0, avgy = 0;
    int maxkeep=5;//maximum accelerometer values to keep to get the average
    
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        inetaddr = getIntent().getExtras().getString("myURL");
        URL="http://"+inetaddr+":27015/c.mjpeg";
        
        //Sensor Initialization 
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		
		//to-do: Initialize home buttons
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        
        //Socket connection and Buffer creation
        try{
        	 if(Build.VERSION.SDK_INT>Build.VERSION_CODES.GINGERBREAD_MR1){
        	   
        	new Thread(new Runnable(){
  			  public void run(){
  				  try {
  					System.out.println("In ICS");	  
  				 //socket = new Socket(addr,27021);
  				addr = InetAddress.getByName(inetaddr);
  	     		socket = new Socket(addr, 27021);
  	     		//socket1 = new Socket(addr, 27017);
  	     		socket.setTcpNoDelay(true);
  	     		out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
  	     		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
  				 
  				 System.out.println("ICS socket connect 27021");
  				
						socket.setTcpNoDelay(true);
  				  } catch (SocketException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
  				 //System.out.println("ICS Getting input stream");
			  		try {
						out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
					
			  		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			  		} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
  			  }
  			  }).start();
        	
        	 }
        	 else{
        	
        	addr = InetAddress.getByName(inetaddr);
     		socket = new Socket(addr, 27021);
     		//socket1 = new Socket(addr, 27017);
     		socket.setTcpNoDelay(true);
     		out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
     		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
     		//out1 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket1.getOutputStream())),true);
     	}}
        catch (IOException e) {
     			e.printStackTrace();
     	}
        
		check=1; //check=1 is the right time to register listener
		
		//***Register sensors*/
    	mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
		mSensorManager.registerListener(this, mField, SensorManager.SENSOR_DELAY_UI);
        
        //Setup MJpeg view
        setContentView(R.layout.stream);
        mv = (MjpegView)findViewById(R.id.surfaceView1);    
        
        ////send screen size to server side
		Display display = getWindowManager().getDefaultDisplay();
		/*Point point = new Point();
	    try { //for API level 13 or above
	        display.getSize(point);
	    } catch (java.lang.NoSuchMethodError ignore) { // Older device
	        point.x = display.getWidth();
	        point.y = display.getHeight();
	    }
		int width = point.x;
		int height = point.y;*/
		
		int width = display.getWidth();
		int height  = display.getHeight();
		out.println(String.valueOf(width));
		out.println(String.valueOf(height));
		////sent screen size
                
        //Setup Zoom Controls
        /*zoombtn = (ZoomControls) findViewById(R.id.zoomcontrols);
        
        zoombtn.setOnZoomInClickListener(new OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				out.println("W\0");	//zoom in by sending W
			}});
        
        zoombtn.setOnZoomOutClickListener(new OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				out.println("S\0");	//zoom out by sending S
			}});*/
        b1=(Button)findViewById(R.id.b1);
        b2=(Button)findViewById(R.id.b2);
        b3=(Button)findViewById(R.id.b3);
        b4=(Button)findViewById(R.id.b4);
        b5=(Button)findViewById(R.id.b5);
        b6=(Button)findViewById(R.id.b6);
        b7=(Button)findViewById(R.id.b7);
        b8=(Button)findViewById(R.id.b8);
        b9=(Button)findViewById(R.id.b9);
        bl=(Button)findViewById(R.id.bl);
        br=(Button)findViewById(R.id.br);
        
        //unwanted buttons set to invisible
        b1.setVisibility(View.INVISIBLE);
        b3.setVisibility(View.INVISIBLE);
        b5.setVisibility(View.INVISIBLE);
        b7.setVisibility(View.INVISIBLE);
        b9.setVisibility(View.INVISIBLE);
        
        //left and right buttons set invisible
        bl.setVisibility(View.INVISIBLE);
        br.setVisibility(View.INVISIBLE);
        
        // left, right, forward, backward set to invisible
        b2.setVisibility(View.INVISIBLE);
        b4.setVisibility(View.INVISIBLE);
        b6.setVisibility(View.INVISIBLE);
        b8.setVisibility(View.INVISIBLE);
        
        
        //old mannual navigation buttons(left, right, forward, backward)
        
        /*b4.setOnTouchListener(new View.OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					out.println("L\0");
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	out.println("A\0");
		        }
				return false;
			}
		});
			
        b2.setOnTouchListener(new View.OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {				
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					out.println("U\0");
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	out.println("A\0");
		        }
				return true;
			}
		});
        
        b6.setOnTouchListener(new View.OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					out.println("R\0");
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	out.println("A\0");
		        }
				return false;
			}
		});
        
        b8.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					out.println("D\0");
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	out.println("A\0");
		        }
				return false;
			}
		});
        
        // left right button
        
        bl.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					out.println("N\0");
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	out.println("A\0");
		        }
				return false;
			}
		});
        
        br.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					out.println("M\0");
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	out.println("A\0");
		        }
				return false;
			}
		});
        */
        try{
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.GINGERBREAD_MR1){
            
            new Thread(new Runnable(){public void run(){
      		  System.out.println("reading data from mjpeg input stream ics");
    			try {
					mv.setSource(MjpegInputStream.read(URL));
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			mv.setDisplayMode(MjpegView.SIZE_FULLSCREEN);
    	        mv.showFps(false);
            }}).start();
            	
            }else{
            
            mv.setSource(MjpegInputStream.read(URL));
            mv.setDisplayMode(MjpegView.SIZE_FULLSCREEN);
            mv.showFps(false);
            }}
        catch (IOException e) {
     			e.printStackTrace();
     	}
        
        //
        
        ImageView topleft = (ImageView) findViewById(R.id.topleft);
        topleft.setDrawingCacheEnabled(true);
        topleft.setOnTouchListener(changeColorListener);
        
        ImageView topright = (ImageView) findViewById(R.id.topright);
        topright.setDrawingCacheEnabled(true);
        topright.setOnTouchListener(changeColorListener);
        
        ImageView bottomleft = (ImageView) findViewById(R.id.bottomleft);
        bottomleft.setDrawingCacheEnabled(true);
        bottomleft.setOnTouchListener(changeColorListener);
        
        ImageView bottomright = (ImageView) findViewById(R.id.bottomright);
        bottomright.setDrawingCacheEnabled(true);
        bottomright.setOnTouchListener(changeColorListener);


        ActionItem dutchEra 	= new ActionItem(1, "DUTCH ERA");
        ActionItem britishEra 	= new ActionItem(2, "BRITISH ERA");
        ActionItem portugueseEra 	= new ActionItem(0, "PORTUGUESE ERA");
        
        ActionItem zoomIn 	= new ActionItem(1, "ZOOM IN");
        ActionItem zoomOut 	= new ActionItem(0, "ZOOM OUT");

        ActionItem sound 	= new ActionItem(1, "SOUND");
        ActionItem annotation 	= new ActionItem(0, "ANNOTATION");
        
        ActionItem left 	= new ActionItem(0, "LEFT");
        ActionItem right 	= new ActionItem(2, "RIGHT");
        ActionItem forward 	= new ActionItem(3, "FORWARD");
        ActionItem backward 	= new ActionItem(1, "BACKWARD");

        //use setSticky(true) to disable QuickAction dialog being dismissed after an item is clicked
        left.setSticky(true); 
        right.setSticky(true);
        forward.setSticky(true);
        backward.setSticky(true); 
        zoomIn.setSticky(true);
        zoomOut.setSticky(true);
        
        //create QuickAction. Use QuickAction.VERTICAL or QuickAction.HORIZONTAL param to define layout
        //orientation
        quickAction1 = new QuickAction(this, QuickAction.VERTICAL);
        quickAction2 = new QuickAction(this, QuickAction.VERTICAL);
        quickAction3 = new QuickAction(this, QuickAction.VERTICAL);
        quickAction4 = new QuickAction(this, QuickAction.VERTICAL);

        //add action items into QuickAction
        quickAction2.addActionItem(dutchEra);
        quickAction2.addActionItem(britishEra);
        quickAction2.addActionItem(portugueseEra);
        
        quickAction1.addActionItem(left);
        quickAction1.addActionItem(right);
        quickAction1.addActionItem(forward);
        quickAction1.addActionItem(backward);

        quickAction3.addActionItem(zoomIn);
        quickAction3.addActionItem(zoomOut);

        quickAction4.addActionItem(sound);
        quickAction4.addActionItem(annotation);
 
        // ................ 
        quickAction1.setOnActionItemTouchListener(new QuickAction.OnActionItemTouchListener() {
            @Override
            public void onItemTouch(QuickAction source, int pos, int action) {
                ActionItem actionItem = quickAction1.getActionItem(pos);
                Log.d("Menu",actionItem.getTitle());
                
                if(action == 1){
                  if(pos==0){
                    out.println("L\0");
                    Toast.makeText(getApplicationContext(), "left press", Toast.LENGTH_SHORT).show();
                  }
                  else if(pos==1){
                    out.println("R\0");
                    Toast.makeText(getApplicationContext(), "right press", Toast.LENGTH_SHORT).show();
                  }
                  else if(pos==2){
                    out.println("U\0");
                    Toast.makeText(getApplicationContext(), "forward press", Toast.LENGTH_SHORT).show();
                  }
                  else if(pos==3){
                    out.println("D\0");
                    Toast.makeText(getApplicationContext(), "backward press", Toast.LENGTH_SHORT).show();
                  }
                }
                else if(action == 0){
                  out.println("A\0");
                  if(pos==0){
                    Toast.makeText(getApplicationContext(), "left release", Toast.LENGTH_SHORT).show();
                  }
                  else if(pos==1){
                    Toast.makeText(getApplicationContext(), "right release", Toast.LENGTH_SHORT).show();
                  }
                  else if(pos==2){
                    Toast.makeText(getApplicationContext(), "forward release", Toast.LENGTH_SHORT).show();
                  }
                  else if(pos==3){
                    Toast.makeText(getApplicationContext(), "backward release", Toast.LENGTH_SHORT).show();
                  }
                }
                
            }
        });




        //Set listener for action item clicked
       /* quickAction1.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                ActionItem actionItem = quickAction1.getActionItem(pos);
                Log.d("Menu",actionItem.getTitle());
                
                if(pos==0){
                	out.println("L\0");
                	//out.println("A\0");
                	Toast.makeText(getApplicationContext(), "left", Toast.LENGTH_SHORT).show();
                }
                else if(pos==1){
                	out.println("R\0");
                	//out.println("A\0");
                	Toast.makeText(getApplicationContext(), "right", Toast.LENGTH_SHORT).show();
                }
                else if(pos==2){
                	out.println("U\0");
                	//out.println("A\0");
                	Toast.makeText(getApplicationContext(), "forward", Toast.LENGTH_SHORT).show();
                }
                else if(pos==3){
                	out.println("D\0");
                	//out.println("A\0");
                	Toast.makeText(getApplicationContext(), "backward", Toast.LENGTH_SHORT).show();
                }
            }
        });*/
        
        

        //set listnener for on dismiss event, this listener will be called only if QuickAction dialog was dismissed
        //by clicking the area outside the dialog.
        quickAction1.setOnDismissListener(new QuickAction.OnDismissListener() {
            @Override
            public void onDismiss() {
                Toast.makeText(getApplicationContext(), "Dismissed", Toast.LENGTH_SHORT).show();
            }
        });
        
        quickAction3.setOnActionItemTouchListener(new QuickAction.OnActionItemTouchListener() {
            @Override
            public void onItemTouch(QuickAction source, int pos, int action) {
                ActionItem actionItem2 = quickAction3.getActionItem(pos);
                Log.d("Menu",actionItem2.getTitle());
                
                if(action == 1){
                  if(pos==0){
                    out.println("W\0");
                    Toast.makeText(getApplicationContext(), "ZoomIn press", Toast.LENGTH_SHORT).show();
                  }
                  else if(pos==1){
                    out.println("S\0");
                    Toast.makeText(getApplicationContext(), "ZoomOut press", Toast.LENGTH_SHORT).show();
                  }
                }
                else if(action == 0){
                  out.println("A\0");
                  if(pos==0){
                    Toast.makeText(getApplicationContext(), "left release", Toast.LENGTH_SHORT).show();
                  }
                  else if(pos==1){
                    Toast.makeText(getApplicationContext(), "right release", Toast.LENGTH_SHORT).show();
                  }
                }
                
            }
        });
        
        /*quickAction3.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                ActionItem actionItem = quickAction3.getActionItem(pos);
                Log.d("Menu",actionItem.getTitle());
                
                if(pos==0){
                	out.println("W\0");
                	//out.println("A\0");
                	Toast.makeText(getApplicationContext(), "Zoom in", Toast.LENGTH_SHORT).show();
                }
                else if(pos==1){
                	out.println("S\0");
                	//out.println("A\0");
                	Toast.makeText(getApplicationContext(), "Zoom out", Toast.LENGTH_SHORT).show();
                }
               
            }
        });
        */
        
    }
    
    public void Hello(View v){

    }
    
    
    
    /* Creates the menu items */
    public boolean onCreateOptionsMenu(Menu menu) {    
    
    	/*menu.add(0, MENU_QUIT, 0, "Quit");
    
    	return true;*/
    	
    	MenuInflater inflator = new MenuInflater(this);
		inflator.inflate(R.layout.menu, menu);
	
		return super.onCreateOptionsMenu(menu);
    }

    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {    
        /*switch (item.getItemId()) {
            case MENU_QUIT:
                finish();
                return true;    
            }    
        return false;*/
    	
    	//Checking which menu item is Selected
        switch (item.getItemId()) {
  			case R.id.DUTCH_ERA:
  				Toast.makeText(getApplicationContext(), "You Clicked On Dutch Menu Item", Toast.LENGTH_LONG).show();
  				break;
  			case R.id.BRITISH_ERA:
  				Toast.makeText(getApplicationContext(), "You Clicked On British Menu Item", Toast.LENGTH_LONG).show();
  				break;
  			case R.id.PORTUGUESE_ERA:
  				Toast.makeText(getApplicationContext(), "You Clicked On Portuguese Menu Item", Toast.LENGTH_LONG).show();
  				break;
  			case R.id.SOUNDS_ON:
  				Toast.makeText(getApplicationContext(), "You Clicked On View Details SubMenu Item", Toast.LENGTH_LONG).show();
  				break;
  			case R.id.SOUNDS_OFF:
  				Toast.makeText(getApplicationContext(), "You Clicked On View Details SubMenu Item", Toast.LENGTH_LONG).show();
  				break;
  			case R.id.ANNOTATION_ON:
  				Toast.makeText(getApplicationContext(), "You Clicked On View Details SubMenu Item", Toast.LENGTH_LONG).show();
  				break;
  			case R.id.ANNOTATION_OFF:
  				Toast.makeText(getApplicationContext(), "You Clicked On View Details SubMenu Item", Toast.LENGTH_LONG).show();
  				break;
  			case R.id.LEFT:
  				Toast.makeText(getApplicationContext(), "You Clicked On View List SubMenu Item", Toast.LENGTH_LONG).show();
  				break;
  			case R.id.RIGHT:
  				Toast.makeText(getApplicationContext(), "You Clicked On View List SubMenu Item", Toast.LENGTH_LONG).show();
  				break;
  			case R.id.FORWARD:
  				Toast.makeText(getApplicationContext(), "You Clicked On View List SubMenu Item", Toast.LENGTH_LONG).show();
  				break;
  			case R.id.BACKWARD:
  				Toast.makeText(getApplicationContext(), "You Clicked On View List SubMenu Item", Toast.LENGTH_LONG).show();
  				break;
  			case R.id.IN:
  				Toast.makeText(getApplicationContext(), "You Clicked On ZoomIn SubMenu Item", Toast.LENGTH_LONG).show();
  				break;
  			case R.id.OUT:
  				Toast.makeText(getApplicationContext(), "You Clicked On ZoomOut SubMenu Item", Toast.LENGTH_LONG).show();
  				break;
        	}
  		return super.onOptionsItemSelected(item);
    }

    public void onPause() {
        super.onPause();
        mv.stopPlayback();
        mSensorManager.unregisterListener(this);
    }
    
    @Override
  	public void onBackPressed(){
    	try{
    		mSensorManager.unregisterListener(this);
    		//out.println("D");
	    	socket.close();
	    	}
	    	catch (IOException e) {
	 			e.printStackTrace();
	    	}
	    	mv.stopPlayback();
	    	finish();
    }
    
    //onResume registers mSensormanager only after onCreate is called and the value of check changed from 0 to 1
    protected void onResume() {
		super.onResume();
		if(check==1){
			mSensorManager.registerListener(this, mAccelerometer,SensorManager.SENSOR_DELAY_UI);
			mSensorManager.registerListener(this, mField, SensorManager.SENSOR_DELAY_UI);
		}
	}  
    
    //Sensor Event Listener
  	public void onSensorChanged(SensorEvent event) {
  		switch(event.sensor.getType()) {
  			case Sensor.TYPE_ACCELEROMETER:
  				//mGravity = event.values.clone();
  				
  				smooth = LowPassFilter.filter(0.5f, 1.0f, event.values, mGravity); //grav-> mgravity (initioalize to zero)
  				mGravity[0] = smooth[0];
  				mGravity[1] = smooth[1];
  				mGravity[2] = smooth[2];
  				break;
  				
  			case Sensor.TYPE_MAGNETIC_FIELD:
  				smooth = LowPassFilter.filter(2.0f, 4.0f, event.values, mMagnetic);
  				mMagnetic[0] = smooth[0];
  				mMagnetic[1] = smooth[1];
  				mMagnetic[2] = smooth[2];
  				break;
  				
  			default:
  				return;
  		}
  		if(mGravity != null && mMagnetic != null) {			
  			sensorworkload();
  		}
  	}
  	//Sensor Listener ends
    
	public void sensorworkload() {
		
		float[] temp = new float[9];
		float[] R = new float[9];
		float yold = values[0];
		float xold = values[1];
		
		try{
  			//Load the Rotation Matrix to R
  			SensorManager.getRotationMatrix(temp, null, mGravity, mMagnetic);
  			//Map according the change of orientation of Phone (landscape mode)
  			SensorManager.remapCoordinateSystem(temp, SensorManager.AXIS_X, SensorManager.AXIS_Z, R);
  			//**Return the orientation values		
  			SensorManager.getOrientation(R, values);		
  			//**Display the compass direction
  			float x,y;
  			
  			//y=(float)((f * 180) / Math.PI); //to degrees
  			
  			if(check2==0)
  			{
  				xold=values[1];
  				yold=values[0];
  			}
  			
  			//updating sensor history array
  			y=getDirectionFromDegrees(values[0], yold);
  			x=getDirectionFromDegrees(values[1], xold);
  			
  			
  			if(check2>(maxkeep-1))	//array is full
  			{
  				avgy  = (avgy*maxkeep - data_ary[pos] + y)/maxkeep; //avgy = (avgy*5 - previousdata + newdata)/5 calculates new avg
	  			data_ary[pos]=y;
	  			
	  			avgx  = (avgx*maxkeep - data_arx[pos] + x)/maxkeep; 
	  			data_arx[pos]=x;	  				
  			}
  			else
  			{
  				check2++;
  				avgy  = (avgy*(check2-1) - data_ary[pos] + y)/check2;
  				data_ary[pos]=y;
  				avgx  = (avgx*(check2-1) - data_arx[pos] + x)/check2;
  				data_arx[pos]=x;
  			}
  			
  			if((avgy<4.0 && avgy>=0.2 && check2>3) || (avgy>-4.0 && avgy<=-0.2)){
  				out.println('Y'+Float.toString(avgy)+'\0');
  			}
  			else if((avgx<4.0 && avgx>=0.2) || (avgx>-4.0 && avgx<=-0.2)){
  				out.println('X'+Float.toString(avgx)+'\0');
  			}
  			pos=(pos+1)%maxkeep;
		}
		catch (Exception e){
			e.printStackTrace();
		}			
	}
  	
	//Used in sensor workload function (immediately previous)
  	public float getDirectionFromDegrees(float ndegrees,float odegrees) {
		
		float f;
		
		if(ndegrees*odegrees >= 0) { f=ndegrees-odegrees; }
		else{f=(float)(2*Math.PI+(ndegrees-odegrees));}
		f=(float)((f * 180) / Math.PI); //convert radian to degrees
		return f;
	}
      
	public void onAccuracyChanged(Sensor sensor, int accuracy) { }
	
	private final View.OnTouchListener changeColorListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(final View v, MotionEvent event) {

            Bitmap bmp = Bitmap.createBitmap(v.getDrawingCache());
            int color = 0;
            try {
                color = bmp.getPixel((int) event.getX(), (int) event.getY());
            } catch (Exception e) {
                // e.printStackTrace();
            }
            if (color == Color.TRANSPARENT) {
                //Log.d("Touch", "Outside");

            	//Toast.makeText(getApplicationContext(), "Clicked outside", Toast.LENGTH_SHORT).show();
                return false;
            }else if(v.getId() == R.id.topleft) {
                quickAction1.show(v);
                
            	 //Toast.makeText(getApplicationContext(), "Clicked inside", Toast.LENGTH_SHORT).show();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d("Touch","Down");
                        //do something here
                        break;
                    case MotionEvent.ACTION_OUTSIDE:
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                   // case MotionEvent.ACTION_SCROLL:
                        //break;
                    case MotionEvent.ACTION_UP:
                        Log.d("Touch","Up");
                        //do something here
                        break;
                    default:
                        break;
                }
                return true;

            }
            
            else if(v.getId() == R.id.topright) {
                quickAction2.show(v);
                
            	 Toast.makeText(getApplicationContext(), "Clicked inside", Toast.LENGTH_SHORT).show();
                
                return true;

            }
            else if(v.getId() == R.id.bottomleft) {
                quickAction3.show(v);
                
            	 Toast.makeText(getApplicationContext(), "Clicked inside", Toast.LENGTH_SHORT).show();
                
                return true;

            }
            else if(v.getId() == R.id.bottomright) {
                quickAction4.show(v);
                
            	 Toast.makeText(getApplicationContext(), "Clicked inside", Toast.LENGTH_SHORT).show();
                
                return true;
            }
			return true;
        }
    };
}