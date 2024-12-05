package com.mobdeve.s19.group2.mco2.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s19.group2.mco2.DetailActivity
import com.mobdeve.s19.group2.mco2.R
import com.mobdeve.s19.group2.mco2.model.CourtName

class CourtAdapter(private val courtList: MutableList<CourtName>) : RecyclerView.Adapter<CourtAdapter.CourtViewHolder>() {

    class CourtViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val courtImage: ImageView = view.findViewById(R.id.courtImage)
        val courtTitle: TextView = view.findViewById(R.id.courtTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourtViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_courts, parent, false)
        return CourtViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourtViewHolder, position: Int) {
        val court = courtList[position]
        holder.courtImage.setImageResource(court.imageId)
        holder.courtTitle.text = court.courtName

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra("NAME", court.courtName)
            intent.putExtra("ADDRESS", court.address)
            intent.putExtra("BUSINESS_HOURS", court.businessHours)
            intent.putExtra("IMAGE", court.imageId)
            intent.putExtra("MAP", court.mapResId)
            context.startActivity(intent)
        }

    }

    fun updateData(newList: MutableList<CourtName>) {
        courtList.clear()
        courtList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = courtList.size
}