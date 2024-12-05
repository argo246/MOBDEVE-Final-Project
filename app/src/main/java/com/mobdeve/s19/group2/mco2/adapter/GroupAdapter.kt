package com.mobdeve.s19.group2.mco2.adapter

import android.content.Intent
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s19.group2.mco2.GroupsDetailActivity
import com.mobdeve.s19.group2.mco2.R
import com.mobdeve.s19.group2.mco2.model.QueueingGroup

class GroupAdapter(private var groupList: MutableList<QueueingGroup>) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val courtImage: ImageView = view.findViewById(R.id.courtImage)
        val groupName: TextView = view.findViewById(R.id.groupName)
        val courtName: TextView = view.findViewById(R.id.courtName)
        val queueMaster: TextView = view.findViewById(R.id.queueMaster)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_groups, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groupList[position]

        holder.courtImage.setImageResource(group.imageId)
        holder.groupName.text = group.groupName
        holder.courtName.text = group.courtName

        val queueMasterText = "Queue Master: ${group.queueMaster}"
        val spannableString = SpannableString(queueMasterText)
        val boldStart = queueMasterText.indexOf(group.queueMaster)
        val boldEnd = boldStart + group.queueMaster.length

        // Apply the bold style to the "${group.queueMaster}" part
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            boldStart,
            boldEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        holder.queueMaster.text = spannableString

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, GroupsDetailActivity::class.java)
            intent.putExtra("NAME", group.groupName)
            intent.putExtra("COURT_NAME", group.courtName)
            intent.putExtra("ADDRESS", group.address)
            intent.putExtra("QUEUE_MASTER", group.queueMaster)
            intent.putExtra("QUEUE_DATE_TIME", group.queueDateTime)
            intent.putExtra("IMAGE", group.imageId)
            intent.putExtra("MAP", group.mapResId)
            context.startActivity(intent)
        }
    }

    fun updateData(newList: List<QueueingGroup>) {
        groupList.clear()
        groupList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = groupList.size
}