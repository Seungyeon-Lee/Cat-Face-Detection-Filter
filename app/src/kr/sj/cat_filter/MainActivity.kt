package kr.sj.cat_filter

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val galBtn = findViewById(R.id.iv_gallery) as ImageView
        galBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivity(intent)
        }

        val filterList = arrayListOf<FilterData>(
                FilterData(0,"필터1"),
                FilterData(1,"필터2"),
                FilterData(0,"필터3"),
                FilterData(0,"필터4"),
                FilterData(0,"필터5"),
                FilterData(0,"필터6"),
                FilterData(0,"필터7"),
                FilterData(0,"필터8")
        )

        rv_filterList.layoutManager = LinearLayoutManager(this, LinearLayout.HORIZONTAL, false)
    //    rv_filterList.setHasFixedSize(true)
        rv_filterList.adapter = FilterAdapter(filterList)




        val filterBtn = findViewById(R.id.iv_filter) as ImageView
        filterBtn.setOnClickListener {
            ll_filter.visibility = View.VISIBLE
        }
    }
}