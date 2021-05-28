package com.example.hierarchicalandroidchatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_messages_row.view.*

class LatestMessagesActivity : AppCompatActivity() {
    companion object {
        var currentUser: User? = null
    }

    val latestMessageAdapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        latestMessagesRecView.adapter = latestMessageAdapter
        latestMessagesRecView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        latestMessageAdapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ChatLogActivity::class.java)
            val row = item as LatestMessageRow

            intent.putExtra(NewMessageActivity.USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }

//        setupDummyRows()
        ListenForLatestMessages()

        fetchCurrentUser()

        verifyUserIsLoggedIn()
    }

    class LatestMessageRow(val chatMessage: ChatLogActivity.ChatMessage) :
        Item<GroupieViewHolder>() {
        var chatPartnerUser: User? = null

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.latestMessagesText.text = chatMessage.text
            val chatPartnerId: String
            if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                chatPartnerId = chatMessage.toId
            } else {
                chatPartnerId = chatMessage.fromId
            }

            val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatPartnerUser = snapshot.getValue(User::class.java)
                    viewHolder.itemView.latestMessagesUsername.text = chatPartnerUser?.username

                    val latestMessageImageView = viewHolder.itemView.latestMessagesProfile
                    Picasso.get().load(chatPartnerUser?.profileImageUrl)
                        .into(latestMessageImageView)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }

        override fun getLayout(): Int {
            return R.layout.latest_messages_row
        }

    }

    val latestMessagesMap = HashMap<String, ChatLogActivity.ChatMessage>()

    private fun refreshLatestMessagesRecView() {
        latestMessageAdapter.clear()
        latestMessagesMap.values.forEach {
            latestMessageAdapter.add(LatestMessageRow(it))
        }
    }

    private fun ListenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val latestChatMessage =
                    snapshot.getValue(ChatLogActivity.ChatMessage::class.java) ?: return

                latestMessagesMap[snapshot.key!!] = latestChatMessage
                refreshLatestMessagesRecView()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val latestChatMessage =
                    snapshot.getValue(ChatLogActivity.ChatMessage::class.java) ?: return

                latestMessagesMap[snapshot.key!!] = latestChatMessage
                refreshLatestMessagesRecView()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun setupDummyRows() {
//        latestMessageAdapter.add(LatestMessageRow())
//        latestMessageAdapter.add(LatestMessageRow())
//        latestMessageAdapter.add(LatestMessageRow())
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}