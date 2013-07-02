package com.sakthipriyan.cricscore;

import java.util.Date;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CricScoreService extends Service{

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				int[] input = {};
				Request request = new Request(input,new Date());
				System.out.println("request :" + request);
				Response response = BackEnd.getInstance().fetchData(request);
				System.out.println("response :" + response);
			}
		}).start();
		
		return Service.START_STICKY;
	}

	
}
