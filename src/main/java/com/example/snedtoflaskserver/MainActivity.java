package com.example.snedtoflaskserver;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    String selectedImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void ConnectToTheServer(View v)
    {
        EditText IPAddress = findViewById(R.id.IPAddress);
        String IPAddressToString = IPAddress.getText().toString();
        EditText IPPort = findViewById(R.id.IPPort);
        String IPPortToString = IPPort.getText().toString();                    //Converting addresses form the UI into usable strings

        String URLToPost = "http://"+IPAddressToString+":"+IPPortToString+"/connect";      //Creating the server URL

        String PostBodyText = "Hello";
        MediaType TypeOfMedia = MediaType.parse("text/plain; charset=utf-8");
        RequestBody BodyToPost = RequestBody.create(TypeOfMedia, PostBodyText); //Posting a message to the server

        EstablishConnection(URLToPost, BodyToPost);
    }

    void EstablishConnection(String URLToPost, RequestBody BodyToPost)
    {
        OkHttpClient Client = new OkHttpClient();

        Request request = new Request.Builder()                         //Building the request
                .url(URLToPost)
                .post(BodyToPost)
                .build();

        Client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                 call.cancel();

                 runOnUiThread(new Runnable() {                             //Setting up a failed connection message
                     @Override
                     public void run() {
                         TextView ResponseText = findViewById(R.id.response);
                         ResponseText.setText("Failed to connect. Check if the server is running or the IP address is correct!!!");
                     }
                 });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView ResponseText = findViewById(R.id.response);
                        try {
                            ResponseText.setText(response.body().string());     //Changing the text to the message from the server
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    public void UploadImage(View view)
    {
        EditText IPAddress = findViewById(R.id.IPAddress);
        String IPAddressToString = IPAddress.getText().toString();
        EditText IPPort = findViewById(R.id.IPPort);
        String IPPortToString = IPPort.getText().toString();                    //Converting addresses form the UI into usable strings

        String URLToPost = "http://"+IPAddressToString+":"+IPPortToString+"/upload";      //Creating the server URL

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath, options);       //Load selected image
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        RequestBody postBodyImage = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "UploadedImage.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                .build();

        TextView response = findViewById(R.id.response);
        response.setText("Uploading in progress...");

        postUploadRequest(URLToPost, postBodyImage);
    }

    private void postUploadRequest(String urlToPost, RequestBody postBody)
    {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(urlToPost)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView ResponseMessage = findViewById(R.id.response);
                        ResponseMessage.setText("Failed to connect. Check if the server is running or the IP address is correct!!!");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView ResponseMessage = findViewById(R.id.response);
                        try
                        {
                            ResponseMessage.setText(response.body().string());
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    public void SelectImage(View view)
    {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if(resCode == RESULT_OK && data != null) {
            Uri uri = data.getData();

            selectedImagePath = getPath(getApplicationContext(), uri);
            EditText imgPath = findViewById(R.id.PathToFile);
            imgPath.setText(selectedImagePath);
            Toast.makeText(getApplicationContext(), selectedImagePath, Toast.LENGTH_LONG).show();
        }
    }

    // Implementation of the getPath() method and all its requirements is taken from the StackOverflow Paul Burke's answer: https://stackoverflow.com/a/20559175/5426539
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}