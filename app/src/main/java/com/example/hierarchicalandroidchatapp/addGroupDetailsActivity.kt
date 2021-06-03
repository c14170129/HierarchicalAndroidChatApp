package com.example.hierarchicalandroidchatapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_add_group_details.*
import java.util.*

class addGroupDetailsActivity : AppCompatActivity() {
    lateinit var groupNameEditText: EditText
    lateinit var groupDescriptionEditText: EditText
    var userJoinedGroup: MutableList<String> = mutableListOf()
    var groupMembers: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_group_details)

        supportActionBar?.title = "Create New Group"

        groupNameEditText = findViewById(R.id.createGroupEditText)
        groupDescriptionEditText = findViewById(R.id.groupDescriptionEditText)

        groupCreateButton.setOnClickListener {
            uploadGroupProfileImage()
        }

        groupPhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    private fun uploadGroupProfileImage() {
        if (selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/group-profile-images/$filename")

        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                getJoinedGroups(it.toString())
            }
        }
    }

    private var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            groupPhotoImageView.setImageBitmap(bitmap)
            groupPhotoButton.alpha = 0f
        }
    }

    class GroupInfo(
        val groupID: String,
        val groupTitle: String,
        val groupDescription: String,
    ) {
        constructor() : this(groupID = "", groupTitle = "", groupDescription = "")
    }

    private fun getJoinedGroups(groupProfileUrl: String) {
        val currentUserID = FirebaseAuth.getInstance().uid ?: ""
        val userGroupRef =
            FirebaseDatabase.getInstance().getReference("users/$currentUserID/groups")
        val groupRef = FirebaseDatabase.getInstance().getReference("groups/").push()
        val tempGroupID = groupRef.key!!

        userGroupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEach { it ->
                        Log.d("This is inside of the joinedGroup", it.toString())
                        userJoinedGroup.add(it.value.toString())
                    }
                }
                userJoinedGroup.add(groupRef.key!!)
                val groupTitle = groupNameEditText.text.toString()
                val groupDescription = groupDescriptionEditText.text.toString()
                val createdGroup =
                    GroupInfo2(
                        tempGroupID,
                        groupTitle,
                        groupDescription,
                        groupProfileUrl,
                    )

                groupRef.setValue(createdGroup).addOnSuccessListener {
                    userGroupRef.setValue(userJoinedGroup).addOnSuccessListener {
                        initializeMembers(tempGroupID)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun initializeMembers(tempID: String) {
        val currentUserID = FirebaseAuth.getInstance().uid ?: ""
        val groupMemberReference =
            FirebaseDatabase.getInstance().getReference("groups/$tempID/members/")

        groupMemberReference.setValue(mutableListOf(currentUserID)).addOnSuccessListener {
            Toast.makeText(this, "Group Added and Default Member", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}

@Parcelize
class GroupInfo2(
    val groupID: String?,
    val groupTitle: String?,
    val groupDescription: String?,
    val groupPictureUrl: String?
) : Parcelable {
    constructor() : this("", "", "", "")
}