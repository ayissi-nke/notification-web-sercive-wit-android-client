package com.prgguru.androidrestfulwsexample ;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MessageFragment extends Fragment {
    public static final RetryPolicy policy = new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    ProgressDialog prgDialog;
    private String file ;
    TextView errorMsg;
    private Spinner To;
    private EditText Subject;
    private EditText Message;
    private Bitmap bitmap;
    private EditText Recepients;
    String[] names = {"500", "400", "300", "200", "staff"};
    ArrayAdapter<String> adapter;
    boolean[] checkedItems;
    Button selectPhoto;
    private Uri filePath;
    ArrayList<String> emailsList;
    ArrayList<Integer> mUserItems = new ArrayList<>();
    String selectedFilePath = null;

    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_message, container, false);

        To = (Spinner) v.findViewById(R.id.to);
        Subject = v.findViewById(R.id.subject);
        Message = v.findViewById(R.id.message);
        emailsList = new ArrayList<>();
        selectPhoto = v.findViewById(R.id.select_image);

        selectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });

        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, names);
        To.setAdapter(adapter);

        To.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String name = names[i];
                String url = "http://192.168.43.10:8080/fet/student/list?year=" + name;
                System.out.println(url);
                JsonArrayRequest js = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        checkedItems = new boolean[response.length()];

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject student = response.getJSONObject(i);
                                Message.append(student.getString("email")); //+"   "+student.getString("phone")+"\n");
                                emailsList.add(student.getString("email"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
                js.setRetryPolicy(policy);
                js.setShouldCache(false);
                Volley.newRequestQueue(getContext()).add(js);

            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        Button buttonSend = v.findViewById(R.id.button_send);
        prgDialog = new ProgressDialog(getContext());
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);


        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMail(v);
            }
        });


        return v;
    }

    private void showFileChooser(){
        Intent intent = new Intent();
        //sets the select file to all types of files
        intent.setType("*/*");
        //allows to select data and return it
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //starts new activity to select file and return data
        startActivityForResult(Intent.createChooser(intent, "Choose File to Upload.."),1);
    }




    private void sendMail(View view) {

        String to = To.getSelectedItem().toString();
        String sub = Subject.getText().toString();
        String message = Message.getText().toString();
        RequestParams params = new RequestParams();


        if (Utility.isNotNull(to) && Utility.isNotNull(sub) && Utility.isNotNull(message)) {

            System.out.println(to);
            params.put("TO", to);
            params.put("file",file);
            params.put("sub", sub);
            params.put("content", message);
            System.out.println("parameter well send ");


            invokeWS(params);
            Message.setText("");

        } else if (selectedFilePath != null) {
          //show progress bar
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //creating new thread to handle Http Operations
                   // uploadFile(selectedFilePath);
                }
            }).start();
        } else {
            Toast.makeText(getContext(), "Please fill the form or choose file, don't leave any field blank", Toast.LENGTH_LONG).show();
        }


    }


   /* @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                if (data == null) {
                    //no data present
                    return;
                }
                Uri selectedFileUri = data.getData();
                selectedFilePath = FilePath.getPath(this, selectedFileUri);
                if (selectedFilePath != null && !selectedFilePath.equals("")) {
                    Toast.makeText(getContext(), "File Selected", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Cannot upload file to server", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
*/

    public void invokeWS(RequestParams params) {

        prgDialog.show();

        AsyncHttpClient client = new AsyncHttpClient();

        client.get("http://192.168.43.103:8080/fet/email/doemail", params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                // Hide Progress Dialog
                prgDialog.hide();
                try {

                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if (obj.getBoolean("status")) {
                        Toast.makeText(getContext(), " successfully send mail!", Toast.LENGTH_LONG).show();
                        // Navigate to Home screen
                        /*navigatetoHomeActivity();*/
                    }
                    // Else display error message
                    else {
                        errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(getContext(), obj.getString("error_msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getContext(), "Error Occured  JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }
            }

            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                // Hide Progress Dialog
                prgDialog.hide();
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404,
                else {
                    Toast.makeText(getContext(), " Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }


        });
    }

}


    //android upload file to server
/*    public int uploadFile(final String selectedFilePath) {

        int serverResponseCode = 0;

        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = new File(selectedFilePath);


        String[] parts = selectedFilePath.split("/");
        final String fileName = parts[parts.length - 1];

    try {
            FileInputStream fileInputStream = new FileInputStream(selectedFile);
            URL url = new URL(SERVER_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);//Allow Inputs
            connection.setDoOutput(true);//Allow Outputs
            connection.setUseCaches(false);//Don't use a cached Copy
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("ENCTYPE", "multipart/form-data");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            connection.setRequestProperty("uploaded_file", selectedFilePath);*/

            //creating new dataoutputstream
          /*  dataOutputStream = new DataOutputStream(connection.getOutputStream());

            //writing bytes to data outputstream
            dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                    + selectedFilePath + "\"" + lineEnd);

            dataOutputStream.writeBytes(lineEnd);

            //returns no. of bytes present in fileInputStream
            bytesAvailable = fileInputStream.available();
            //selecting the buffer size as minimum of available bytes or 1 MB
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            //setting the buffer as byte array of size of bufferSize
            buffer = new byte[bufferSize];

            //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            //loop repeats till bytesRead = -1, i.e., no bytes are left to read
            while (bytesRead > 0) {
                //write the bytes read from inputstream
                dataOutputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            dataOutputStream.writeBytes(lineEnd);
            dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();

            //response code of 200 indicates the server status OK
            if (serverResponseCode == 200) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvFileName.setText("File Upload completed.\n\n You can see the uploaded file here: \n\n" + "http://coderefer.com/extras/uploads/" + fileName);
                    }
                });
            }

            //closing the input and output streams
            fileInputStream.close();
            dataOutputStream.flush();
            dataOutputStream.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(get, "File Not Found", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "URL error!", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();
        }
            return serverResponseCode;
        }

    }
}*/