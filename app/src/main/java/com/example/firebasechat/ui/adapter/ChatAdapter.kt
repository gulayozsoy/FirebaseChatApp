package com.example.firebasechat.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasechat.R
import com.example.firebasechat.model.Message
import com.example.firebasechat.util.TimeUtil
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.item_message_me.view.*

class ChatAdapter(var context: Context, var messageList: ArrayList<Message>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_CURRENT_USER = 1
        const val VIEW_TYPE_FRIEND = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //2 ayrı item'dan dolayı, viewType'a göre hangi item olduğu belirlenecek
        val inflater = LayoutInflater.from(parent.context)
        var viewHolder: RecyclerView.ViewHolder? = null

        when(viewType) {
            VIEW_TYPE_CURRENT_USER -> {
                val view = inflater.inflate(R.layout.item_message_me, parent, false)
                viewHolder = CurrentUserViewHolder(view)
            }
            VIEW_TYPE_FRIEND-> {
                val view = inflater.inflate(R.layout.item_message_friend, parent, false)
                viewHolder = FriendViewHolder(view)
            }
        }
        return viewHolder!!
    }

    override fun getItemCount(): Int = messageList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //2 ayrı durum olduğu için aşağıda bununla aynı formatta 2 ayrı method yaratırız
        if(messageList[position].from == FirebaseAuth.getInstance().currentUser?.uid) {
            //o anki giriş yaptığımız kullanıcı ise,currentUser biz demek, yani bizsek!

            prepareCurrentUserViewHolder(holder as CurrentUserViewHolder, position)
        } else {
            prepareFriendViewHolder(holder as FriendViewHolder, position)
        }
    }

    //neye göre ViewType'a karar vereceğini bu method belirler.Biz manuel ekledik
    override fun getItemViewType(position: Int): Int {
        return if(messageList[position].from == FirebaseAuth.getInstance().currentUser?.uid) {
            VIEW_TYPE_CURRENT_USER
        } else VIEW_TYPE_FRIEND
    }

    //bu metod da FriendFragment'ta tanımlanan add metodunun adapter'da tanımlanmış halidir
    fun add(message: Message) {
        messageList.add(message)
        notifyItemInserted(messageList.size-1)
    }

    private fun prepareCurrentUserViewHolder(holder: CurrentUserViewHolder, position: Int) {
        holder.userMessage.text = messageList[position].message

        //time, timestamp formatında db'den geleceği için onu stringe ve time görünümüne değiştirmeliyiz:
        holder.userTimeStamp.text = TimeUtil.timeStamptoDate(messageList[position].time)
    }

    private fun prepareFriendViewHolder(holder: FriendViewHolder, position: Int) {
        holder.userMessage.text = messageList[position].message
        holder.userTimeStamp.text = TimeUtil.timeStamptoDate(messageList[position].time)
    }


    //2 tane item tasarımı olduğu için 2 ayrı ViewHolder class'I tanımlanır!
    class CurrentUserViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val userMessage = view.findViewById<TextView>(R.id.item_message_me_messageText)
        val userTimeStamp = view.findViewById<TextView>(R.id.item_message_me_timestamp)
    }

    class FriendViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val userMessage = view.findViewById<TextView>(R.id.item_message_friend_messageText)
        val userTimeStamp = view.findViewById<TextView>(R.id.item_message_friend_timestamp)
    }
}