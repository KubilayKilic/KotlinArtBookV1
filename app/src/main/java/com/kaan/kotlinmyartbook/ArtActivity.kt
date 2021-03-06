package com.kaan.kotlinmyartbook

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_art.*
import java.io.ByteArrayOutputStream
import java.lang.Exception

class ArtActivity : AppCompatActivity() {

    var selectedPicture : Uri? = null
    var selectedBitmap : Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_art)

        val intent = intent

        val info = intent.getStringExtra("info")

        if (info.equals("new")) {
            artText.setText("")
            artistText.setText("")
            yearText.setText("")
            button.visibility = View.VISIBLE

            val selectedImageBg = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.selectimage)
            imageView.setImageBitmap(selectedImageBg)

        } else {
            button.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id", 1)

            val database = this.openOrCreateDatabase("Arts", Context.MODE_PRIVATE, null)

            val cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedId.toString()))

            val artNameIx = cursor.getColumnIndex("artname")
            val artistNameIx = cursor.getColumnIndex("artistname")
            val yearIx = cursor.getColumnIndex("year")
            val imageIx = cursor.getColumnIndex("image")

            while (cursor.moveToNext()) {
                artText.setText(cursor.getString(artNameIx))
                artistText.setText(cursor.getString(artistNameIx))
                yearText.setText(cursor.getString(yearIx))

                val byteArray = cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                imageView.setImageBitmap(bitmap)
            }

            cursor.close()

        }

    }

    fun save(view: View) {

        val artName = artText.text.toString()
        val artistName = artistText.text.toString()
        val year = yearText.text.toString()

        if (selectedBitmap != null) {

            val smallBitmap = makeSmallerBitmap(selectedBitmap!!, 300)
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteArray = outputStream.toByteArray()

            try {

                val database = this.openOrCreateDatabase("Arts", Context.MODE_PRIVATE, null)
                database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artname VARCHAR, artistname VARCHAR, year VARCHAR, image BLOB)")

                val sqlString =
                    "INSERT INTO arts (artname, artistname, year, image) VALUES (?,?,?,?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1, artName)
                statement.bindString(2, artistName)
                statement.bindString(3, year)
                statement.bindBlob(4, byteArray)

                //verileri sqlite'a kaydet
                statement.execute()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            startActivity(intent)
            //finish()

        }
    }

    //g??rsel k??????ltme.
    fun makeSmallerBitmap (image: Bitmap, maxSize : Int) : Bitmap {
        //geni??lik ve y??kseli??i al, oranlara g??re yatay m?? d????ey mi tespit et.
        var width = image.width
        var height = image.height

        val bitmapRatio : Double = width.toDouble() / height.toDouble()
        if (bitmapRatio > 1) {
            //g??rsel yatay ise;
            width = maxSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        } else {
            //g??rsel dikey ise
            height = maxSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun selectImage(view: View) {
        //1 kereye mahsus galeriye gitme izni istenir,
        //??nceden istenip istenmedi??i kontrol edilir.

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //e??er izin verilmediyse, izin iste
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        } else {
            //izin verdiyse, galeriye git
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intentToGallery, 2)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == 1) {

            //granted results bo?? de??ilse ve izin verildiyse;
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intentToGallery, 2)
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //galeriye gitti, geri geldi ve resim se??tiyse
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {

            selectedPicture = data.data

            try {

                if (selectedPicture != null) {

                    if (Build.VERSION.SDK_INT >= 28) {
                        val source =
                            ImageDecoder.createSource(this.contentResolver, selectedPicture!!)
                        selectedBitmap = ImageDecoder.decodeBitmap(source)
                        imageView.setImageBitmap(selectedBitmap)

                    } else {
                        selectedBitmap =
                            MediaStore.Images.Media.getBitmap(this.contentResolver, selectedPicture)
                        imageView.setImageBitmap(selectedBitmap)
                    }
                }
            } catch (e: Exception) {

            }

        }

        super.onActivityResult(requestCode, resultCode, data)
    }

}