package com.example.treespotter_firebase

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// treeHeartListener operates as a callback function that is called when the checkbox is interacted with
class TreeRecyclerViewAdapter(var trees: List<Tree>, val treeHeartListener: (Tree, Boolean) -> Unit):
    RecyclerView.Adapter<TreeRecyclerViewAdapter.ViewHolder>()
{

    inner class ViewHolder(val view: View): RecyclerView.ViewHolder(view) {


        fun bind (tree: Tree) {
            // Handles binding the view for the RecyclerListView
            view.findViewById<TextView>(R.id.tree_name).text = tree.name
            view.findViewById<TextView>(R.id.date_spotted).text = "${tree.dateSpotted}"
            view.findViewById<CheckBox>(R.id.heart_check).apply {
                isChecked = tree.favorite ?: false
                setOnCheckedChangeListener { checkBox, isChecked ->
                    treeHeartListener(tree, isChecked) // Issue with Pixel 3 XL Boxes flickering
                }
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context) // parent will be the fragment.
            .inflate(R.layout.fragment_tree_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tree = trees[position]
        holder.bind(tree)
    }

    override fun getItemCount(): Int {
        return trees.size
    }

}