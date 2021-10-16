package com.example.firebasechat.ui.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.app.ProgressDialog.show
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.firebasechat.R
import com.example.firebasechat.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.profile_page_layout.*
import java.io.FileNotFoundException

class ProfileActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mDatabase: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    private val mStorageRef: StorageReference by lazy { FirebaseStorage.getInstance().reference }
    private val mCurrentUser: FirebaseUser by lazy { mAuth.currentUser!! }

    //Kullanıcının tablodaki referansı oluşturulur
    private lateinit var mUserReference: DatabaseReference
    private lateinit var progressDialog: ProgressDialog


    private val GALLERY_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setSupportActionBar(profile_page_toolbar)

        setUserDatas()

        change_image_fab.setOnClickListener { changeImage() }
        status_text.setOnClickListener { showStatusChangeDialog() }

    }

    //Database'den verileri çekeceğiz ve activity'deki kullanıcı ismi, status ve profil resmini ekranda göstereceğiz

    private fun setUserDatas() {
        val userId = mCurrentUser.uid
        mUserReference = mDatabase.reference.child(Constants.CHILD_USERS).child(userId)  //firebase'den read data için, subUrl girildi

        //ilgili user'ın id'sini dinleyecek..
        mUserReference.addValueEventListener(object:ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }
            //veri alınabildiyse, başarılı!: aşağıdkai değişkenlere atanır
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val name = dataSnapshot.child(Constants.CHILD_NAME).value.toString()
                val profileImage = dataSnapshot.child(Constants.CHILD_PIMAGE).value.toString()
                val status = dataSnapshot.child(Constants.CHILD_STATUS).value.toString()

                profile_page_collapsing.title = name    //Hangi kullanıcı ise onun ismi collapsing toolbar titleda görünsün!
                profile_page_toolbar.title = name
                status_text.text = status

                //Firebase tablolarında no_image olarak görünüyorsa kullanıcının resmi yok demektir
                if(profileImage == "no_image") {
                    Glide.with(applicationContext).load(R.drawable.defaultuser).into(profile_page_pimage)  //yoksa defaultuser resmi gelsin
                } else {
                    Glide.with(applicationContext).load(profileImage).into(profile_page_pimage)   //varsa db'deki resmi gelsin, bunu yapan Glide..
                }
            }
        } )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //resultCode, verinin galeriden başarıyla alınıp alınmadığını içerir
        if(requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            progressDialog = ProgressDialog(this).apply {
                title = "Lütfen bekleyiniz..."
                setMessage("Profile resim yükleniyor")
                setCancelable(false)  //progress dialog ekranda kalsın, geriye basıldığında da
                show()
            }

            try {
                val imageUri = data?.data    //data, diğer activity'den yani galeriden gelen intent'tir.
                val imageStream = contentResolver.openInputStream(imageUri!!)   //uri formatına gelir
                val selectedImage = BitmapFactory.decodeStream(imageStream)         //bitmap formatına gelir
                profile_page_pimage.setImageBitmap(selectedImage)            //seçilen resim uyg'daki imageview'e yerleştirilir

                //resmi firebase storage'a yükleriz
                //resmin bulunduğu 2.child dosyasına resmin isim formatını da belirleriz. O anki zamanı ve uid'yi versin..
                //Özetle, Resmin tutulduğu folder ve path belirlenir.

                val filePath = mStorageRef.child(Constants.PPFOLDER).child("${System.currentTimeMillis()}-${mCurrentUser.uid}")

                //tanımlanan filePath'e resmin Uri'sini koyarız.
                filePath.putFile(imageUri!!).continueWithTask { task ->
                    if(!task.isSuccessful) task.exception?.let { throw it}      //başarısızsa hata gönder
                            filePath.downloadUrl                                //başarılı ise downloadUrl ile ilgili imageUrl indirilir

                }.addOnCompleteListener {
                    if(it.isSuccessful) {
                        val downloadUri = it.result    //tüm işlem başarılı ise değişkene downloadUrl'i ata, resim uri olarak storage'a kaydolur.
                        saveImageUrltoStorage(downloadUri.toString())   //url' i string'e dönüştürüp, database'e kaydederiz
                    } else {
                        Toast.makeText(this,"Resmi storage'a yüklerken hata oluştu",Toast.LENGTH_SHORT).show()
                        progressDialog.dismiss()   // progress dialoğu ekrandan kaldırır
                    }
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Toast.makeText(this,"Seçilen resmi uygulamaya yüklerken hata oluştu",Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this,"Galeriden resim seçmediniz",Toast.LENGTH_LONG).show()
        }
    }

    private fun saveImageUrltoStorage(url: String) {
        mDatabase.reference.child(Constants.CHILD_USERS).child(mCurrentUser.uid).child(Constants.CHILD_PIMAGE)
            .setValue(url).addOnCompleteListener {
                if (it.isComplete) {         //database'e resim string url olarak kaydoldu
                    progressDialog.dismiss()
                    Toast.makeText(this,"Resim Firebase'e yüklendi",Toast.LENGTH_LONG).show()
                }
            }
    }

    //floating action butona tıklandığında kullanıcının galerisi açılacak (Implicit intent)
    private fun changeImage() {
        val intent = Intent()
        intent.type = "image/*"  //type is the MIME type of the data
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent,"Profil resmi seçiniz"), GALLERY_REQUEST_CODE)

        //Galeri açılıp orada kalınmayacağından değeri geleceğinden startActivity yerine
        //startActivityForResult kullanılır! ancak bu method da onActivityResult ile birlikte kullanılmalıdr!
        //startAcForRes. ile gönderilen kod ve değer onActivityResult ile okunur.
    }

    private fun showStatusChangeDialog() {

        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView: View = layoutInflater.inflate(R.layout.custom_dialogu, null)
        dialogBuilder.setView(dialogView)

        val edt: EditText = dialogView.findViewById(R.id.dialog_status_text)
        dialogBuilder.setTitle("Durum değiştir")
        dialogBuilder.setPositiveButton("Uygula")  {  //setPosBut- yazılan dialoğun onay butonudur
            dialog, which ->
            val dialogStatusText = edt.text.toString() //girilen yazıyı alıyor

            setStatusText(dialogStatusText)
        }
        dialogBuilder.setNegativeButton("İptal") {    //setNegBut- yazılan dialoğun iptal butonudur
            dialog, which -> dialog.dismiss()
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    //Firebase database'deki veriyi değiştiririz.
    private fun setStatusText(status: String) {
        mDatabase.reference.child(Constants.CHILD_USERS).child(mCurrentUser.uid).child(Constants.CHILD_STATUS)
            .setValue(status).addOnCompleteListener {
                if(it.isComplete)  {
                    Toast.makeText(this,"Status değiştirildi",Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this,"Hata,Status değiştirilemedi",Toast.LENGTH_SHORT).show()
                }
            }
    }

}



