    package com.example.app8.UIClass;
    import androidx.annotation.Nullable;
    import androidx.appcompat.app.ActionBarDrawerToggle;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.appcompat.widget.Toolbar;
    import androidx.core.view.GravityCompat;
    import androidx.drawerlayout.widget.DrawerLayout;

    import android.content.Context;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.widget.ExpandableListView;
    import android.content.Intent;
    import android.os.Bundle;
    import android.view.View;
    import android.widget.AdapterView;
    import android.widget.Button;
    import android.widget.ListView;
    import android.widget.TextView;
    import android.widget.Toast;

    import com.dvinfosys.model.HeaderModel;
    import com.dvinfosys.ui.NavigationListView;
    import com.example.app8.NavigationDrawer.Common;
    import com.example.app8.Login.LoginActivity;
    import com.example.app8.R;
    import com.example.app8.SQLServer.SQLConnection;
    import com.example.app8.NavigationDrawer.SettingsActivity;
    import com.example.app8.UIStudent_Admin.StudentListActivity;
    import com.google.android.material.navigation.NavigationView;

    import java.sql.Connection;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.SQLException;
    import java.util.ArrayList;

    public class ClassListActivity extends AppCompatActivity
            implements NavigationView.OnNavigationItemSelectedListener{
        private DrawerLayout drawer;
        private ActionBarDrawerToggle toggle;
        private ListView listView;
        private NavigationListView expandable_navigation;
        private Context context;
        private ArrayList<String> accountList;
        private ArrayList<Integer> backgroundList;
        private ArrayList<Integer> studentCountList;
        private CustomClassListAdapter adapter;
        private Connection connection;
        private static final int ADD_ACCOUNT_REQUEST = 1;
        private TextView role;
        private TextView adminName;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.class_list_activity);

            Toolbar toolbar = findViewById(R.id.toolbar_layout);
            setSupportActionBar(toolbar);

            context = ClassListActivity.this;


            drawer = findViewById(R.id.drawer_layout);
            listView = findViewById(R.id.listView);
            expandable_navigation = findViewById(R.id.expandable_navigation);
            NavigationView navigationView = findViewById(R.id.nav_view);
            View header= navigationView.getHeaderView(0);
            role= header.findViewById(R.id.role);
            role.setText(R.string.role_admin);
            Intent intent = getIntent();
            if (intent.hasExtra("username")) {
                String username = intent.getStringExtra("username");

                // Now, you can use 'username' to display it in your UI elements
                // For example, if you have a TextView with ID 'textViewUsername':
                adminName = header.findViewById(R.id.adminName);;
                adminName.setText(username.toUpperCase());
            }

            navigationView.setNavigationItemSelectedListener(this);

            toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();


            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Trong phương thức onItemClick
                    int classId = getClassIdForPosition(position);
                    if (classId != -1) {
                        Intent intent = new Intent(ClassListActivity.this, StudentListActivity.class);
                        intent.putExtra("ClassId", classId); // Truyền class ID
                        startActivityForResult(intent, ADD_ACCOUNT_REQUEST);
                    }
                }
            });

            expandable_navigation.init(this)
                    .addHeaderModel(new HeaderModel("Home"))
                    .addHeaderModel(new HeaderModel("Log out"))
                    .build()
                    .addOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                        @Override
                        public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                            expandable_navigation.setSelected(groupPosition);

                            //drawer.closeDrawer(GravityCompat.START);
                            if (id == 0) {
                                //Home Menu
                                Common.showToast(context, "Home Select");
                                Intent intent = new Intent(v.getContext(), ClassListActivity.class);
                                startActivity(intent);
                                drawer.closeDrawer(GravityCompat.START);
                            }
                             else if (id == 1) {
                                //Wishlist Menu
                                Common.showToast(context, "Log out Selected");
                                Intent intent = new Intent(v.getContext(), LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            return false;
                        }
                    });
            //listView.expandGroup(2);


            accountList = new ArrayList<>();
            backgroundList = new ArrayList<>();
            studentCountList = new ArrayList<>();

            adapter = new CustomClassListAdapter(this, accountList, backgroundList, studentCountList);
            listView.setAdapter(adapter);

            connection = SQLConnection.getConnection();

            if (connection != null) {
                loadAccountData();
            } else {
                Toast.makeText(this, "Không thể kết nối đến cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();
            }




            Button addAccountButton = findViewById(R.id.addAccountButton);
            addAccountButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ClassListActivity.this, ClassAddActivity.class);
                    startActivityForResult(intent, ADD_ACCOUNT_REQUEST);
                }
            });


        }
        @Override
        public void onBackPressed() {
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                Intent intent = new Intent(ClassListActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

        @SuppressWarnings("StatementWithEmptyBody")
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            // Handle navigation view item clicks here.
            int id = item.getItemId();

//            if(id == R.id.Home) {
//                Intent intent = new Intent(this, ClassListActivity.class);
//                startActivity(intent);
//            } else if (id == R.id.Recognize) {
//                Intent intent = new Intent(this, RecognizeActivity.class);
//                startActivity(intent);
//            } else if (id == R.id.Logout) {
//                Intent intent = new Intent(this, LoginActivity.class);
//                startActivity(intent);
//            }
       /* if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        private void loadAccountData() {
            accountList.clear();
            backgroundList.clear();
            studentCountList.clear();

            String query = "SELECT [id], [name_class], [name_subject], [background] FROM [PROJECT].[dbo].[CLASS]";

            try {
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String name_class = resultSet.getString("name_class");
                    String name_subject = resultSet.getString("name_subject");
                    int backgroundValue = resultSet.getInt("background");
                    int studentCount = getStudentCountForClass(resultSet.getInt("id"));

                    String accountInfo = name_subject + "\n" + name_class;
                    accountList.add(accountInfo);
                    backgroundList.add(backgroundValue);
                    studentCountList.add(studentCount);
                }
                resultSet.close();
                preparedStatement.close();
                adapter.notifyDataSetChanged();
            } catch (SQLException e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi khi lấy dữ liệu từ cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == ADD_ACCOUNT_REQUEST && resultCode == RESULT_OK) {
                loadAccountData();
            }
        }

        private int getStudentCountForClass(int classId) {
            String query = "SELECT COUNT(*) AS studentCount FROM STUDENT_LIST WHERE class_id = ?";

            try {
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, classId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getInt("studentCount");
                }
                resultSet.close();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        }
        private int getClassIdForPosition(int position) {
            if (connection != null && position >= 0 && position < accountList.size()) {
                String selectedSubject = accountList.get(position).split("\n")[0];
                String query = "SELECT [id] FROM [PROJECT].[dbo].[CLASS] WHERE [name_subject] = ?";
                try {
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, selectedSubject);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        return resultSet.getInt("id");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return -1; // Trả về -1 nếu không tìm thấy class ID
        }
        @Override
        protected void onResume() {
            super.onResume();
                loadAccountData();
        }
    }
