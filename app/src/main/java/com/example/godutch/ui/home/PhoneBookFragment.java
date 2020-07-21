package com.example.godutch.ui.home;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.godutch.Constants;
import com.example.godutch.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class PhoneBookFragment extends Fragment implements View.OnClickListener{
    protected static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    protected static final int PERMISSIONS_REQUEST_SEND_SMS = 2;
    protected static final int PERMISSIONS_CALL_PHONE = 3;
    protected static final int PERMISSIONS_INTERNET = 4;
    protected static final int PERMISSIONS_REQUEST_ALL = 5;
    private static String[] requiredPermissions = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.INTERNET
    };
    private PhoneBookViewModel phoneBookViewModel;
    private Animation fab_open, fab_close;
    private boolean isFabOpen = false;

    private PhoneBookAdapter adapter;
    private LinearLayoutManager layoutManager;

    private ListView listview;
    private ArrayAdapter searchAdapter;
    private SearchView searchView;
    private ArrayList<JsonData> backupList ;
    private Button btn;
    private OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8"); //json 파일 형식 예고함
    String myFBid = "empty"; // facebook id로 본인 자료 찾아서 가져옴
    protected FloatingActionButton options, numberAdd, upload_data;
    private EditText byname;

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.update){
            animate();
            UPLOAD();
            Toast workdone = Toast.makeText(this.getContext(), "Upload succeed", Toast.LENGTH_SHORT );
            workdone.show();
        }
        else if(id == R.id.add_number){
            animate();

        }
        else if(id== R.id.add_options){
            animate();

        }

    }

    private void UPLOAD() {
        try{
            String contact = phoneBookViewModel.getContacts().getValue().toString();
            String topass = "{ \"id\" : \"" + myFBid + "\", " + "\"contacts\" : " + contact + "}";
            Log.d("서버 전송", topass);
            RequestBody body = RequestBody.create(topass, JSON);
            Request request = new Request.Builder()
                    .url(String.format("%s/api/contacts/insert", Constants.SERVER_IP))
                    .post(body)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.d("서버 Fail", "실패");
                    call.cancel();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    final String jsonString = response.body().string();
                    Log.d("서버 respond", jsonString);
                }
            });

        }catch(NullPointerException e){
            Log.d("서버 전송 안함", e.toString());
        }
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        myFBid = getActivity().getIntent().getStringExtra("USER_ID");

        PhoneBookViewModelFactory factory = new PhoneBookViewModelFactory(this.getContext(), getActivity());
        phoneBookViewModel = ViewModelProviders.of(getActivity(), factory).get(PhoneBookViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        adapter = new PhoneBookAdapter(new ArrayList<JsonData>(), getContext());
        backupList = new ArrayList<>();
        // searchAdapter = new ArrayAdapter(root.getContext(), R.layout.fragment_phonebook);

        final Observer<ArrayList<JsonData>> contactObserver = new Observer<ArrayList<JsonData>>() {
            @Override
            public void onChanged(@Nullable final ArrayList<JsonData> newContacts) {
                adapter.updateItems(newContacts);
            }
        };


        RecyclerView recyclerView = root.findViewById(R.id.pb_recycler_view);
        recyclerView.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        initializeContacts();//data null이면 repo에서 inital메소드 호출. 아니면 updateitem 부름. DB에 데이터 있다면 불러와도 좋을 듯 함.
        requestRequiredPermissions();
        phoneBookViewModel.getContacts().observe(getViewLifecycleOwner(), contactObserver);

        options = root.findViewById(R.id.add_options);
        options.setOnClickListener(this);
        numberAdd = root.findViewById(R.id.add_number);
        numberAdd.setOnClickListener(this);
        upload_data = root.findViewById(R.id.update);
        upload_data.setOnClickListener(this);

        fab_open = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close);


        byname = root.findViewById(R.id.search_bar);
        View.OnKeyListener keyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == keyEvent.KEYCODE_ENTER){
                    String tofind = byname.getText().toString();
                    adapter.fillter(tofind, backupList);
                }

                return false;
            }
        };

        byname.setOnKeyListener(keyListener);

        return root;
    }

    private void animate() {
        if (isFabOpen) {
            numberAdd.startAnimation(fab_close);
            upload_data.startAnimation(fab_close);
            numberAdd.setVisibility(View.GONE);
            upload_data.setVisibility(View.GONE);
            numberAdd.setClickable(false);
            upload_data.setClickable(false);
            options.animate().rotation(0)
                    .setInterpolator(new LinearInterpolator())
                    .setDuration(300);
            isFabOpen = false;
        } else {
            numberAdd.startAnimation(fab_open);
            upload_data.startAnimation(fab_open);
            numberAdd.setVisibility(View.VISIBLE);
            upload_data.setVisibility(View.VISIBLE);
            numberAdd.setClickable(true);
            upload_data.setClickable(true);
            options.animate().rotation(135)
                    .setInterpolator(new LinearInterpolator())
                    .setDuration(300);
            isFabOpen = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS:
            case PERMISSIONS_REQUEST_ALL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    phoneBookViewModel.initializeContacts();
        }
    }

    private void initializeContacts() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            ArrayList<JsonData> data = phoneBookViewModel.getContacts().getValue();
            if (data == null)
                phoneBookViewModel.initializeContacts();
            else
                adapter.updateItems(phoneBookViewModel.getContacts().getValue());
                backupList.addAll(phoneBookViewModel.getContacts().getValue());
        }
    }

    private void requestRequiredPermissions() {
        boolean allGranted = true;
        for (String permission : PhoneBookFragment.requiredPermissions) {
            boolean granted = ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
            allGranted = allGranted && granted;
        }

        if (!allGranted)
            requestPermissions(requiredPermissions, PERMISSIONS_REQUEST_ALL);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        //adapter.notifyDataSetChanged();


        inflater.inflate(R.menu.top_menu, menu);
        MenuItem item = menu.findItem(R.id.profile_button);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);


        searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                // listview.setAdapter(searchAdapter);
                //adapter.fillter(query);
                Log.d("검색 입력: ",query);
                return false;

            }

            @Override
            public boolean onQueryTextChange(String newText) {


                Log.d("검색 : ",newText+backupList.size());

                if(newText.length() >0)
                {
                    adapter.fillter(newText,backupList); // 필터를 통해서 현재 보여주는 값 수정함.
                    //TODO: 현재 검색이 안될 경우 clear를 통해 초기화 됌. 최종으로 축소되었을때 backup
                    Log.d("Changed: ",newText+backupList.size());
                }
                else{

                }
                return true;
            }

        });

        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {

                //adapter.getListViewItemList().clear();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                adapter.getListViewItemList().clear();
                adapter.getListViewItemList().addAll(backupList);
                adapter.notifyDataSetChanged();

                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

}