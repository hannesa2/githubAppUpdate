package info.hannes.github.sample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import info.hannes.github.AppUpdateHelper

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.ic_menu)
            setDisplayHomeAsUpEnabled(true)
        }

        AppUpdateHelper.checkForNewVersion(
            this,
            BuildConfig.GIT_REPOSITORY,
            { msg -> Log.d("result", msg) },
            force = true // just to enable debugging, without you can only debug once a day
        )
    }

}
