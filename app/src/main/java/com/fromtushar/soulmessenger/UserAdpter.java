package com.fromtushar.soulmessenger;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdpter extends RecyclerView.Adapter<UserAdpter.viewholder> {
    Context context;
    ArrayList<Users> usersArrayList;

    public UserAdpter(Context context, ArrayList<Users> usersArrayList) {
        this.context = context;
        this.usersArrayList = usersArrayList;
    }

    // 🔹 YE METHOD SEARCH KE LIYE SABSE ZARURI HAI
    public void filterList(ArrayList<Users> filteredList) {
        this.usersArrayList = filteredList; // List ko naye data se badla
        notifyDataSetChanged(); // RecyclerView ko refresh kiya
    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewholder holder, int position) {
        Users users = usersArrayList.get(position);
        holder.username.setText(users.getUserName());
        holder.userstatus.setText(users.getStatus());

        if (users.getProfilepic() != null && !users.getProfilepic().isEmpty()) {
            Picasso.get().load(users.getProfilepic()).placeholder(R.drawable.avtar_dp).into(holder.userimg);
        } else {
            holder.userimg.setImageResource(R.drawable.avtar_dp);
        }

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, chatWin.class);
            intent.putExtra("nameee", users.getUserName());
            intent.putExtra("reciverImg", users.getProfilepic());
            intent.putExtra("uid", users.getUserId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        CircleImageView userimg;
        TextView username, userstatus;
        public viewholder(@NonNull View itemView) {
            super(itemView);
            userimg = itemView.findViewById(R.id.userimg);
            username = itemView.findViewById(R.id.username);
            userstatus = itemView.findViewById(R.id.userstatus);
        }
    }
}