package com.prgguru.androidrestfulwsexample ;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RetryPolicy;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;


public class SmsFragment extends Fragment {
    public static final RetryPolicy policy = new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    ProgressDialog prgDialog;
    // Error Msg TextView Object
    TextView errorMsg;
    private Spinner To;
    private EditText Subject;
    private EditText Message;
    private EditText Recepients ;
    String[] names = {"500","400","300","200","staff"} ;
    ArrayAdapter<String> adapter ;
    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_sms, container, false);

        To = (Spinner)v.findViewById(R.id.receiver);
        Message = v.findViewById(R.id.sms);

        adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_dropdown_item_1line,names);
        To.setAdapter(adapter);


        Button buttonSend = v.findViewById(R.id.sendsms);
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




    private void sendMail(View view) {

        String to = To.getSelectedItem().toString() ;
        //String sub = Subject.getText().toString();
        String message = Message.getText().toString();
        RequestParams params = new RequestParams();


        if(Utility.isNotNull(to)  && Utility.isNotNull(message)){

            System.out.println(to);
            params.put("TO", to);

           // params.put("sub", sub);
            params.put("content", message);



            invokeWS(params);



        } else{
            Toast.makeText(getContext(), "Please fill the form, don't leave any field blank", Toast.LENGTH_LONG).show();
        }









    }




    public void invokeWS(RequestParams params){

        prgDialog.show();

        AsyncHttpClient client = new AsyncHttpClient();

        client.get("http://192.168.43.103:8080/fet/sms/dosms",params , new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                // Hide Progress Dialog
                prgDialog.hide();
                try {

                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if(obj.getBoolean("status")){
                        Toast.makeText(getContext(), " successfully send mail!", Toast.LENGTH_LONG).show();
                        // Navigate to Home screen
                        /*navigatetoHomeActivity();*/
                    }
                    // Else display error message
                    else{
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
                if(statusCode == 404){
                    Toast.makeText(getContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if(statusCode == 500){
                    Toast.makeText(getContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else{
                    Toast.makeText(getContext(), " Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
