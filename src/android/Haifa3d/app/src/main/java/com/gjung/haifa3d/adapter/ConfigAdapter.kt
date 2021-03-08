
package com.gjung.haifa3d.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
//import com.gjung.haifa3d.ble.IConfigField
import com.example.haifa3d_ble_api.ble.IConfigField
import com.gjung.haifa3d.databinding.ConfigItemBinding
import com.gjung.haifa3d.R
import kotlinx.android.synthetic.main.config_item_header.view.*


@ExperimentalUnsignedTypes
class ConfigAdapter(var fields_list: List<IConfigField>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var onItemClickListener: OnItemClickListener? = null
    var onItemEditClickListener: OnItemClickListener? = null
    var fields: List<IConfigField> = fields_list
    private val TYPE_HEADER : Int = 0
    private val TYPE_LIST : Int = 1

    @FunctionalInterface
    interface OnItemClickListener {
        fun onItemClick(field: IConfigField)
    }

    init {
        setHasStableIds(true)
    }

    override fun getItemViewType(position: Int): Int {

        if(position == 0 || position == 7) // 0/7 postions in recycleview list mark "Basic"/"Advanced" config headers
        {
            return TYPE_HEADER
        }
        return TYPE_LIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == TYPE_HEADER)
        {
            val header = LayoutInflater.from(parent.context).inflate(R.layout.config_item_header,parent,false)
            return header_item(header)
        }

        val binding = ConfigItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return view_item(binding)
    }

    override fun getItemCount(): Int = fields.size

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val field = fields?.get(position)
        when (holder) {
            is view_item -> {

                holder.caption = field.caption
                holder.content = field.content.value?.toString() ?: "-- ? --"
                holder.canEdit = field.canEdit
            }
            is header_item -> {

                holder.caption = field.caption
                val x = 1
            }
        }
    }


    inner class view_item(val binding: ConfigItemBinding) : RecyclerView.ViewHolder(binding.root) {
        var caption: String
            get() = binding.configName.text.toString()
            set(value) { binding.configName.text = value }

        var content: String
            get() = binding.configSubtitle.text.toString()
            set(value) { binding.configSubtitle.text = value }

        var canEdit: Boolean
            get() = binding.editButton.visibility == View.VISIBLE
            set(value) { binding.editButton.visibility = if (value) View.VISIBLE else View.GONE }

        init {
            binding.configContainer.setOnClickListener {
                onItemClickListener?.onItemClick(fields.get(adapterPosition))
            }

            binding.editButton.setOnClickListener {
                onItemEditClickListener?.onItemClick(fields.get(adapterPosition))
            }
        }
    }

    inner class header_item(val binding:  View) : RecyclerView.ViewHolder(binding) {
        var caption: String
            get() = binding.config_header_name.text.toString()
            set(value) { binding.config_header_name.text = value }
        init {
            binding.visibility = View.VISIBLE
        }
    }


}