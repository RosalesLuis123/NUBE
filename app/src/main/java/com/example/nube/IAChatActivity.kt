package com.example.nube

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class IAChatActivity : AppCompatActivity() {

    private lateinit var chatContainer: LinearLayout
    private lateinit var chatScrollView: ScrollView
    private lateinit var userInput: EditText
    private lateinit var sendButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iachat)

        // Vincular vistas
        chatContainer = findViewById(R.id.chat_container)
        chatScrollView = findViewById(R.id.chat_scroll_view)
        userInput = findViewById(R.id.user_input)
        sendButton = findViewById(R.id.send_button)

        // Evento del botón enviar
        sendButton.setOnClickListener {
            val userMessage = userInput.text.toString()
            if (userMessage.isNotBlank()) {
                // Añadir mensaje del usuario
                addMessage(userMessage, true)
                userInput.text.clear()

                // Simular respuesta del bot
                addMessage("Esto es una respuesta automática del bot.", false)
            }
        }
    }

    private fun addMessage(message: String, isUser: Boolean) {
        // Crear un nuevo TextView para el mensaje
        val messageView = TextView(this)
        messageView.text = message
        messageView.setPadding(16, 8, 16, 8)
        messageView.textSize = 16f

        // Estilo según sea usuario o bot
        if (isUser) {
            messageView.setBackgroundResource(R.drawable.user_message_background)
            messageView.setTextColor(resources.getColor(android.R.color.white, theme))
        } else {
            messageView.setBackgroundResource(R.drawable.bot_message_background)
            messageView.setTextColor(resources.getColor(android.R.color.black, theme))
        }

        // Añadir el mensaje al contenedor
        chatContainer.addView(messageView)

        // Scroll automático al final
        chatScrollView.post {
            chatScrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
}