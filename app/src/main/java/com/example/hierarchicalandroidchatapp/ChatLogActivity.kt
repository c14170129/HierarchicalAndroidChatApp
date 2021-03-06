package com.example.hierarchicalandroidchatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {
    companion object {
        const val TAG = "ChatLog"
    }

    val adapterChatLog = GroupieAdapter()
    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        findViewById<RecyclerView>(R.id.chatLogRecView).adapter = adapterChatLog

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
//        val username = intent.getStringExtra((NewMessageActivity.USER_KEY))
        supportActionBar?.title = toUser?.username

//        setupDummyData()

        listenForMessages()

        findViewById<Button>(R.id.sendButton).setOnClickListener {
            Log.d(TAG, "Attempt to send message...")
            performSendMessage()
        }
    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
//        val ref = FirebaseDatabase.getInstance().getReference("/messages")
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessagesActivity.currentUser ?: return
                        adapterChatLog.add(ChatFromItem(chatMessage.text, currentUser))
                    } else {
                        adapterChatLog.add(ChatToItem(chatMessage.text, toUser!!))
                    }
                }

                chatLogRecView.scrollToPosition(adapterChatLog.itemCount - 1)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    class ChatMessage(
        val id: String,
        val text: String,
        val fromId: String,
        val toId: String,
        val timestamp: Long
    ) {
        constructor() : this("", "", "", "", -1)
    }

    private fun performSendMessage() {
        val textMessage = findViewById<EditText>(R.id.messageTextField).text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user?.uid

        if (fromId == null || toId == null) return

//        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val reference =
            FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toReference =
            FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()
        val chatMessage =
            ChatMessage(reference.key!!, textMessage, fromId, toId, System.currentTimeMillis())

        reference.setValue(chatMessage).addOnSuccessListener {
            findViewById<EditText>(R.id.messageTextField).text.clear()
            findViewById<RecyclerView>(R.id.chatLogRecView).scrollToPosition(adapterChatLog.itemCount - 1)
        }
        toReference.setValue(chatMessage)

        val latestMessagesRef =
            FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessagesRef.setValue(chatMessage)

        val latestMessagesToRef =
            FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessagesToRef.setValue(chatMessage)
    }

    private fun setupDummyData() {
        val adapter = GroupieAdapter()

//        adapter.add(ChatFromItem("WOWOWOWOW"))
//        adapter.add(ChatToItem("HAOHAOHAO\nHAOHAOHAO"))
//        adapter.add(ChatFromItem("WOWOWOWOW"))
//        adapter.add(ChatToItem("HAOHAOHAO\nHAOHAOHAO"))
//        adapter.add(ChatFromItem("WOWOWOWOW"))
//        adapter.add(ChatToItem("HAOHAOHAO\nHAOHAOHAO"))
//        adapter.add(ChatFromItem("WOWOWOWOW"))
//        adapter.add(ChatToItem("HAOHAOHAO\nHAOHAOHAO"))

        val recyclerViewChatLog = findViewById<RecyclerView>(R.id.chatLogRecView)
        recyclerViewChatLog.adapter = adapter
    }
}

class ChatFromItem(private val textChat: String, private val user: User) :
    Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.fromMessageTextView).text = textChat
        viewHolder.itemView.fromUsernameTextView.text = user.username

        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.profileFromImageView)
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(private val textChat: String, private val user: User) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.toMessageTextView).text = textChat
        viewHolder.itemView.toUsernameTextView.text = user.username

        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.profileToImageView)
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}