package com.example.firebasechat.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.example.firebasechat.R
import com.example.firebasechat.ui.adapter.ViewPagerAdapter
import com.example.firebasechat.ui.fragment.ChatsFragment
import com.example.firebasechat.ui.fragment.FriendsFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    //çıkış için mAuth nesnesine ihtiyacımız olacak

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpUI()
        //Options menu tanımlanır
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

        //createoptions'dan sonraki 2.adım menu item'ları içindir:
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId) {
            R.id.action_logout -> {
                mAuth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            R.id.action_profile_page -> {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
        }
        return true
    }

    private fun setUpUI() {
        setSupportActionBar(main_toolbar)      //toolbar tanımlanır
        setupViewPager()
        main_tabs.setupWithViewPager(main_viewpager)
    }

    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.apply {
            addFragment(ChatsFragment(),"Mesajlar")
            addFragment(FriendsFragment(), "Arkadaşlar")
        }
        main_viewpager.adapter = adapter
    }
}
