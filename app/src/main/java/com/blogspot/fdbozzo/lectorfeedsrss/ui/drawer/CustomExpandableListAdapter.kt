package com.blogspot.fdbozzo.lectorfeedsrss.ui.drawer

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.blogspot.fdbozzo.lectorfeedsrss.R

import java.util.HashMap

class CustomExpandableListAdapter internal constructor(
    private val context: Context,
    private val titleList: List<String>,
    private val dataList: HashMap<String, List<String>>) : BaseExpandableListAdapter() {

    override fun getChild(listPosition: Int, expandedListPosition: Int): Any {
        return this.dataList[this.titleList[listPosition]]!![expandedListPosition]
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    override fun getChildView(listPosition: Int, expandedListPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
        var oldViewToReuse = convertView
        val expandedListText = getChild(listPosition, expandedListPosition) as String
        if (oldViewToReuse == null) {
            val layoutInflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            oldViewToReuse = layoutInflater.inflate(R.layout.list_item, null)
        }
        val expandedListTextView = oldViewToReuse!!.findViewById<TextView>(R.id.expandedListItem)
        expandedListTextView.text = expandedListText
        return oldViewToReuse
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return if (this.dataList[this.titleList[listPosition]] == null ||
            this.dataList[this.titleList[listPosition]]?.get(0) == null) {
            0
        } else {
            this.dataList[this.titleList[listPosition]]!!.size
        }
    }

    override fun getGroup(listPosition: Int): Any {
        return this.titleList[listPosition]
    }

    override fun getGroupCount(): Int {
        return this.titleList.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(listPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
        var oldViewToReuse = convertView
        val listTitle = getGroup(listPosition) as String
        if (oldViewToReuse == null) {
            val layoutInflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            oldViewToReuse = layoutInflater.inflate(R.layout.list_group, null)
        }
        val listTitleTextView = oldViewToReuse!!.findViewById<TextView>(R.id.listTitle)
        listTitleTextView.setTypeface(null, Typeface.BOLD)
        listTitleTextView.text = listTitle
        return oldViewToReuse
    }

    override fun hasStableIds(): Boolean {
        return false
    }
//
//    override fun onGroupExpanded(groupPosition: Int) {
//        Toast.makeText(context, (titleList as ArrayList<String>)[groupPosition] + " List Expanded.", Toast.LENGTH_SHORT).show()
//    }
//
//    override fun onGroupCollapsed(groupPosition: Int) {
//        Toast.makeText(context, (titleList as ArrayList<String>)[groupPosition] + " List Collapsed.", Toast.LENGTH_SHORT).show()
//    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }
}
