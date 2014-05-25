package fr.magistry.taigime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

public class CommunityActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_community);
		WebView wb = (WebView) this.getWindow().findViewById(R.id.webView1);
		InputStream inputStream = getResources().openRawResource(R.raw.credits);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuffer sb = new StringBuffer();
		try {
		try {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
		}
		finally {
			reader.close();
		}
		}
        catch( IOException e) {
    			Log.e("IME_DATA", "exception when reading the html file");
        }
       
		wb.loadData(sb.toString(), "text/html; charset=UTF-8", "charset=UTF8");
		
		Button bnGo = (Button) getWindow().findViewById(R.id.buttonGoCommunity);
		bnGo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View b) {
            	String url = "http://plus.google.com/communities/101493598847860391958";
        		Intent i = new Intent(Intent.ACTION_VIEW);
        		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		i.setData(Uri.parse(url));
        		startActivity(i);
            }
        });
		
		return;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_community, menu);
		return true;
	}

}
