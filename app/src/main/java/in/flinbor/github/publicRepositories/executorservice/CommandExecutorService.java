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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Command Executor based on ThreadPool
 * responsible for start commands in new Thread
 */
public class CommandExecutorService extends Service {

	public static final int THREADS_COUNT = 3;
	private static final String TAG = "CommandExecutorService";
	private static final int SHUTDOWN_DELAY = 1000 * 5;
	public static boolean LOG_ENABLED = true;
	private final Handler handler = new Handler();

	private final LongSparseArray<Future<?>> pendingOperations = new LongSparseArray<>();
	private ExecutorService executorService;
	private int startId;
	private final Runnable shutdownRunnable = new Runnable() {

		@Override
		public void run() {
			if (LOG_ENABLED) {
				Log.d(TAG, "stopSelf");
			}
			stopSelf(startId);
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		if (LOG_ENABLED) {
			Log.d(TAG, "onCreate");
		}
		executorService = Executors.newFixedThreadPool(THREADS_COUNT);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (LOG_ENABLED) {
			Log.d(TAG, "onDestroy");
		}
		executorService.shutdownNow();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		this.startId = startId;
		final long requestId = intent.getLongExtra(Command.EXTRA_REQUEST_ID, 0);
		if (LOG_ENABLED) {
			Log.d(TAG, "onStartCommand,requestId:" + requestId);
		}
		if (Command.ACTION_CANCEL.equals(intent.getAction())) {
			synchronized (handler) {
				Future<?> f = pendingOperations.get(requestId);
				if (f != null) {
					f.cancel(true);
					pendingOperations.remove(requestId);
					if (pendingOperations.size() == 0) {
						handler.postDelayed(shutdownRunnable, SHUTDOWN_DELAY);
					}
				}
			}
		} else {
			Future<?> future = executorService.submit(new Runnable() {

				@Override
				public void run() {
					Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
					Command cmd = null;
					try {
						cmd = createCommand(intent);
						cmd.run();
					} catch (Throwable e) {
						e.printStackTrace();
					} finally {
						if (cmd != null && !cmd.isResultPosted()) {
							if (LOG_ENABLED) {
								Log.e(TAG, "class " + cmd.getClass().getName() + " didn't send any result");
								Log.e(TAG, "RESULT_FAILURE sent manually");
							}
							cmd.postResult(Command.RESULT_FAILURE, null);
						}
						synchronized (handler) {
							if (pendingOperations.get(requestId) != null) {
								pendingOperations.remove(requestId);
								if (pendingOperations.size() == 0) {
									handler.postDelayed(shutdownRunnable, SHUTDOWN_DELAY);
								}
							}
						}
					}
				}
			});

			synchronized (handler) {
				handler.removeCallbacks(shutdownRunnable);
				pendingOperations.put(requestId, future);
			}
		}
		return START_NOT_STICKY;
	}

	private Command createCommand(Intent intent) {
		if (LOG_ENABLED) {
			Log.d(TAG, "action: " + intent.getAction());
		}
		try {
			Class<? extends Command> clazz = (Class<? extends Command>) Class.forName(intent.getAction());
			Constructor<? extends Command> ctor = clazz.getConstructor(Context.class, Intent.class);
			Command command = ctor.newInstance(this, intent);
			return command;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (Throwable th) {
			th.printStackTrace();
		}
		return null;
	}
}
