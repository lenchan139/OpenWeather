package com.example.len.openweather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import android.support.v4.content.ContextCompat
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.content.Intent
import android.content.DialogInterface
import android.R.string.cancel
import android.location.Location
import android.location.LocationListener
import android.os.AsyncTask
import android.provider.Settings
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    var gpsPermission = false
    private fun isGpsAble(lm: LocationManager): Boolean {
        return if (lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) true else false
    }
    val bookmarkSQLiteHelper = BookmarkSQLiteHelper(this)
    private fun openGPS2() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivityForResult(intent, 0)
    }
    private fun requestLocationPermission() {
        // 如果裝置版本是6.0（包含）以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 取得授權狀態，參數是請求授權的名稱
            val hasPermission = checkSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION)

            // 如果未授權
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                // 請求授權
                //     第一個參數是請求授權的名稱
                //     第二個參數是請求代碼
                requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        100)
            } else {
                // 啟動地圖與定位元件

            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        lateinit var gps : LocationManager
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= 23) {
            // 如果裝置版本是6.0（包含）以上
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 取得授權狀態，參數是請求授權的名稱
                val hasPermission = checkSelfPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION)

                // 如果未授權
                if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                    // 請求授權
                    //     第一個參數是請求授權的名稱
                    //     第二個參數是請求代碼
                    requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            100)
                } else {
                    // 啟動地圖與定位元件

                    gps = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    if(isGpsAble(gps)) {
                        var listener : ImLocationListener = ImLocationListener(this)
                        gps.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,10.toFloat(),listener)
                        fab.hide()
                    }else{
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivityForResult(intent, 0)
                    }
                }
            }
        }
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {}
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    /*---------- Listener class to get coordinates ------------- */
     class ImLocationListener( activity:AppCompatActivity) : LocationListener  {
        val activity = activity
        override fun onLocationChanged(location: Location?) {
            Log.v("OpenWeatherCustomDebug","location change triggered: "+ location.toString())

            if (location != null) {
                val task = DownloadWeatherInfoTask(activity, location.latitude.toString(), location.longitude.toString())
                task.execute()
            }

        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            Log.v("OpenWeatherCustomDebug","status change triggered: provider["+ provider +"] go to status[" + status.toString() + "]")
        }

        override fun onProviderEnabled(provider: String?) {
            Log.v("OpenWeatherCustomDebug", "provider[" + provider + "] goes to on")

        }

        override fun onProviderDisabled(provider: String?) {
            Log.v("OpenWeatherCustomDebug", "provider[" + provider + "] goes to off")
        }
        class DownloadWeatherInfoTask(var activity: AppCompatActivity, var lat:String,var long:String) : AsyncTask<Void,Void,String>(){
            val OPENWEATHERMAP_API_KEY = "9563bb226146f857414e853a969b077b"
            var jsonObj : JSONObject? = null
            fun updateNavMenu(){
                val navigationView = activity.nav_view
                if (navigationView != null) {
                    val menu = navigationView.menu
                    val items = BookmarkSQLiteHelper(activity).getBookmarks()
                    val values : ArrayList<String> = ArrayList()
                    for(i in items){
                        values.add(i.name)
                    }
                      val adapter : ArrayAdapter<String> = ArrayAdapter(activity , android.R.layout.simple_list_item_1 ,values);

                    activity.nav_listview.adapter = adapter

                }
            }
            fun get_owm_url(api_key:String, lat:String, long:String) : String{
                val OPENWEATHERMAP_BASE_URL = "http://api.openweathermap.org/data/2.5/weather"
                return OPENWEATHERMAP_BASE_URL + "?appid="+api_key+"&lat="+lat+"&lon="+long
            }
            override fun onPreExecute() {
                super.onPreExecute()
            }

            override fun doInBackground(vararg params: Void?): String {
                if(lat.isNotEmpty() && long.isNotEmpty()) {
                    jsonObj = JSONObject(readUrl(get_owm_url(OPENWEATHERMAP_API_KEY,lat,long)))
                    Log.v("OpenWeatherCustomDebug", "JsonStringFromApi: " + jsonObj.toString())

                }
            return ""
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                if(jsonObj != null) {

                        val jsonObj = this.jsonObj!!
                        val weatherDescr = jsonObj["weather"] as JSONArray
                        val weatherMain = jsonObj["main"] as JSONObject
                        val wind = jsonObj["wind"] as JSONObject
                        val sysInfo = jsonObj["sys"] as JSONObject
                        //for print
                        var locationName = jsonObj["name"] as String
                        val windSpeed = wind["speed"] as Double
                        val descrMain = (weatherDescr[0] as JSONObject)["description"]
                        val temp = weatherMain["temp"]
                        if(locationName.isEmpty()){
                            locationName = "unknown"
                        }
                        activity.tfCountry.text = locationName
                        activity.fab.setOnClickListener(View.OnClickListener {
                            val bookmarkSQLiteHelper = BookmarkSQLiteHelper(activity)
                            bookmarkSQLiteHelper.addBookmark(activity.tfCountry.text as String, lat, long)
                            updateNavMenu()
                        })
                        activity.fab.show()
                        updateNavMenu()

                }

            }

            @Throws(Exception::class)
            private fun readUrl(urlString: String): String {
                return URL(urlString).readText()
            }
        }
    }

}
