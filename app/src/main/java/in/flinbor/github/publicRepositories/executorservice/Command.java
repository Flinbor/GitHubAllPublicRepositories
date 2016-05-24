/*
 * Copyright 2016 Flinbor Bogdanov Oleksandr
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package in.flinbor.github.publicRepositories.executorservice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import in.flinbor.github.publicRepositories.app.App;

/**
 * Base Class that should be extended by custom Commands to execute
 */
public abstract class Command implements Runnable {

	public static final int RESULT_PROGRESS = 1;

	public static final int RESULT_SUCCESSFUL = 0;

	public static final int RESULT_FAILURE = -1;

	public static final int RESULT_CANCELED = -2;

	public static final String ACTION_CANCEL 			   = App.getApp().getPackageName().concat("CANCEL");

	public static final String EXTRA_REQUEST_ID 		   = App.getApp().getPackageName().concat("REQUEST_ID");

	public static final String EXTRA_RESULT_RECEIVER 	   = App.getApp().getPackageName().concat("RESULT_RECEIVER");

	public static final String EXTRA_EXECUTOR_CLASS 	   = App.getApp().getPackageName().concat("EXECUTOR_CLASS");

	public static final String EXTRA_NESTED_COMMAND_CLASS  = App.getApp().getPackageName().concat("NESTED_COMMAND_CLASS");

	public static final String EXTRA_NESTED_COMMAND_EXTRAS = App.getApp().getPackageName().concat("NESTED_COMMAND_EXTRAS");

	private final Context 		 context;

	private final ResultReceiver resultReceiver;

	private final long 			 requestId;

	private boolean 			 isResultPosted = false;

	private Intent 				 intent;

	protected Command(Context context, Intent intent) {
		this.context = context.getApplicationContext();
		resultReceiver = intent.getParcelableExtra(EXTRA_RESULT_RECEIVER);
		requestId = intent.getLongExtra(EXTRA_REQUEST_ID,
				ServiceHelper.DEFAULT_REQUEST_ID);
		this.intent = intent;
	}

	protected Context getContext() {
		return context;
	}

	public final void postResult(int resultCode, Bundle resultData) {
		if (CommandExecutorService.LOG_ENABLED) {
			String str = getTextResult(resultCode);
			String resString = null;
			if (resultData != null) {
				resString = resultData.toString();
			}
			Log.d(this.getClass().getName(),
					str
							+ " for "
							+ requestId
							+ (resultData != null ? " | "
							+ resultData.toString().substring(0,
							Math.min(resString.length(), 50))
							: ""));
		}
		if (resultReceiver != null) {
			resultReceiver.send(resultCode, resultData);
		}
		isResultPosted = resultCode == RESULT_SUCCESSFUL || resultCode == RESULT_FAILURE || isResultPosted;
	}

	public final void postProgress(Bundle progress) {
		resultReceiver.send(RESULT_PROGRESS, progress);
	}

	public long getRequestId() {
		return requestId;
	}

	public boolean isResultPosted() {
		return isResultPosted;
	}

	/**
	 *
	 * @return isInterrupted of caller thread, so should be called only in run
	 *         method.
	 */
	protected boolean isCancelled() {
		return Thread.currentThread().isInterrupted();
	}

	public static String getTextResult(int result) {
		switch (result) {
			case RESULT_SUCCESSFUL:
				return "RESULT_SUCCESSFUL";

			case RESULT_FAILURE:
				return "RESULT_FAILURE";

			case RESULT_PROGRESS:
				return "RESULT_PROGRESS";

			case RESULT_CANCELED:
				return "RESULT_CANCELED";

			default:
				return "<UNKNOWN>";
		}
	}

	@Override
	public void run() {
		if (CommandExecutorService.LOG_ENABLED){
			Log.d(this.getClass().getName(),"run command with extras:"+intent.getExtras().toString());
		}
		doWork();
	}

	public abstract void doWork();

	public static Bundle prepareParameters() {
		return new Bundle();
	}
}
