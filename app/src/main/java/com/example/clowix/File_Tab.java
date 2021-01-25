package com.example.clowix;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;



public class File_Tab extends Fragment implements Listener.CallBackFromListener {

    private static final String TAG = "File_Tab";
    private List<Cloud_Data_Information> data;
    private RecycleViewAdaptor recycleViewAdaptor;
    private final String user_profile_locker;
    private RelativeLayout relativeLayout;
    private final Handler handler=new Handler();
    private final SearchView searchView;
    private final int network_status;

    public File_Tab(SearchView searchView,int network_status,String user_profile_locker) {

        this.searchView=searchView;
        this.network_status=network_status;
        this.user_profile_locker=user_profile_locker;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_file__tab, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.data_holder);
         relativeLayout=view.findViewById(R.id.user_alert);
         recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
         relativeLayout.setVisibility(View.INVISIBLE);
         recyclerView.addOnItemTouchListener(new Listener(recyclerView,this,getContext()));

        data=new ArrayList<>();

          recycleViewAdaptor=new RecycleViewAdaptor(new ArrayList<>(),getContext(),user_profile_locker);

           recyclerView.setAdapter(recycleViewAdaptor);

           searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
               @Override
               public boolean onQueryTextSubmit(String s) {
                   return false;
               }

               @Override
               public boolean onQueryTextChange(String s) {

                   recycleViewAdaptor.getFilter().filter(s);
                   return false;
               }
           });


           if(network_status==Network_Status.TYPE_NOT_CONNECTED)
           {
               DialogShower dialogShower=new DialogShower(R.layout.internet_error,R.style.translate_animator,getContext());
               dialogShower.showDialog();
               relativeLayout.setVisibility(View.VISIBLE);
           }
           else
           {
               read_time_access();
           }
    }

    private  void read_time_access()
    {
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference(user_profile_locker);

        databaseReference.addChildEventListener(new ChildEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {


                 Cloud_Data_Information cloud_data_information=snapshot.getValue(Cloud_Data_Information.class);

                    data.add(cloud_data_information);

                    if(data.isEmpty())
                    {
                        relativeLayout.setVisibility(View.INVISIBLE);
                    }

                    RecycleViewAdaptor.unfilteredList=new ArrayList<>(data);

                   recycleViewAdaptor.setData(data);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Log.d(TAG, "onChildChanged: ");
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                Cloud_Data_Information cloud_data_information=snapshot.getValue(Cloud_Data_Information.class);

                data.remove(cloud_data_information);

                recycleViewAdaptor.setData(data);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d(TAG, "onChildMoved: ");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: ");
            }
        });
    }


    @Override
    public void simpleTab(int position) {

        recycleViewAdaptor.side_handler(position);
    }

    @Override
    public void longTab(int position) {

        Dialog long_press_content_shower=new Dialog(getContext(),R.style.translate_animator);
        long_press_content_shower.setContentView(R.layout.long_tab_shower_box);
        long_press_content_shower.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        long_press_content_shower.setCanceledOnTouchOutside(true);
        Window window=long_press_content_shower.getWindow();
        window.setGravity(Gravity.CENTER);
        long_press_content_shower.show();

        WebView webView=long_press_content_shower.findViewById(R.id.vedio_displayer);
        ImageView photo_shower=long_press_content_shower.findViewById(R.id.photo_shower);
        RelativeLayout inner_warning_text=long_press_content_shower.findViewById(R.id.warning_text);
        webView.setVisibility(View.VISIBLE);
        photo_shower.setVisibility(View.VISIBLE);
        inner_warning_text.setVisibility(View.VISIBLE);

        if(data.get(position).getTag().equals(MainFrame.File_Type.Image.toString()))
        {
            webView.setVisibility(View.INVISIBLE);
            photo_shower.setVisibility(View.VISIBLE);
            inner_warning_text.setVisibility(View.GONE);
            Picasso.with(getContext()).load(Uri.parse(data.get(position).getDownload_uri())).into(photo_shower);
        }
        else if(data.get(position).getTag().equals(MainFrame.File_Type.Audio.toString()) || data.get(position).getTag().equals(MainFrame.File_Type.Video.toString()))
        {
            photo_shower.setVisibility(View.INVISIBLE);
            webView.setVisibility(View.VISIBLE);
            inner_warning_text.setVisibility(View.VISIBLE);
            webView.loadUrl(data.get(position).getDownload_uri());
        }
        else
        {
            long_press_content_shower.dismiss();
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data.get(position).getDownload_uri()));
            startActivity(browserIntent);
        }
    }
}