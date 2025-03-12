package com.example.mobile_app;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private UserApiService userApiService;
    private Button buttonGetUsers;
    private Button buttonAddUser;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Retrofit service
        userApiService = RetrofitClient.getInstance().getUserApiService();

        // Initialize UI components
        buttonGetUsers = findViewById(R.id.button);
        buttonAddUser = findViewById(R.id.button2);
        recyclerView = findViewById(R.id.recyclerView); // You'll need to add this to your layout
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userAdapter = new UserAdapter(userList, new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User user) {
                showUserActionDialog(user);
            }
        });

        recyclerView.setAdapter(userAdapter);

        buttonGetUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAllUsers();
            }
        });

        buttonAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddUserDialog();
            }
        });

        // Add a FloatingActionButton for creating new users
        FloatingActionButton fab = findViewById(R.id.fab); // You'll need to add this to your layout
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddUserDialog();
            }
        });
    }

    // Get all users (READ operation)
    private void getAllUsers() {
        Call<List<User>> call = userApiService.getUsers();
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userList.clear();
                    userList.addAll(response.body());
                    userAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to get users", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Add new user (CREATE operation)
    private void addUser(String name, String email, String password) {
        User newUser = new User(name, email, password);
        Call<User> call = userApiService.createUser(newUser);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User createdUser = response.body();
                    userList.add(createdUser);
                    userAdapter.notifyItemInserted(userList.size() - 1);
                    Toast.makeText(MainActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to create user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Update existing user (UPDATE operation)
    private void updateUser(int userId, String name, String email, String password) {
        User updatedUser = new User(userId, name, email, password);
        Call<User> call = userApiService.updateUser(userId, updatedUser);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User updated = response.body();
                    for (int i = 0; i < userList.size(); i++) {
                        if (userList.get(i).getId() == userId) {
                            userList.set(i, updated);
                            userAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                    Toast.makeText(MainActivity.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to update user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Delete user (DELETE operation)
    private void deleteUser(int userId) {
        Call<Void> call = userApiService.deleteUser(userId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    for (int i = 0; i < userList.size(); i++) {
                        if (userList.get(i).getId() == userId) {
                            userList.remove(i);
                            userAdapter.notifyItemRemoved(i);
                            break;
                        }
                    }
                    Toast.makeText(MainActivity.this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to delete user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Dialog for adding a new user
    private void showAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New User");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_user_form, null);
        final EditText nameInput = viewInflated.findViewById(R.id.edit_name);
        final EditText emailInput = viewInflated.findViewById(R.id.edit_email);
        final EditText passwordInput = viewInflated.findViewById(R.id.edit_password);

        builder.setView(viewInflated);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameInput.getText().toString();
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();

                if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                    addUser(name, email, password);
                } else {
                    Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    // Dialog for actions on existing user (Update/Delete)
    private void showUserActionDialog(final User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("User Actions");

        String[] options = {"Update User", "Delete User"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Update User
                        showUpdateUserDialog(user);
                        break;
                    case 1: // Delete User
                        showDeleteConfirmationDialog(user.getId());
                        break;
                }
            }
        });

        builder.show();
    }

    // Dialog for updating an existing user
    private void showUpdateUserDialog(final User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update User");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_user_form, null);
        final EditText nameInput = viewInflated.findViewById(R.id.edit_name);
        final EditText emailInput = viewInflated.findViewById(R.id.edit_email);
        final EditText passwordInput = viewInflated.findViewById(R.id.edit_password);

        // Pre-fill existing data
        nameInput.setText(user.getName());
        emailInput.setText(user.getEmail());
        passwordInput.setText(user.getPassword());

        builder.setView(viewInflated);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameInput.getText().toString();
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();

                if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                    updateUser(user.getId(), name, email, password);
                } else {
                    Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    // Confirmation dialog for deleting a user
    private void showDeleteConfirmationDialog(final int userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this user?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteUser(userId);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}