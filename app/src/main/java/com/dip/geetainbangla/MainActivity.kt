package com.dip.geetainbangla

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dip.geetainbangla.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->

            val customSnackbarView = LayoutInflater.from(view.context)
                .inflate(R.layout.custom_snackbar, null)

            val textView = customSnackbarView.findViewById<TextView>(R.id.snackbar_text)


            // Set your formatted text
            val message = "<b>যেকোনো ত্রুটি বা মূল্যবান পরামর্শ জানাতে - </b><br><a href='mailto:dip06ece@gmail.com'>dip06ece@gmail.com</a>\n"
            textView.text = Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY)
            textView.movementMethod = LinkMovementMethod.getInstance()

            val snackbar = Snackbar.make(view, "", Snackbar.LENGTH_LONG)
            val snackbarView = snackbar.view
            snackbarView.setPadding(0, 0, 0, 0)
            (snackbarView as ViewGroup).removeAllViews()
            (snackbarView as ViewGroup).addView(customSnackbarView)

            snackbar.show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_introduction, R.id.nav_bookmarks, R.id.nav_chapter1,
                R.id.nav_chapter2, R.id.nav_chapter3, R.id.nav_chapter4,
                R.id.nav_chapter5, R.id.nav_chapter6, R.id.nav_chapter7,
                R.id.nav_chapter8, R.id.nav_chapter9, R.id.nav_chapter10,
                R.id.nav_chapter11, R.id.nav_chapter12, R.id.nav_chapter13,
                R.id.nav_chapter14, R.id.nav_chapter15, R.id.nav_chapter16,
                R.id.nav_chapter17, R.id.nav_chapter18
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun showSettingsPopup() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_settings, null)

        // ✅ Make hyperlinks clickable
        val textView2 = dialogView.findViewById<TextView>(R.id.textDialogMessage2)
        val creditHtml = getString(R.string.creditgoes_to_text)
        textView2.text = Html.fromHtml(creditHtml, Html.FROM_HTML_MODE_LEGACY)
        textView2.movementMethod = LinkMovementMethod.getInstance()

        val textView3 = dialogView.findViewById<TextView>(R.id.textDialogMessage3)
        val licenseHtml = getString(R.string.licensed_under_text)
        textView3.text = Html.fromHtml(licenseHtml, Html.FROM_HTML_MODE_LEGACY)
        textView3.movementMethod = LinkMovementMethod.getInstance()

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val buttonClose = dialogView.findViewById<Button>(R.id.buttonClose)
        buttonClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                showSettingsPopup()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}