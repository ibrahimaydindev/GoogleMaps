package com.example.googlemaps.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.googlemaps.R
import com.example.googlemaps.adapter.PlaceAdapter
import com.example.googlemaps.databinding.ActivityMainBinding
import com.example.googlemaps.model.Place
import com.example.googlemaps.database.PlaceDatabase
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private var mDisposable= CompositeDisposable()
    private lateinit var db : PlaceDatabase
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding=ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        db=Room.databaseBuilder(applicationContext, PlaceDatabase::class.java,"Places").build()
        val placeDao=db.placeDao()

        mDisposable.add(
            placeDao.getAll()
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse)
        )
    }
    private fun handleResponse(placeList:List<Place>){
        binding.recyclerView.layoutManager= LinearLayoutManager(this)
        val adapter= PlaceAdapter(placeList)
        binding.recyclerView.adapter=adapter
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.place_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.add_place){
            val intent= Intent(this, MapsActivity::class.java)
            intent.putExtra("info","new")
            startActivity(intent)

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {

        mDisposable.clear()
        super.onDestroy()
    }
}