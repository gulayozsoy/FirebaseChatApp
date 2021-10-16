package com.example.firebasechat.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.firebasechat.R
import com.example.firebasechat.util.gone
import com.example.firebasechat.util.visible
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

//RegisterActivity'e çok benzer, açıklamalar için ona bak..

class LoginActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_button.setOnClickListener{
            val email = login_email.text.toString().trim()
            val password = login_password.text.toString().trim()

            if(email.isNotEmpty() && password.isNotEmpty()) {
                login_progressBar.visible()
                login(email, password)
            } else {
                if(email.isEmpty())  login_email.error = "Email boş geçilemez"
                if(password.isEmpty()) login_password.error = "Şifre boş geçilemez"
            }
        }

        //'yeni hesap oluştur' tıklandığında RegisterActivity sayfası açılır:

        login_register_button.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
    }

    private fun login(email: String, password: String)  {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if(!it.isSuccessful) {
                Toast.makeText(this,"Başarısız", Toast.LENGTH_SHORT).show()
                login_progressBar.gone()
            } else {
                login_progressBar.visible()
                val intent= Intent(this, MainActivity::class.java)
                startActivity(intent)

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                finish()
            }
        }

    }
}
