package virtualeye.virtual.main;

import virtualeye.virtual.main.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class Splash extends Activity {

	View viewToAnimate1;
	View viewToAnimate2;
	private Animation out1,out2;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		//Animation initializations
		out1 = AnimationUtils.makeOutAnimation(Splash.this, false);
		out2 = AnimationUtils.makeOutAnimation(Splash.this, true);
		//Setting durations for animations
		out1.setDuration(3000);
		out2.setDuration(3000);
		//Setting views for animations
		viewToAnimate1 = findViewById(R.id.theView1);
		viewToAnimate2 = findViewById(R.id.theView2);
		
		new Handler().postDelayed(new Runnable() {
            public void run() {
                   
                    /* Create an intent that will start the main activity. */
                    Intent mainIntent = new Intent(Splash.this,MainActivity.class);
                    Splash.this.startActivity(mainIntent);
                    /* Finish splash activity so user cant go back to it. */
                    //Splash.this.finish();
                    /* Apply our splash exit (fade out) and main
                       entry (fade in) animation transitions. */
                    overridePendingTransition(R.anim.mainanim,R.anim.splashanim);
            }
    },3000);
		
		//Animation happening
		viewToAnimate1.startAnimation(out1);
		viewToAnimate2.startAnimation(out2);
		viewToAnimate1.setVisibility(View.INVISIBLE);
		viewToAnimate2.setVisibility(View.INVISIBLE);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}
}