package com.example.downloaddemo2nd;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dc.downloadmanager.DownloadManager;
import com.dc.downloadmanager.DownloadManagerConfig;
import com.dc.downloadmanager.LoadState;
import com.dc.downloadmanager.TransferTask;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity implements com.dc.downloadmanager.DownloadManager.DownloadUpdateListener {
    private ListView listView;
    com.dc.downloadmanager.DownloadManager downloadManager;
    protected Adapter adapter;
    private Button btnAddLink;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAddLink = findViewById(R.id.btnLink);
        listView = findViewById(R.id.listView);
        DownloadManagerConfig config = new DownloadManagerConfig()
                .setMaxTasksNumber(3)
                .setSingleTaskThreadNumber(3)
                .setSavePath("");

        com.dc.downloadmanager.DownloadManager.init(this.getApplicationContext(), config);
        downloadManager = DownloadManager.getInstance();
        downloadManager.setUpdateListener(this);
        setListView();
        verifyStoragePermissions(this);
        btnAddLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();


            }
        });

    }

    private void showDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_layout, null);

        final EditText url = (EditText) dialogView.findViewById(R.id.url);
        final EditText fileName = (EditText) dialogView.findViewById(R.id.fileName);
        dialog.setView(dialogView)
                // Add action buttons
                .setPositiveButton("confirm",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                String url1 = url.getText().toString().trim();
                                String nameFile = fileName.getText().toString().trim();
                                downloadManager.addTask(url1,nameFile);
                            }
                        }).setNegativeButton("Cancel", null);

        final Dialog dialog1 = dialog.show();

    }


    public void setListView(){
        adapter = new Adapter(this,downloadManager.getTaskList());
        listView.setAdapter(adapter);
    }


    @Override
    public void OnUIUpdate() {
        adapter.notifyDataSetChanged();
    }
    static class Adapter extends BaseAdapter
    {
        LinkedList<TransferTask> data;
        Context context;

        public Adapter(Context context, LinkedList<TransferTask> data)
        {
            this.data = data;
            this.context = context;
        }

        @Override
        public int getCount()
        {
            return data.size();
        }

        @Override
        public Object getItem(int position)
        {
            return data.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            if (convertView == null) {
                convertView = ((Activity) context).getLayoutInflater().inflate(R.layout.item_download, parent, false);
            }
            final TransferTask tf = data.get(position);
            ((TextView) convertView.findViewById(R.id.title)).setText(tf.getFileName());

            ((ProgressBar) convertView.findViewById(R.id.progressBar)).setProgress((int) (tf.getTaskSize() > 0 ? 100
                    * tf.getCompletedSize() / tf.getTaskSize() : 0));
            if (tf.getState() == LoadState.PREPARE) {
                (convertView.findViewById(R.id.operation)).setEnabled(false);
                ((Button) convertView.findViewById(R.id.operation)).setText("connecting");
            }
            if (tf.getState() == LoadState.PAUSE) {
                ((Button) convertView.findViewById(R.id.operation)).setText("start");
            }
            if (tf.getState() == LoadState.DOWNLOADING) {
                (convertView.findViewById(R.id.operation)).setEnabled(true);
                ((Button) convertView.findViewById(R.id.operation)).setText("pause");
            }
            (convertView.findViewById(R.id.operation)).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (tf.getState() == LoadState.DOWNLOADING)
                        DownloadManager.getInstance().pauseTask(position);
                    else if (tf.getState() == LoadState.PAUSE)
                        DownloadManager.getInstance().restartTask(position);
                }
            });
            return convertView;
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity)
    {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

}

