package com.manuel.tai.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.manuel.tai.R
import com.manuel.tai.model.QuestionItem

class QuestionAdapter(private var items: List<QuestionItem>) :
    RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {

    fun submitList(newItems: List<QuestionItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_question, parent, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class QuestionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val promptText: TextView = view.findViewById(R.id.questionPromptText)
        private val optionsText: TextView = view.findViewById(R.id.questionOptionsText)
        private val answerText: TextView = view.findViewById(R.id.questionAnswerText)
        private val explanationText: TextView = view.findViewById(R.id.questionExplanationText)

        fun bind(item: QuestionItem) {
            promptText.text = "${item.number}. ${item.prompt}"

            if (item.options.isNotEmpty()) {
                optionsText.text = item.options.joinToString("\n")
                optionsText.visibility = View.VISIBLE
            } else {
                optionsText.visibility = View.GONE
            }

            if (!item.answer.isNullOrBlank()) {
                answerText.text = "Answer: ${item.answer}"
                answerText.visibility = View.VISIBLE
            } else {
                answerText.visibility = View.GONE
            }

            if (!item.explanation.isNullOrBlank()) {
                explanationText.text = item.explanation
                explanationText.visibility = View.VISIBLE
            } else {
                explanationText.visibility = View.GONE
            }
        }
    }
}
