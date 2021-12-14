package com.llamasoft.envi.ui.simulator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.llamasoft.envi.R
import com.llamasoft.envi.databinding.ActivityMainBinding
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader

class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val navController by lazy {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navHostFragment.navController
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        navController.addOnDestinationChangedListener(this)
        setupActionBarWithNavController(navController, appBarConfiguration)
        configureOpenCV()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {

    }

    private fun configureOpenCV() {
        val loaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
            override fun onManagerConnected(status: Int) { if (status != SUCCESS) super.onManagerConnected(status) }
        }
        when (!OpenCVLoader.initDebug()) {
            true -> OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, loaderCallback)
            false -> loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }
}