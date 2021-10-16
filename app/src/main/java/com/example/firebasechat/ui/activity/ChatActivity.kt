package com.example.firebasechat.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasechat.R
import com.example.firebasechat.model.Message
import com.example.firebasechat.ui.adapter.ChatAdapter
import com.example.firebasechat.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : AppCompatActivity() {

    private lateinit var mChatUserId:String
    private lateinit var adapter: ChatAdapter
    private var messageList: ArrayList<Message> = arrayListOf()

    //mesajları firebase'den çekmek (read) ve göndermek için (write) gerekli
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mRef: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference }

    //Current user yani hesap sahibini tanımlamamız gerekli
    private val mCurrentUserId by lazy { mAuth.currentUser?.uid }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        //FriendsFragment'tan gelen intent verisini alır: (name ve mesajlaşılan diğer kullanıcının id'si)

        val chatUserName = intent.extras!!.getString(Constants.EXTRA_NAME)
        mChatUserId = intent.extras!!.getString(Constants.EXTRA_ID)!!

        setSupportActionBar(chat_toolbar)
        supportActionBar?.title = chatUserName

        adapter = ChatAdapter(this,messageList)

        //Listeye ekleme yaptığımızda eklenen mesaj sona eklensin (also'dan sonra)
        //yani mesajları bir yığın, stack halinde tutsun
        chat_recyclerView.layoutManager = LinearLayoutManager(this).also { it.stackFromEnd = true }
        chat_recyclerView.adapter = adapter

        //varolan mesajları çekeceğiz
        loadMessages()

        //mesaj göndereceğiz
        send_message_btn.setOnClickListener { sendMessage() }

    }

    private fun loadMessages() {
        //tüm kullanıcıların mesajlarından var olan kullanıcın id'si ve bunun üzerinden mesajlaşılan kullanıcını id'si seçilir
        //Aslında aşağıda bir path giriliyor, biz oluşturuyoruz!
        mRef.child(Constants.MESSAGES).child(mChatUserId!!).child(mChatUserId)
            .addChildEventListener(object : ChildEventListener {
                //mesaj her an gelebileceğinden sürekli dinleme yapar, addChildEventtList kullanılır

                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                }
                override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                }
                //firebase'den mesaj çekme:
                override fun onChildAdded(dataSnapShot: DataSnapshot, p1: String?) {
                    //firebase'den okudugumuz degerleri message class'ına aldık
                    val message = dataSnapShot.getValue(Message::class.java)
                    //daha sonra da aynı mesajı adapter'e göndereceğiz ki ekranda görünsün!
                    //adapter liste yapısı gibi davranır,
                    adapter.add(message!!)
                    chat_recyclerView.scrollToPosition(chat_recyclerView.adapter?.itemCount?.minus(1)!!)
                }

                override fun onChildRemoved(p0: DataSnapshot) {
                }
            })
    }

    private fun sendMessage() {

        val message = send_message_edt.text.toString()

        if(message.isNotEmpty()) {  //göndereceğimiz mesaj boş değilse
            //mesajlaşan ve mesajlaşılan kullanıcının referansı alınır, A ve B tarafları için dual bir id oluşturulur
            val currentUserRef = "messages/$mCurrentUserId/$mChatUserId"
            val chatUserRef = "messages/$mChatUserId/$mCurrentUserId"

            //push ile mesaj gönderilir, UL edilir
            val userMessageRef: DatabaseReference = mRef.child(Constants.MESSAGES).child(mCurrentUserId!!)
                .child(mChatUserId!!).push()

            //her gönderilen mesaj bir id ile yazılır, bunu .key metodu sağlar:
            val messageId = userMessageRef.key

            //veritabanında mesajı, zamanı ve kimden geldiğini HashMap kullanarak kaydederiz:
            //Aynı Register işlemindeki gibi!

            val messageMap: HashMap<String, Any> = HashMap()
            messageMap["message"] = message
            messageMap["time"] = System.currentTimeMillis().toString()
            messageMap["from"] = mCurrentUserId!!

            val messageUserMap = mutableMapOf<String, Any>()
            messageUserMap["$currentUserRef/$messageId"] = messageMap //bizim için kendi id'miz kullanılarak tutulur
            messageUserMap["$chatUserRef/$messageId"] = messageMap     //karşı taraf için onların id'si kullanılarak tutulur

            //mesaj gönderildikten sonra edit text boşaltılmalıdır!
            send_message_edt.setText("")
            mRef.updateChildren(messageUserMap)


        }

    }

}


