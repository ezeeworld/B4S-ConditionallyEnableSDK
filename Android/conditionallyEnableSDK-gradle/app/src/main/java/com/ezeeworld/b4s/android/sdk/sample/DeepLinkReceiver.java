package com.ezeeworld.b4s.android.sdk.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DeepLinkReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent start = new Intent(context, LaunchActivity.class);
		start.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		start.putExtra("extraEventId", intent.getStringExtra("b4s_intent_session"));
		context.startActivity(start);
	}

}
