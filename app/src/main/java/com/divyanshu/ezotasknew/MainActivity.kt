package com.divyanshu.ezotasknew

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: ItemAdapter
    private val itemList = mutableListOf<Item>()
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.progressBar)
        recyclerView = findViewById(R.id.recyclerView)

        adapter = ItemAdapter(itemList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        fetchDataFromApi()
    }

    private fun fetchDataFromApi() {
        val url = "https://db.ezobooks.in/kappa/image/task"
        progressBar.visibility = View.VISIBLE

        // Using Kotlin Coroutines to perform the network call on IO thread
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = makeNetworkCall(url)
                response?.let {
                    parseJsonData(it)
                }
            } catch (e: IOException) {
                // Handle network error
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@MainActivity, "Failed to fetch data from API", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun makeNetworkCall(url: String): String? {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        return response.body?.string()
    }

    private fun parseJsonData(jsonData: String) {
        try {
            val jsonObject = JSONObject(jsonData)
            val status = jsonObject.getString("status")
            if (status == "success") {
                val itemsArray = jsonObject.getJSONArray("items")
                val itemList = mutableListOf<Item>()

                for (i in 0 until itemsArray.length()) {
                    val itemObject = itemsArray.getJSONObject(i)
                    val itemName = itemObject.getString("itemName")
                    val itemBarcode = itemObject.getInt("itemBarcode")
                    val itemPrice = itemObject.getString("itemPrice")
                    val url = itemObject.getString("url")

                    val item = Item(itemName, itemPrice, url)
                    itemList.add(item)
                }

                runOnUiThread {
                    progressBar.visibility = View.GONE
                    recyclerView.adapter = ItemAdapter(itemList)
                }
            } else {
                // Handle error when the status is not "success"
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to fetch data from API",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}