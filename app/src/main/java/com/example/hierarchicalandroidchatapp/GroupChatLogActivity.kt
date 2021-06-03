package com.example.hierarchicalandroidchatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupieAdapter
import kotlinx.android.synthetic.main.activity_group_chat_log.*

class GroupChatLogActivity : AppCompatActivity() {
    val adapterGroupChatLog = GroupieAdapter()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat_log)

        groupChatLogRecView.adapter = adapterGroupChatLog

        sendMessageGroupChatLog.setOnClickListener {
//            performSendGroupMessage()
        }
    }

    private fun performSendGroupMessage() {
        val textMessage = messageTextFieldGroupChatLog.text.toString()

        if (textMessage != "") {
            val groupID =
                intent.getStringExtra("CURRENT_GROUP_ID") // Need to get groupID somehow from the last page through parcelable
            val groupMessagesRef =
                FirebaseDatabase.getInstance().getReference("/group-messages/$groupID").push()
            groupMessagesRef.setValue(textMessage)
        } else {
            return
        }
    }

}