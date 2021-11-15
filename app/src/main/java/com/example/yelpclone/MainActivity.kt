package com.example.yelpclone

import android.app.SearchManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.CompoundButton
import android.widget.SearchView
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
private const val TAG = "MainActivity"
private const val BASE_URL = "https://api.yelp.com/v3/"
private const val API_KEY = "TLzTaracDmZjZhjVqF_VgslKq8ZyN0wEuF_vg-x-CSmimDhe9zOMwjH0e7eauxzTG98dNt8HT4wcv2NjI87dxj-DnO51vugJFST2f3n7rZ-5nhK_ueFRbvTrp_qRYXYx"

class MainActivity : AppCompatActivity() {
    private lateinit var searchTerm: String
    private lateinit var searchLocation: String
    private lateinit var restaurants: MutableList<YelpRestaurant>
    private lateinit var adapter: RestaurantsAdapter
    private lateinit var yelpService: YelpService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rvRestaurants = findViewById<RecyclerView>(R.id.rvRestaurants)
        searchTerm = "Avocado Toast"
        searchLocation = "New York"

        restaurants = mutableListOf<YelpRestaurant>()
        adapter = RestaurantsAdapter(this, restaurants)
        rvRestaurants.adapter = adapter
        rvRestaurants.layoutManager = LinearLayoutManager(this)

        val retrofit =
            Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
        yelpService = retrofit.create(YelpService::class.java)

        putData()
    }

    private fun putData(){
        restaurants.clear()
        yelpService.searchRestaurants("Bearer $API_KEY", searchTerm, searchLocation).enqueue(object : Callback<YelpSearchResult> {
            override fun onResponse(call: Call<YelpSearchResult>, response: Response<YelpSearchResult>) {
                Log.i(TAG, "onResponse $response")
                val body = response.body()
                if(body == null){
                    Log.w(TAG, "Did not receive valid response body from Yelp API... exiting")
                    return
                }
                restaurants.addAll(body.restaurants)
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {
                Log.i(TAG, "onFailure $t")
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = menu.findItem(R.id.search)
        val searchView = searchItem?.actionView as SearchView

        //searchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    if(query.contains(" : ")){
                        val keys = query.split(" : ")
                        searchTerm = keys[0]
                        searchLocation = keys[1]
                    }
                    else {
                        searchTerm = query
                    }
                    putData()
                }
                //Toast.makeText(this@MainActivity, searchTerm, Toast.LENGTH_LONG).show()
                searchView.clearFocus()
                searchView.setQuery("", false)
                searchItem.collapseActionView()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                //Toast.makeText(this@MainActivity, newText, Toast.LENGTH_SHORT).show()
                return false
            }
        })

        return true
    }

}