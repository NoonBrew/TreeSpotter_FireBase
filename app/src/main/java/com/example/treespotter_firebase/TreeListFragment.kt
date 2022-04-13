package com.example.treespotter_firebase

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.RuntimeException

class TreeListFragment : Fragment() {

    private val treeViewModel: TreeSpotterViewModel by lazy {
        ViewModelProvider(requireActivity()).get(TreeSpotterViewModel::class.java)
    } // If both the ListFragment and MapFragment use requireActivity() They will be linked by the
    // sharing the same activity and will both be able to use the same viewModel?? - is that correct?

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val recyclerView = inflater.inflate(R.layout.fragment_tree_list, container, false)
        // Inflate the layout for this fragment
        if (recyclerView !is RecyclerView) {
            throw RuntimeException("TreeListFragment view should be a recycler view")}

        val trees = listOf<Tree>()
        val adapter = TreeRecyclerViewAdapter(trees)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        treeViewModel.latestTrees.observe(requireActivity()) { treeList ->
            adapter.trees = treeList
            adapter.notifyDataSetChanged()
        }

        return recyclerView
    }

    companion object {

        @JvmStatic
        fun newInstance() = TreeListFragment()
    }
}