package com.example.hierarchicalandroidchatapp

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_group_list.*
import kotlinx.android.synthetic.main.group_list_row.view.*

class GroupListActivity : AppCompatActivity() {

    companion object {
        val USER_KEY = "USER_KEY"
    }

    val groupListAdapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_list)

        supportActionBar?.title = "Group List"

        groupListRecView.addItemDecoration(
            DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL
            )
        )

        findViewById<FloatingActionButton>(R.id.addGroupFAB).setOnClickListener {
            val intent = Intent(this, addGroupDetailsActivity::class.java)
            startActivity(intent)
        }

//        setDummyItems()
        retrieveGroups()

        listenForGroupChanges()
    }

    private fun setDummyItems() {
        val adapter = GroupAdapter<GroupieViewHolder>()
        adapter.add(DummyItem())
        adapter.add(DummyItem())
        adapter.add(DummyItem())
        adapter.add(DummyItem())
        adapter.add(DummyItem())
        adapter.add(DummyItem())

        groupListRecView.adapter = adapter
    }

    private fun retrieveGroups() {
        val ref = FirebaseDatabase.getInstance().getReference("/groups/")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()

                snapshot.children.forEach {
                    val group = it.getValue(GroupInfo2::class.java)
                    if (group != null) {
                        adapter.add(GroupItem(group))
                    }
                }

                adapter.setOnItemClickListener { item, view ->
                    val intent = Intent(view.context, GroupChatLogActivity::class.java)
                    val groupInfo = item as GroupItem
                    intent.putExtra("CURRENT_GROUP_ID", item.currentGroupID)
                    startActivity(intent)
                }

                groupListRecView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun listenForGroupChanges() {

    }
}

class GroupItem(val group: GroupInfo2) : Item<GroupieViewHolder>() {
    var currentGroupID: String? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val groupListNameTextView: TextView = viewHolder.itemView.groupListNameTextField
        val groupDescriptionListTextView: TextView =
            viewHolder.itemView.groupListDescriptionTextField

        currentGroupID = group.groupID
        groupListNameTextView.text = group.groupTitle
        groupDescriptionListTextView.text = group.groupDescription
        Picasso.get().load(group.groupPictureUrl)
            .into(viewHolder.itemView.groupListProfileImageView)
    }

    override fun getLayout(): Int {
        return R.layout.group_list_row
    }
}

class DummyItem: Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

    }

    override fun getLayout(): Int {
        return R.layout.group_list_row
    }

}