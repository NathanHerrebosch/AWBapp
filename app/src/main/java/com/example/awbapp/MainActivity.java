package com.example.awbapp;


import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
import com.microsoft.windowsazure.mobileservices.table.query.QueryOperations;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;
import com.squareup.okhttp.OkHttpClient;

import static com.microsoft.windowsazure.mobileservices.table.query.QueryOperations.*;

public class MainActivity extends Activity {

    //Client reference
    private MobileServiceClient mDbClient;

    //Table used to store data locally sync with the mobile app backend.
    private MobileServiceSyncTable<MowerDataItem> mToDoTable;

    // Adapter to sync the items list with the view
    private ToDoItemAdapter mAdapter;

    //EditText containing the "New To Do" text
    private EditText mTextNewToDo;

    // Progress spinner to use for table operations
    private ProgressBar mProgressBar;

    /**
     * Initializes the activity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do);

        mProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);

        // Initialize the progress bar
        mProgressBar.setVisibility(ProgressBar.GONE);

        try {
            // Create the client instance, using the provided mobile app URL.
            mDbClient = new MobileServiceClient(
                    "https://awbapp.azurewebsites.net",
                    this).withFilter(new ProgressFilter());

            // Extend timeout from default of 10s to 20s
            mDbClient.setAndroidHttpClientFactory(new OkHttpClientFactory() {
                @Override
                public OkHttpClient createOkHttpClient() {
                    OkHttpClient client = new OkHttpClient();
                    client.setReadTimeout(20, TimeUnit.SECONDS);
                    client.setWriteTimeout(20, TimeUnit.SECONDS);
                    return client;
                }
            });

            // Offline sync table instance.
            mToDoTable = mDbClient.getSyncTable("MowerDataItem", MowerDataItem.class);

            //Init local storage
            initLocalStore().get();

            mTextNewToDo = (EditText) findViewById(R.id.textNewToDo);

            // Create an adapter to bind the items with the view
            mAdapter = new ToDoItemAdapter(this, R.layout.row_list_to_do);
            ListView listViewToDo = (ListView) findViewById(R.id.listViewToDo);
            listViewToDo.setAdapter(mAdapter);

            // Load the items from the mobile app backend.
            refreshItemsFromTable();

        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e){
            createAndShowDialog(e, "Error");
        }
    }

    /**
     * Initializes the activity menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    /**
     * Select an option from the menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            refreshItemsFromTable();
        }

        return true;
    }

    /**
     * Mark an item as completed
     */
    public void checkItem(final MowerDataItem item) {
        if (mDbClient == null) {
            return;
        }

        // Set the item as completed and update it in the table
        item.setComplete(true);

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    checkItemInTable(item);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (item.isComplete()) {
                                mAdapter.remove(item);
                            }
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        };

        runAsyncTask(task);

    }

    /**
     * Mark an item as completed in the Mobile Service Table
     */
    public void checkItemInTable(MowerDataItem item) throws ExecutionException, InterruptedException {
        mToDoTable.update(item).get();
    }

    /**
     * Add a new item to the table
     */
    public void addItem(View view) {
        if (mDbClient == null) {
            return;
        }

        // Create a new item
        final MowerDataItem item = new MowerDataItem();

        //parse the incoming data
        parseData(mTextNewToDo.getText().toString(), item);
        item.setComplete(false);

        // Insert the new item
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final MowerDataItem entity = addItemInTable(item);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!entity.isComplete()){
                                mAdapter.add(entity);
                            }
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        runAsyncTask(task);

        mTextNewToDo.setText("");
    }

    /**
     *  the arduino sends the data in the format:
     *  "data">timestamp>lat>lng>x>y>z>up>down>angel1>angle2>angle3>angle4>angle5>temp>ventilator
     *  this method splits the data and converts it to the correct format
     */
    private void parseData(String data, MowerDataItem item) {

        if(data.startsWith("data>")){
            try {
                String[] splitData = data.split(">");
                item.setTimestamp(splitData[1]);
                item.setmLat(Float.parseFloat(splitData[2]));
                item.setmLng(Float.parseFloat(splitData[3]));
                item.setmXaxis(Float.parseFloat(splitData[4]));
                item.setmYaxis(Float.parseFloat(splitData[5]));
                item.setmZaxis(Float.parseFloat(splitData[6]));
                item.setmUp("1".equals(splitData[7]));
                item.setmDown("1".equals(splitData[8]));
                item.setmAngle1(Float.parseFloat(splitData[9]));
                item.setmAngle2(Float.parseFloat(splitData[10]));
                item.setmAngle3(Float.parseFloat(splitData[11]));
                item.setmAngle4(Float.parseFloat(splitData[12]));
                item.setmAngle5(Float.parseFloat(splitData[13]));
                item.setmTemperature(Float.parseFloat(splitData[14]));
                item.setVentilator(Integer.parseInt(splitData[15]));
            }catch(NumberFormatException e){
                //when the data is not properly formatted, send it to the UI
                createAndShowDialogFromTask(e, "Parsing error: \n" + data);
                e.printStackTrace();
            }

        }
        else{ //if the incoming data is no mower data, we assume it is a message from the arduino to the driver and we print it on the screen
            createAndShowDialog(data, "Arduino: ");
        }
    }

    /**
     * Add an item to the Mobile Service Table
     */
    public MowerDataItem addItemInTable(MowerDataItem item) throws ExecutionException, InterruptedException {
        MowerDataItem entity = mToDoTable.insert(item).get();
        return entity;
    }

    /**
     * Refresh the list with the items in the Table
     */
    private void refreshItemsFromTable() {

        // Get the items that weren't marked as completed and add them in the
        // adapter

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {

                try {

                    final List<MowerDataItem> results = refreshItemsFromMobileServiceTableSyncTable();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.clear();

                            for (MowerDataItem item : results) {
                                mAdapter.add(item);
                            }
                        }
                    });
                } catch (final Exception e){
                    e.printStackTrace();
                }

                return null;
            }
        };

        runAsyncTask(task);
    }


    /**
     * Refresh the list with the items in the Mobile Service Sync Table
     */
    private List<MowerDataItem> refreshItemsFromMobileServiceTableSyncTable() throws ExecutionException, InterruptedException {
        //sync the data
        sync().get();
        Query query = QueryOperations.field("complete").
                eq(val(false));
        return mToDoTable.read(query).get();
    }

    /**
     * Initialize local storage
     * @throws MobileServiceLocalStoreException
     * @throws ExecutionException
     * @throws InterruptedException
     *
     * the tableDefinition here has to have the exact same key-value pairs as the columns are defined in MowerDataItem.java
     */
    private AsyncTask<Void, Void, Void> initLocalStore() throws MobileServiceLocalStoreException, ExecutionException, InterruptedException {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    MobileServiceSyncContext syncContext = mDbClient.getSyncContext();

                    if (syncContext.isInitialized())
                        return null;

                    SQLiteLocalStore localStore = new SQLiteLocalStore(mDbClient.getContext(), "OfflineStore", null, 1);

                    Map<String, ColumnDataType> tableDefinition = new HashMap<String, ColumnDataType>();
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("timestamp", ColumnDataType.String);
                    tableDefinition.put("complete", ColumnDataType.Boolean);
                    tableDefinition.put("lat", ColumnDataType.Real);
                    tableDefinition.put("lng", ColumnDataType.Real);
                    tableDefinition.put("x_axis", ColumnDataType.Real);
                    tableDefinition.put("y_axis", ColumnDataType.Real);
                    tableDefinition.put("z_axis", ColumnDataType.Real);
                    tableDefinition.put("up", ColumnDataType.Boolean);
                    tableDefinition.put("down", ColumnDataType.Boolean);
                    tableDefinition.put("angle1", ColumnDataType.Real);
                    tableDefinition.put("angle2", ColumnDataType.Real);
                    tableDefinition.put("angle3", ColumnDataType.Real);
                    tableDefinition.put("angle4", ColumnDataType.Real);
                    tableDefinition.put("angle5", ColumnDataType.Real);
                    tableDefinition.put("temperature", ColumnDataType.Real);
                    tableDefinition.put("ventilator", ColumnDataType.Integer);

                    localStore.defineTable("MowerDataItem", tableDefinition);

                    SimpleSyncHandler handler = new SimpleSyncHandler();

                    syncContext.initialize(localStore, handler).get();

                } catch (final Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        };

        return runAsyncTask(task);
    }

    /**
     * Sync the current context and the Mobile Service Sync Table
     */
    private AsyncTask<Void, Void, Void> sync() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    MobileServiceSyncContext syncContext = mDbClient.getSyncContext();
                    syncContext.push().get();
                    mToDoTable.pull(null).get();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        return runAsyncTask(task);
    }


    /**
     * Creates a new thread to create a dialog and show it
     */
    private void createAndShowDialogFromTask(final Exception exception, String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(exception, "Error");
            }
        });
    }


    /**
     * Creates a dialog from an exception and shows it
     */
    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if(exception.getCause() != null){
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }

    /**
     * Creates a dialog from a string and shows it
     */
    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

    /**
     * Run an ASync task on the corresponding executor
     */
    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }

    private class ProgressFilter implements ServiceFilter {

        @Override
        public ListenableFuture<ServiceFilterResponse> handleRequest(ServiceFilterRequest request, NextServiceFilterCallback nextServiceFilterCallback) {

            final SettableFuture<ServiceFilterResponse> resultFuture = SettableFuture.create();


            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.VISIBLE);
                }
            });

            ListenableFuture<ServiceFilterResponse> future = nextServiceFilterCallback.onNext(request);

            Futures.addCallback(future, new FutureCallback<ServiceFilterResponse>() {
                @Override
                public void onFailure(Throwable e) {
                    resultFuture.setException(e);
                }

                @Override
                public void onSuccess(ServiceFilterResponse response) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.GONE);
                        }
                    });

                    resultFuture.set(response);
                }
            });

            return resultFuture;
        }
    }
}