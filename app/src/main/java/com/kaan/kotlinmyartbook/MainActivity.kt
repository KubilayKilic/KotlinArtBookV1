package com.kaan.kotlinmyartbook

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val artNameList = ArrayList<String>()
        val artIdList = ArrayList<Int>()

        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, artNameList)
        listView.adapter = arrayAdapter

        try {

            val database = this.openOrCreateDatabase("Arts", Context.MODE_PRIVATE, null)

            val cursor = database.rawQuery("SELECT * FROM arts", null)
            val artNameIx = cursor.getColumnIndex("artname")
            val IdIx = cursor.getColumnIndex("id")

            while (cursor.moveToNext()) {
                artNameList.add(cursor.getString(artNameIx))
                artIdList.add(cursor.getInt(IdIx))
            }

            arrayAdapter.notifyDataSetChanged()

            cursor.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val intent = Intent(this, ArtActivity::class.java)
            intent.putExtra("info", "old")
            intent.putExtra("id", artIdList[position])
            startActivity(intent)
        }

    }


    //menüyü bağlama.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        //Inflater xml dosyasını kullanmak için
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.add_art, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //seçilenin ne olduğunu anlayıp işlem yaptırmak.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.add_art_item) {
            val intent = Intent(this, ArtActivity::class.java)
            intent.putExtra("info", "new")
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }



}