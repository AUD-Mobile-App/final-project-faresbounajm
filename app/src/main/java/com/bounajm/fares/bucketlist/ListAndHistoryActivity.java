package com.bounajm.fares.bucketlist;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import static com.bounajm.fares.bucketlist.LoginActivity.myRef;
import static com.bounajm.fares.bucketlist.LoginActivity.userID;

public class ListAndHistoryActivity extends AppCompatActivity {

    public static class ListItem{
        public String name;
        public String description;
        public Date dueDate;
        public boolean locationSet;
        public double latitude;
        public double longitude;
        public boolean completed;
        public boolean isOnline;
        public String dbKey;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.mymenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.logout){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ListAndHistoryActivity.this, LoginActivity.class));
            finish();
        }
        return true;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    current = true;
                    listViewCurrent.setVisibility(View.VISIBLE);
                    listViewHistory.setVisibility(View.INVISIBLE);
                    setTitle("Bucket List");
                    return true;
                case R.id.navigation_notifications:
                    current = false;
                    listViewCurrent.setVisibility(View.INVISIBLE);
                    listViewHistory.setVisibility(View.VISIBLE);
                    setTitle("Completed");
                    return true;
            }
            return false;
        }
    };

    final ArrayList<ListItem> todoListAll = new ArrayList<>();

    final ArrayList<ListItem> todoListCurrent = new ArrayList<>();
    final CustomAdapter listAdapterCurrent = new CustomAdapter(todoListCurrent);

    final ArrayList<ListItem> todoListHistory = new ArrayList<>();
    final CustomAdapter listAdapterHistory = new CustomAdapter(todoListHistory);

    public static boolean editMode = false;
    public static ListItem todoItem;

    ListView listViewCurrent;
    ListView listViewHistory;

    boolean current = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_and_history);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        listViewCurrent = (ListView) findViewById(R.id.ListViewCurrent);

        listViewHistory = (ListView) findViewById(R.id.ListViewHistory);


        listViewCurrent.setAdapter(listAdapterCurrent);
        listViewHistory.setAdapter(listAdapterHistory);

        listViewHistory.setVisibility(View.INVISIBLE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editMode = false;
                startActivity(new Intent(ListAndHistoryActivity.this, AddItemActivity.class));
            }
        });


        myRef.child(userID()).child("bucketItem").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                todoListAll.clear();

                for (DataSnapshot imageSnapshot: dataSnapshot.getChildren()) {

                    ListItem x = imageSnapshot.getValue(ListItem.class);
                    x.dbKey = imageSnapshot.getKey();

                    todoListAll.add(x);
                }

                split();

                listAdapterCurrent.notifyDataSetChanged();
                listAdapterHistory.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        listViewCurrent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                editMode = true;
                todoItem = todoListCurrent.get(position);
                startActivity(new Intent(ListAndHistoryActivity.this, AddItemActivity.class));

            }
        });

        listViewHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                editMode = true;
                todoItem = todoListHistory.get(position);
                startActivity(new Intent(ListAndHistoryActivity.this, AddItemActivity.class));
            }
        });


        listViewCurrent.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                if(current){
                    myRef.child(userID()).child("bucketItem").child(todoListCurrent.get(position).dbKey).removeValue();
                }else{
                    myRef.child(userID()).child("bucketItem").child(todoListHistory.get(position).dbKey).removeValue();
                }

                return true;
            }
        });

    }

    public void split(){
        todoListHistory.clear();
        todoListCurrent.clear();


        for (int i = 0; i < todoListAll.size(); i++) {
            if(todoListAll.get(i).completed){
                todoListHistory.add(todoListAll.get(i));
            }else{
                todoListCurrent.add(todoListAll.get(i));
            }
        }


        Collections.sort(todoListHistory, new Comparator<ListItem>() {
            public int compare(ListItem o1, ListItem o2) {
                if (o1.dueDate == null || o2.dueDate == null) return 0;
                return o1.dueDate.compareTo(o2.dueDate);
            }
        });


        Collections.sort(todoListCurrent, new Comparator<ListItem>() {
            public int compare(ListItem o1, ListItem o2) {
                if (o1.dueDate == null || o2.dueDate == null) return 0;
                return o1.dueDate.compareTo(o2.dueDate);
            }
        });

    }

    final DateFormat txtDate = new SimpleDateFormat("dd MMM, yyyy");

    public class CustomAdapter extends BaseAdapter {

        private ArrayList<ListItem> list = new ArrayList<>();

        public CustomAdapter(ArrayList<ListItem> l){
            list = l;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {

            view = getLayoutInflater().inflate(R.layout.customlist,null);
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
            TextView name = (TextView) view.findViewById(R.id.textView_name);
            TextView description = (TextView) view.findViewById(R.id.textView_description);
            TextView date = (TextView) view.findViewById(R.id.textView_date);
            ImageView offlineImg = (ImageView) view.findViewById(R.id.offline_iv);

            final CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkBox);

            checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    boolean newState = checkbox.isChecked();

                    if(current){
                        if(i < todoListCurrent.size() && newState){
                            todoListCurrent.get(i).completed = true;
                            myRef.child(userID()).child("bucketItem")
                                    .child(todoListCurrent.get(i).dbKey).setValue(todoListCurrent.get(i));
                        }
                    }else{
                        if(i < todoListHistory.size() && !newState) {
                            todoListHistory.get(i).completed = false;
                            myRef.child(userID()).child("bucketItem")
                                    .child(todoListHistory.get(i).dbKey).setValue(todoListHistory.get(i));
                        }
                    }
                }
            });

            if(!list.get(i).isOnline){

                Log.d("xxx", String.valueOf(i) + "offline");

                offlineImg.setVisibility(View.VISIBLE);

                offlineImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(ListAndHistoryActivity.this,
                                "Currently saved offline, please check internet connection.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

            }else{

                Log.d("xxx", String.valueOf(i) + "online");

                offlineImg.setVisibility(View.INVISIBLE);
            }

            name.setText(trim(list.get(i).name, 25));
            description.setText(trim(list.get(i).description, 25));
            date.setText(txtDate.format(list.get(i).dueDate));

            if(list.get(i).completed){
                checkBox.setChecked(true);
            }else{
                checkBox.setChecked(false);
            }

            return view;
        }

        public String trim(String s, int length){

            String str = s.substring(0, Math.min(s.length(), length));

            if(s.length() > length) str += "...";

            return str;
        }

    }

}
