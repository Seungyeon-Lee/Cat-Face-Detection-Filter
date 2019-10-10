package kr.sj.cat_filter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_filter.view.*

class FilterAdapter(val filterList:ArrayList<FilterData>) : RecyclerView.Adapter<FilterAdapter.ViewHolder>() {

    //http://blog.naver.com/PostView.nhn?blogId=cosmosjs&logNo=221205326784&categoryNo=87&parentCategoryNo=0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int ): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_filter, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return filterList.size
    }

    override fun onBindViewHolder(holder: FilterAdapter.ViewHolder, position: Int) {
        holder.bindItems(filterList[position])

    }


    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        fun bindItems(data : FilterData){
            itemView.tv_filterTitle.text = data.filter_title
            //itemView.imageView_photo.setImageBitmap(data.photo)

            itemView.setOnClickListener({ })
        }

    }


}