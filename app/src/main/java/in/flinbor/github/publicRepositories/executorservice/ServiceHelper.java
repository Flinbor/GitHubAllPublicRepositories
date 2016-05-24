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
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class helper for run Commands on CommandExecutorService
 */
public class ServiceHelper {

	public static final long DEFAULT_REQUEST_ID = -1;

	private final Handler 	handler;

	private final Context 	context;

	private final LongSparseArray<Intent> pendingOperations = new LongSparseArray<>();

	// maked CopyOnWrite because ConcurrentModificationException was caught
	private final CopyOnWriteArrayList<WeakReference<OnServiceResultListener>> listeners = new CopyOnWriteArrayList<>();

	public interface OnServiceResultListener {
		void onServiceResult(long requestId, Intent requestIntent,
							 int resultCode, Bundle resultData, long nestedRequestId);
	}

	public ServiceHelper(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
	}

	/**
	 * Add listener to listen results of executing commands in command executor service
	 *
	 * @param listener
	 */
	public void addListener(OnServiceResultListener listener) {
		if (listener != null) {
			for (WeakReference<OnServiceResultListener> ref : listeners) {
				if (ref.get() != null && ref.get() == listener) {
					return;
				}
			}
			listeners.add(new WeakReference<>(listener));
		}
	}

	/**
	 * Remove listener
	 * @param listener
	 */
	public void removeListener(OnServiceResultListener listener) {
		if (listener != null) {
			for (WeakReference<OnServiceResultListener> ref : listeners) {
				if (ref.get() != null && ref.get() == listener) {
					listeners.remove(ref);
				}
			}
		}
	}

	/**
	 * Method which check command with requestId is pending or not
	 *
	 * @param requestId
	 * @return true if command with the request id is pending otherwise false
	 */
	public boolean isPending(long requestId) {
		return pendingOperations.get(requestId) != null;
	}

	protected static long createId() {
		return SystemClock.elapsedRealtime();
	}

	protected long runRequest(final long requestId, final Intent intent) {
		return runRequest(requestId, intent, false);
	}

	/**
	 * Method which cancel request of pending command with the same parameters
	 *
	 * @param intent - Intent which describes a new command
	 * @return true if there was cancel request otherwise return false
	 */
	protected boolean cancelRequest(final Intent intent) {
		Long existingRequestId = getPendingRequestWithTheSameParameters(intent);
		if (existingRequestId != null) {
			cancelRequest(existingRequestId);
			return true;
		}
		return false;
	}

	protected long runRequest(final long requestId, final Intent intent, final boolean isNoDuplication) {
		if (isNoDuplication){
			Long key = getPendingRequestWithTheSameParameters(intent);
			if (key != null) return key;
		}

		pendingOperations.append(requestId, intent);
		context.startService(intent);
		return requestId;
	}

	/**
	 * Method which return request id of pending command with the same parameters
	 *
	 * @param intent - Intent which describes a new command and we want to check there is the command with the same parameters as in this intent or not
	 * @return request id of command which is executing now which have the same parameters or null in case if there is not command with the same parameters
	 */
	protected Long getPendingRequestWithTheSameParameters(Intent intent) {
		for (int i = 0; i < pendingOperations.size(); ++i){
			long key = pendingOperations.keyAt(i);
			Intent existingIntent = pendingOperations.get(key);
			if (TextUtils.equals(intent.getAction(), existingIntent.getAction())){
				Bundle extras = intent.getExtras();
				Bundle existingExtras = existingIntent.getExtras();
				if (existingExtras == null && extras == null){
					return key;
				}
				if (existingExtras != null && extras != null) {
					boolean extrasIsEqual = true;
					for (String extrasKey : extras.keySet()) {
						if (!existingExtras.containsKey(extrasKey)){
							extrasIsEqual = false;
							break;
						}
                        /*
                            ignore that intent has different request id or result receiver
                         */
						if (TextUtils.equals(Command.EXTRA_REQUEST_ID, extrasKey) || TextUtils.equals(Command.EXTRA_RESULT_RECEIVER, extrasKey)){
							continue;
						}
						Object value = extras.get(extrasKey);
						Object existingValue = existingExtras.get(extrasKey);

						if ( (value == null && existingValue != null) || (existingValue == null && value != null)){
							extrasIsEqual = false;
							break;
						} else if (value != null && existingValue != null){
							if (!value.equals(existingValue)){
								extrasIsEqual = false;
								break;
							}
						}
					}
					if (extrasIsEqual){
						return key;
					}
				}
			}
		}
		return null;
	}

	private Intent prepareIntent(final String commandClassName, final Bundle extras, final long requestId, final Class<? extends Service> cls) {
		Intent i = new Intent(context, cls);
		i.setAction(commandClassName);
		i.putExtras(extras);
		i.putExtra(Command.EXTRA_REQUEST_ID, requestId);
		i.putExtra(Command.EXTRA_EXECUTOR_CLASS, cls);
		i.putExtra(Command.EXTRA_RESULT_RECEIVER,
				new ResultReceiver(handler) {
					@Override
					protected void onReceiveResult(int resultCode, Bundle resultData) {
						Intent originalIntent = pendingOperations.get(requestId);
						if (originalIntent != null) {
							if (resultCode != Command.RESULT_PROGRESS){
								pendingOperations.remove(requestId);
							}
							long nestedRequestId = ServiceHelper.DEFAULT_REQUEST_ID;
							if (resultCode == Command.RESULT_SUCCESSFUL){
								if (originalIntent.hasExtra(Command.EXTRA_NESTED_COMMAND_CLASS)){
									getRequest(originalIntent.getStringExtra(Command.EXTRA_NESTED_COMMAND_CLASS), (Bundle) originalIntent.getParcelableExtra(Command.EXTRA_NESTED_COMMAND_EXTRAS));
								}
							}
							for (WeakReference<OnServiceResultListener> ref : listeners) {
								OnServiceResultListener listener = ref.get();
								if (listener == null) {
									listeners.remove(ref);
								} else {
									listener.onServiceResult(requestId, originalIntent,
											resultCode, resultData, nestedRequestId);
								}
							}
						}
					}
				});

		return i;
	}

	private Intent prepareCancelIntent(long requestId, Class<? extends Service> cls) {
		Intent cancelIntent = new Intent(context, cls);
		cancelIntent.setAction(Command.ACTION_CANCEL);
		cancelIntent.putExtra(Command.EXTRA_REQUEST_ID, requestId);
		return cancelIntent;
	}

	/**
	 * Cancel pending operation with requestId (listeners won't notified but
	 * operation still going to be done)
	 *
	 * @param requestId
	 * @return true if operation was pending and successfully removed from
	 *         pending list
	 */
	public boolean cancelRequest(long requestId) {
		Intent originalIntent = pendingOperations.get(requestId);
		if (originalIntent != null) {
			pendingOperations.remove(requestId);
			for (WeakReference<OnServiceResultListener> ref : listeners) {
				OnServiceResultListener listener = ref.get();
				if (listener == null) {
					listeners.remove(ref);
				} else {
					listener.onServiceResult(requestId, originalIntent, Command.RESULT_CANCELED, null, DEFAULT_REQUEST_ID);
				}
			}
			@SuppressWarnings("unchecked")
			Class<? extends Service> cls = (Class<? extends Service>) originalIntent.getSerializableExtra(Command.EXTRA_EXECUTOR_CLASS);
			context.startService(prepareCancelIntent(requestId, cls));
			return true;
		}
		return false;
	}

	/**
	 * Start request in service which have class cls
	 *
	 * @param commandClassName - Class name of command
	 * @param extras - bundle with parameters of request
	 * @param cls - Class of executing Service
	 * @param avoidDuplication if true then return existing request if of pending command with the same parameters as in extras
	 * @param cancelTheSameRequestBeforeStart if true then cancel pending request with the same parameters
	 * @return request of id
	 */
	public long getRequest(final String commandClassName, final Bundle extras, final Class<? extends Service> cls, boolean avoidDuplication, boolean cancelTheSameRequestBeforeStart){
		final long requestId = createId();


		Intent intent;
		while (true) {
			intent = prepareIntent(commandClassName, extras, requestId, cls);
			if (cancelTheSameRequestBeforeStart) {
				if (!cancelRequest(intent)) {
					break;
				}
			} else {
				break;
			}
		}
		return runRequest(requestId, intent, avoidDuplication);
	}

	/**
	 * Start request in service with CommandExecutorService class
	 *
	 * @param commandClassName - Class name of command
	 * @param extras - bundle with parameters of request
	 * @param avoidDuplication if true then return existing request if of pending command with the same parameters as in extras
	 * @param cancelTheSameRequestBeforeStart if true then cancel pending request with the same parameters
	 * @return request of id
	 */
	public long getRequest(final String commandClassName, final Bundle extras, boolean avoidDuplication, boolean cancelTheSameRequestBeforeStart){
		final long requestId = createId();

		Intent intent;
		while (true) {
			intent = prepareIntent(commandClassName, extras, requestId, CommandExecutorService.class);
			if (cancelTheSameRequestBeforeStart) {
				if (!cancelRequest(intent)) {
					break;
				}
			} else {
				break;
			}
		}
		return runRequest(requestId, intent, avoidDuplication);
	}

	/**
	 * Start request without parameters in service with CommandExecutorService class
	 *
	 * @param commandClassName - Class name of command
	 * @param avoidDuplication if true then return existing request if of pending command with the same parameters as in extras
	 * @param cancelTheSameRequestBeforeStart if true then cancel pending request with the same parameters
	 * @return request of id
	 */
	public long getRequest(final String commandClassName, boolean avoidDuplication, boolean cancelTheSameRequestBeforeStart){
		final long requestId = createId();

		Intent intent;
		while (true) {
			intent = prepareIntent(commandClassName, Command.prepareParameters(), requestId, CommandExecutorService.class);
			if (cancelTheSameRequestBeforeStart) {
				if (!cancelRequest(intent)) {
					break;
				}
			} else {
				break;
			}
		}
		return runRequest(requestId, intent, avoidDuplication);
	}

	/**
	 * Start request in service with CommandExecutorService class with avoidDuplication and no cancel previous the same request
	 *
	 * @param commandClassName - Class name of command
	 * @param extras - bundle with parameters of request
	 * @return request of id
	 */
	public long getRequest(final String commandClassName, final Bundle extras){
		return getRequest(commandClassName, extras, true, false);
	}

	/**
	 * Start request in service with CommandExecutorService class without parameters and avoidDuplication and no cancel previous the same request
	 *
	 * @param commandClassName - Class name of command
	 * @return request of id
	 */
	public long getRequest(final String commandClassName){
		return getRequest(commandClassName, Command.prepareParameters(), true, false);
	}

	/**
	 * Cancel all pending requests
	 */
	public void cancelAllRequests() {
		while (pendingOperations.size() > 0) {
			long requestId = pendingOperations.keyAt(0);
			Intent originalIntent = pendingOperations.valueAt(0);
			if (originalIntent != null) {
				pendingOperations.remove(requestId);
				for (WeakReference<OnServiceResultListener> ref : listeners) {
					OnServiceResultListener listener = ref.get();
					if (listener == null) {
						listeners.remove(ref);
					} else {
						listener.onServiceResult(requestId, originalIntent, Command.RESULT_CANCELED, null, DEFAULT_REQUEST_ID);
					}
				}
				@SuppressWarnings("unchecked")
				Class<? extends Service> cls = (Class<? extends Service>) originalIntent.getSerializableExtra(Command.EXTRA_EXECUTOR_CLASS);
				context.startService(prepareCancelIntent(requestId, cls));
			}
		}
	}
}