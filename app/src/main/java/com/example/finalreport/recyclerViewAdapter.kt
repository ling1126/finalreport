package com.example.finalreport

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView

class recyclerViewAdapter(val mList:ArrayList<oneItem105>)
    :RecyclerView.Adapter<recyclerViewAdapter.ViewHolder>() {
    class ViewHolder(ItemView: View):RecyclerView.ViewHolder(ItemView) {

        val j0name: TextView = itemView.findViewById(R.id.jname)
        val j0address: TextView = itemView.findViewById(R.id.jaddress)
        val j0tel: TextView = itemView.findViewById(R.id.jtel)





    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview,parent,false)
        return  ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return  mList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val myoneItem105 = mList[position]
        holder.j0name.text = myoneItem105.name
        holder.j0address.text = myoneItem105.address
        holder.j0tel.text = myoneItem105.tel
    }
}