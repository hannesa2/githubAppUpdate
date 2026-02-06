package info.hannes.github.sample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import info.hannes.github.AppUpdateHelper
import info.hannes.github.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))

        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.ic_menu)
            setDisplayHomeAsUpEnabled(true)
        }

        AppUpdateHelper.checkForNewVersion(
            this,
            BuildConfig.GIT_REPOSITORY,
            token = "abcdefgh"
        )

        binding.buttonGithub.setOnClickListener {
            AppUpdateHelper.checkWithDialog(
                this,
                BuildConfig.GIT_REPOSITORY,
                { msg -> Log.d("result", msg) },
                force = true // just to enable debugging, without you can only debug once a day
            )
        }
    }

}
