package tk.kvakva.testwork.ui.main

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tk.kvakva.testwork.databinding.ContItemBinding


class RecyViewAdap(val clickListener: RecyViewListener) :
    RecyclerView.Adapter<RecyViewAdap.ViewHolder>() {
    class ViewHolder private constructor(/*itemView: View*/ val binding: ContItemBinding) :
        RecyclerView.ViewHolder(/*itemView*/ binding.root) {

        fun bind(item: ImageFile, clickListener: RecyViewListener) {

            binding.imageFile = item
            binding.recyviewLsnr = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup, viewType: Int): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ContItemBinding.inflate(layoutInflater, parent, false)
                /*val view = layoutInflater
                    .inflate(R.layout.cont_item, parent, false)*/
                return ViewHolder(binding)
            }
        }
    }

    var data = listOf<ImageFile>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, viewType)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(data[position], clickListener)

}

data class ImageFile(
    val Name: String?,
    val Phone: String?,
    val email: String? = "",
    val fName: String = "",
    val fPath: String = "",
    val fUri: Uri? = null,
    val fId: Long = -1
)

class RecyViewListener(val clickListener: (iFl: ImageFile) -> Unit) {
    fun onClick(imageFile: ImageFile) = clickListener(imageFile)
}
