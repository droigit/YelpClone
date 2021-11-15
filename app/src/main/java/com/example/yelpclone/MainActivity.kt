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
private const val API_KEY = ""

class MainActivity : AppCompatActivity() {
    private lateinit var searchTerm: String
    private lateinit var searchLocation: String
    private lateinit var restaurants: MutableList<YelpRestaurant>
    private lateinit var adapter: RestaurantsAdapter
    private lateinit var yelpService: YelpService
    var desiredPrice = "No Preference"

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

    fun putData(){
        restaurants.clear()
        yelpService.searchRestaurants("Bearer $API_KEY", searchTerm, searchLocation).enqueue(object : Callback<YelpSearchResult> {
            override fun onResponse(call: Call<YelpSearchResult>, response: Response<YelpSearchResult>) {
                Log.i(TAG, "onResponse $response")
                val body = response.body()
                if(body == null){
                    Log.w(TAG, "Did not receive valid response body from Yelp API... exiting")
                    return
                }
                if(desiredPrice == "No Preference") restaurants.addAll(body.restaurants)
                else {
                    val restaurantsIterator = body.restaurants.iterator()
                    for (curRestaurant in restaurantsIterator) {
                        if (curRestaurant.price == desiredPrice) restaurants.add(curRestaurant)
                    }
                }
                if(restaurants.isEmpty()) Toast.makeText(this@MainActivity, "No results.", Toast.LENGTH_LONG).show()
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

        //filterButton
        val filterButton = menu.findItem(R.id.filterButton)

        filterButton.setOnMenuItemClickListener(object: MenuItem.OnMenuItemClickListener{
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                var dialog = FilterDialogFragment()
                dialog.show(supportFragmentManager, "filterDialog")
                return true
            }
        })

        //searchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = menu.findItem(R.id.search)
        val searchView = searchItem?.actionView as SearchView

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
