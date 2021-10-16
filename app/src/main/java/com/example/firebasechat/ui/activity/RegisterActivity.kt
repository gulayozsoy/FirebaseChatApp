package com.example.firebasechat.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isGone
import com.example.firebasechat.R
import com.example.firebasechat.util.Constants
import com.example.firebasechat.util.gone
import com.example.firebasechat.util.visible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth by lazy {FirebaseAuth.getInstance()}  //Authentication nesnesi oluşturulur
    private val mDatabase: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }  //Database nesnesi oluşturulur

    private lateinit var mReference: DatabaseReference   //Tablonun geneline referans deriz
    private lateinit var mUserReference: DatabaseReference   //Tablonun her user için satırı

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_buton.setOnClickListener{     //girilen veriler değişkenlere atanır
            val email = register_email.text.toString().trim()
            val password = register_password.text.toString().trim()
            val name = register_name.text.toString().trim()

            //bu değerlerin boş olup olmadığına bakılır
            if(email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                if(password.length>=6) {
                    register_progressBar.visible()
                    registerUser(name,email, password)
                } else {
                    register_password.error = "Şifre en az 6 karakter olmalı"
                }
            } else {
                if(email.isEmpty()) register_email.error = "Email boş geçilemez"
                if(password.isEmpty()) register_password.error = "Şifre boş geçilemez"
                if(name.isEmpty()) register_name.error = "İsim boş geçilemez"
            }
        }

        //'Hesabınız varsa giriş yapın' butonu tıklandığında LoginActivity.kt açılır:

        register_login_button.setOnClickListener { startActivity(Intent(this, LoginActivity::class.java)) }
    }

    private fun registerUser(name: String, email: String, password: String)  {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if(!it.isSuccessful) {
                Toast.makeText(this, "Başarısız", Toast.LENGTH_SHORT).show()
                register_progressBar.gone()
            } else {
                val currentUser = mAuth.currentUser
                val userId = currentUser?.uid  //başarılı kayıtta User'a id verilir

                mReference = mDatabase.reference
                mUserReference = mReference.child(Constants.CHILD_USERS).child(userId!!)  //https://deneme-f09c6.firebaseio.com/users/1 url'ivfvnden user'a ulaşılıyordu

                val userMap = HashMap<String, String>()  //Hashmap<Key,Value> şeklindedir, farklı map'ler yani farklı key-value değerleri girilebilir.
                userMap["name"] = name                   //key=name olarak tanımlanır, value'sü registerUser(name,pswd)'deki name'dir.
                userMap["profile_image"] = "no_image"    //key=profile_image olur, value' sü "no image" dir.
                userMap["status"] = "Hey there. I'm using ChatApp"  //key=status olur, value'sü verilen cümledir..

                //mUserReference tablosuna userMap overwrite edilir: name,profile ve status kolonları ve değerleri eklenir.
                mUserReference.setValue(userMap).addOnCompleteListener {
                    Toast.makeText(this,"BAŞARILI", Toast.LENGTH_SHORT).show()
                    register_progressBar.gone()

                    //Registration tamamlandığı için MainActivity'e geçiş yapılır.
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)

                    //geriye basıldığında bir daha registeractivity gelmemesi için:
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    finish()  //Call this when your activity is done and should be closed.
                }
            }
        }
    }
}
