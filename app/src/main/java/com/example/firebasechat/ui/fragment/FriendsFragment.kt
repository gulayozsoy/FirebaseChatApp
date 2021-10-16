package com.example.firebasechat.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.firebasechat.R
import com.example.firebasechat.model.User
import com.example.firebasechat.ui.activity.ChatActivity
import com.example.firebasechat.ui.adapter.FriendsAdapter
import com.example.firebasechat.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_friends.*
import java.lang.Exception

class FriendsFragment : Fragment(), FriendsAdapter.OnFriendClickListener {

    //Kullandığımız veritabanından datayı okutmak için yine referans oluşturmalıyız ki db'ye erişelim

    private val mUserDatabase: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child(Constants.CHILD_USERS) }

    private lateinit var adapter: FriendsAdapter
    private var userList: ArrayList<User> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_friends, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        adapter = FriendsAdapter(activity!!, userList)
        friends_recyclerView.layoutManager = LinearLayoutManager(activity)
        //recyclerview'e adapter'ü tanımlarız
        friends_recyclerView.adapter = adapter

        //() içine eklenen metodlar sayesinde child'lerin durumlarını kontrol edebiliyoruz
        mUserDatabase.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            //The child_added event is typically used when retrieving a list of items from the database.
            //burada User class'ına datayı göndermek için kullanılır
            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                if(dataSnapshot.value!= null)  {   //tüm değerler de boş değilse
                    try {
                        //Firebase tarafındaki veri değerleri aşağıda getValue ile User class'ına gelir!
                        val model = dataSnapshot.getValue(User::class.java)
                        val friendKey = dataSnapshot.ref.key

                        if(!currentUserId?.equals(friendKey)!!) {  //currentuser'ın id'si= yani bizim id'miz, friendKey'den farklıysa, biz hariç, arkadaşlarınkini alır..
                            userList.add(model!!)
                            adapter.notifyItemInserted(userList.size-1)  //çekilen datayla, listeye yeni eklenen item'la adapter'ü bilgilendirir, o da ekrana yansıtır.
                        }
                    }catch (e: Exception) {
                        Log.e("onChildAdded", e.message)
                    }
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })

        adapter.setOnFriendClickListener(this)
    }

    override fun onFriendClick(user: User) {
        mUserDatabase.orderByChild(Constants.CHILD_NAME).equalTo(user.name)
            //addValueEventListener deseydik,db'yi sürekli dinlememiz gerekirdi, burda 1 kez dinleyecek
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }
                //onDataChange'de chatActivity'e geçiş gerçekleşecek
                //hangi user'a tıklandıysa key'ini almamız gerekir
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val clickedUserKey = dataSnapshot.children.iterator().next().ref.key
                    //tıklanan kullanıcının key id'sini çeker

                    val intent = Intent(activity, ChatActivity::class.java)
                    //chat activity'e kullanıcının name'ini ve key'ini göndereceğiz
                    intent.putExtra(Constants.EXTRA_NAME, user.name)
                    intent.putExtra(Constants.EXTRA_ID, clickedUserKey)
                    startActivity(intent)
                }

            })
    }
}
