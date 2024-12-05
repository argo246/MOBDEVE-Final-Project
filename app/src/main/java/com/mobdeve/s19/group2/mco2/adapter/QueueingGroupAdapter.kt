package com.mobdeve.s19.group2.mco2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s19.group2.mco2.R
import com.mobdeve.s19.group2.mco2.model.QueueingGroup

class QueueingGroupAdapter(
    private val groups: List<QueueingGroup>,
    private val onItemClicked: (QueueingGroup) -> Unit
) : RecyclerView.Adapter<QueueingGroupAdapter.QueueingGroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueingGroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_queueing_group, parent, false)
        return QueueingGroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: QueueingGroupViewHolder, position: Int) {
        val group = groups[position]
        holder.bind(group, onItemClicked)
    }

    override fun getItemCount(): Int = groups.size

    class QueueingGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val groupNameTextView: TextView = itemView.findViewById(R.id.textViewGroupName)

        fun bind(group: QueueingGroup, onItemClicked: (QueueingGroup) -> Unit) {
            groupNameTextView.text = group.groupName
            itemView.setOnClickListener {
                onItemClicked(group)
            }
        }
    }
}