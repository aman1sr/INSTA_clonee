package com.example.instaclone.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instaclone.Fragment.ProfileFragment;
import com.example.instaclone.MainActivity;
import com.example.instaclone.R;
import com.example.instaclone.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUser;
    private boolean isFragment;

    private FirebaseUser firebaseUser;

    public UserAdapter(Context mContext, List<User> mUser, boolean isFragment, FirebaseUser firebaseUser) {
        this.mContext = mContext;
        this.mUser = mUser;
        this.isFragment = isFragment;
        this.firebaseUser = firebaseUser;
    }

    /* why attachToRoot: false  --   https://stackoverflow.com/questions/12567578/what-does-the-layoutinflater-attachtoroot-parameter-mean  */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);

        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final User user = mUser.get(position);

        holder.btnFollow.setVisibility(View.VISIBLE);
        holder.fullname.setText(user.getUsername());

        Picasso.get().load(user.getImageurl()).placeholder(R.mipmap.ic_launcher).into(holder.imageProfile);

        isFollowed(user.getId(), holder.btnFollow);

        if (user.getId().equals(firebaseUser.getUid())) {
            holder.btnFollow.setVisibility(View.GONE);
        }

        holder.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (holder.btnFollow.getText().toString().equals("follow")) {

                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(firebaseUser.getUid()).child("followers").child(firebaseUser.getUid()).setValue(true);

                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(user.getId()).child("followers").child(firebaseUser.getUid()).setValue(true);

                    addNotification(user.getId());

                } else {

                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(firebaseUser.getUid()).child("following").child(user.getId()).removeValue();

                    FirebaseDatabase.getInstance().getReference().child("Follow").
                            child(user.getId()).child("followers").child(firebaseUser.getUid()).removeValue();


                }

            }
        });

// ok ok

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFragment) {
                    mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit().putString("profileId", user.getId()).apply();

                    ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();

                } else {
                    Intent intent = new Intent(mContext, MainActivity.class);
                    intent.putExtra("publisherId", user.getId());
                    mContext.startActivity(intent);
                }
            }
        });



    }

    private void addNotification(String id) {
        HashMap<String, Object> map = new HashMap<>();

        map.put("userid", id);
        map.put("text", "started following you");
        map.put("postid", "");
        map.put("isPost", false);

        FirebaseDatabase.getInstance().getReference().child("Notifications").child(firebaseUser.getUid()).push().setValue(map);

    }

    private void isFollowed(String id, Button btnFollow) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid());
         reference.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 if (snapshot.child(id).exists()) {
                     btnFollow.setText("following");
                 } else {
                     btnFollow.setText("follow");
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }
         });

    }

    @Override
    public int getItemCount() {
        return mUser.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView  imageProfile;
        public TextView username,fullname;
        public Button  btnFollow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageProfile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            fullname = itemView.findViewById(R.id.fullname);
            btnFollow = itemView.findViewById(R.id.btn_follow);

        }
    }

}
