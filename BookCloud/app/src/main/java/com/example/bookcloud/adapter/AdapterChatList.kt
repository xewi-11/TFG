import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookcloud.R
import com.example.bookcloud.model.ChatPreview

class AdapterChatList(
    private val chats: List<ChatPreview>,
    private val onChatClick: (withUserId: String, bookId: String) -> Unit
) : RecyclerView.Adapter<AdapterChatList.ChatViewHolder>() {

    inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgUser: ImageView = view.findViewById(R.id.imgUser)
        val textName: TextView = view.findViewById(R.id.textUserName)
        val textBook: TextView = view.findViewById(R.id.textBookTitle)

        init {
            view.setOnClickListener {
                val chat = chats[adapterPosition]
                onChatClick(chat.withUserId, chat.bookId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_preview, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.textName.text = chat.userName
        holder.textBook.text = "Libro: ${chat.bookTitle}"

        if (!chat.userPhotoUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(chat.userPhotoUrl)
                .placeholder(R.drawable.gente)
                .into(holder.imgUser)
        } else {
            holder.imgUser.setImageResource(R.drawable.gente)
        }
    }

    override fun getItemCount(): Int = chats.size
}