package com.example.godutch.ui.home;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.example.godutch.Constants;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ContactRepository {
    private Context context;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client = new OkHttpClient();

    public ContactRepository(Context context) {
        this.context = context;
    }

    private String fetchPhoneNumber(ContentResolver cr, String id) {
        Cursor phoneCursor = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                null,
                null
        );
        String number = "";

        if (phoneCursor.moveToFirst())
            number = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

        phoneCursor.close();
        return number;
    }

    private String fetchEmail(ContentResolver cr, String id) {
        Cursor emailCursor = cr.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + id,
                null,
                null
        );

        String email = "";
        if (emailCursor.moveToFirst())
            email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));

        emailCursor.close();
        return email;
    }

    private Uri fetchPhotoUri(ContentResolver cr, String id) {
        try {
            Cursor cursor = cr.query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.CONTACT_ID + " = " + id + " AND " + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'",
                    null,
                    null
            );
            if (cursor == null || !cursor.moveToFirst())
                return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id));
        return Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
    }

    public ArrayList<JsonData> getContactList(String FBid) {

        ArrayList<JsonData> contacts = new ArrayList<JsonData>();

        Log.d("검색", "내부에서 출력");
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cur == null || cur.getCount() == 0)
            return contacts;

        while (cur != null && cur.moveToNext()) {
            String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
            String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String number = fetchPhoneNumber(cr, id);
            String email = fetchEmail(cr, id);
            Uri photo = fetchPhotoUri(cr, id);

            contacts.add(new JsonData(name, number, email, photo));
        }

        if (cur != null)
            cur.close();

        return contacts;
    }


    public ArrayList<JsonData> getDBcontacts(String FBid){

        final ArrayList<JsonData> contacts = new ArrayList<JsonData>();
        Log.d("받은 아이디값", FBid);

        Log.d("서버에서 받은것", "getDB들어옴") ;
        Request request = new Request.Builder()
                .url(String.format("%s/api/contacts/"+FBid , Constants.SERVER_IP))
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String jsonString = response.body().string();
                Log.d("서버에서 받은것", jsonString);

                try {
                    JSONObject jsonObject = new JSONObject( jsonString );
                    JSONArray jsonArray = jsonObject.getJSONArray("contacts");
                    for(int i =0; i < jsonArray.length() ; i++){
                        JSONObject obj = jsonArray.getJSONObject(i);
                        String name = obj.getString("name");
                        String email = obj.getString("email");
                        String number = obj.getString("number");

                        contacts.add(new JsonData(name, number, email, null));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }


        });
        Log.d("서버 정보 추가한 갯수", Integer.toString(contacts.size()));
        return contacts;

    }

//
//    public ArrayList<JsonData> filter(String find, String FBid){ //aka 무식한 검색색
//        final ArrayList<JsonData> contacts = new ArrayList<JsonData>();
//        final String tofind = find.toLowerCase();
//        Request request = new Request.Builder()
//                .url(String.format("%s/api/contacts/"+FBid , Constants.SERVER_IP))
//                .get()
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                call.cancel();
//            }
//
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                final String jsonString = response.body().string();
//                Log.d("서버에서 검색할 것", jsonString);
//                if(jsonString.contains("502 Bad Gateway")) {
//                    //Toast badway = Toast.makeText(context, "서버 연결이 불안정 합니다.", Toast.LENGTH_SHORT);
//
//                    ContentResolver cr = context.getContentResolver();
//                    Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
//
//                    if (cur == null || cur.getCount() == 0)
//                        return;
//
//                    while (cur != null && cur.moveToNext()) {
//                        String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
//                        String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//                        String number = fetchPhoneNumber(cr, id);
//                        String email = fetchEmail(cr, id);
//                        Uri photo = fetchPhotoUri(cr, id);
//
//                        Boolean result = name.toLowerCase().contains(tofind);
//                        Log.d("내부 저장소 검색 결과", name+" contains "+tofind +" is "+String.valueOf(result));
//                        if (result)
//                            {contacts.add(new JsonData(name, number, email, photo));}
//                    }
//
//                    if (cur != null)
//                        cur.close();
//
//
//                    return;
//                } // 서버랑 연결이 안된다면 실행
//
//                try {
//                    JSONObject jsonObject = new JSONObject( jsonString );
//                    JSONArray jsonArray = jsonObject.getJSONArray("contacts");
//                    for(int i =0; i < jsonArray.length() ; i++){
//                        JSONObject obj = jsonArray.getJSONObject(i);
//                        String name = obj.getString("name");
//                        String email = obj.getString("email");
//                        String number = obj.getString("number");
//
//                        Boolean result = name.toLowerCase().contains(tofind);
//                        Log.d("검색 결과", name+" contains "+tofind +" is "+String.valueOf(result));
//
//                        if(result)
//                            {contacts.add(new JsonData(name, number, email, null));}
//                    }
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//
//            }
//
//
//        });
//        return contacts;
//    }

}