package com.example.hierarchicalandroidchatapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text

class NewMessageActivity : AppCompatActivity() {
    lateinit var recyclerView_NewMessage : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title = "Select User"

//        val _adapter = GroupieAdapter()
//        _adapter.add(UserItem())
//        _adapter.add(UserItem())
//        _adapter.add(UserItem())
        recyclerView_NewMessage = findViewById<RecyclerView>(R.id.recyclerView_newMessage)
//        recyclerView_NewMessage.adapter = _adapter

        fetchUsers()
    }

    companion object {
        val USER_KEY = "USER_KEY"
    }

    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")

        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()

                snapshot.children.forEach {
                    Log.d("NewMessage", it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        adapter.add(UserItem(user))
                    }

                    adapter.setOnItemClickListener{ item, view ->

                        val userItem = item as UserItem
                        val intent = Intent(view.context, ChatLogActivity::class.java)
                        intent.putExtra(USER_KEY, item.user)
                        startActivity(intent)
                        finish()
                    }

                    recyclerView_NewMessage.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}

class UserItem(val user: User): Item<GroupieViewHolder>() {

    private lateinit var usernameTextView : TextView

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        usernameTextView = viewHolder.itemView.findViewById<TextView>(R.id.usernameTextViewNewMessage)
        usernameTextView.text = user.username

        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<CircleImageView>(R.id.imageView))
    }

    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }

}